#!/usr/bin/env python3
"""Backfills gaps in observations.csv on MinIO using the matching media.csv entries

Legacy CamTrap DP (0.1.6) uploads can end up with observations.csv rows that are
missing data the exporter never filled in: a blank timestamp, a blank or stale
sequenceID, and a blank observationID (see observations.csv column layout in
Sanimal FX/src/main/java/model/image/Observations.java). This script scans SPARCd
MinIO collections, matches each observations.csv row to its media.csv row by
mediaID, and fills the gaps:
  - timestamp is copied from the matching media.csv row when blank
  - sequenceID is synced to the matching media.csv row's sequenceID
  - observationID is assigned "<mediaID>:<index>" when blank, where index is the
    row's 0-based position among that mediaID's observations.csv rows (matching
    the `defaultObservationId` scheme in sparcd-explorer-gpt's
    packages/camtrap/src/index.ts)
  - classificationConfidence is set to 0.0 when blank and no scientificName is
    given, or 1.0 when blank and a scientificName is present
  - count and countNew are set to 0 when blank
The original observations.csv is backed up locally before any changes are
uploaded.
"""

import argparse
import concurrent.futures
import csv
import io
import os
import sys

from minio import Minio, S3Error

# The endpoint to connect to
MINIO_ENDPOINT = "wildcats.sparcd.arizona.edu:443"

# The default number of worker threads used to process upload folders
DEFAULT_NUMBER_WORKERS = 10

# The name of the media and observations CSV files
MEDIA_CSV = "media.csv"
OBSERVATIONS_CSV = "observations.csv"

# media.csv column indexes (see Sanimal FX model/image/Media.java toArray()/instance())
MEDIA_COL_MEDIA_ID = 0
MEDIA_COL_SEQUENCE_ID = 2
MEDIA_COL_TIMESTAMP = 4

# observations.csv column indexes (see Sanimal FX model/image/Observations.java
# toArray()/instance())
OBS_COL_OBSERVATION_ID = 0
OBS_COL_SEQUENCE_ID = 2
OBS_COL_MEDIA_ID = 3
OBS_COL_TIMESTAMP = 4
OBS_COL_SCIENTIFIC_NAME = 8
OBS_COL_COUNT = 9
OBS_COL_COUNT_NEW = 10
OBS_COL_CLASSIFICATION_CONFIDENCE = 18

# Describes each gap this script can fill: (stat key, column, label, dry-run verb, verb)
FIELD_FIXES = (
    ("timestamp", OBS_COL_TIMESTAMP, "timestamp", "Found", "Fixed"),
    ("sequence", OBS_COL_SEQUENCE_ID, "sequenceID", "Found", "Fixed"),
    ("observation_id", OBS_COL_OBSERVATION_ID, "observationID", "Would assign", "Assigned"),
    ("confidence", OBS_COL_CLASSIFICATION_CONFIDENCE, "classificationConfidence",
     "Would set", "Set"),
    ("count", OBS_COL_COUNT, "count", "Would set", "Set"),
    ("count_new", OBS_COL_COUNT_NEW, "countNew", "Would set", "Set"),
)


def get_params() -> tuple:
    """Parses command line arguments
    Returns:
        A tuple of (collection UUID or None for all, username, password, dry_run flag,
                    number of worker threads)
    """
    parser = argparse.ArgumentParser(
        description='Backfills missing observations.csv timestamps, sequenceIDs, and '
                    'observationIDs using the matching media.csv entries (CamTrap DP 0.1.6)'
    )
    parser.description = f'Fixes missing SPARCd observations.csv gaps on MinIO ' \
                         f'({MINIO_ENDPOINT})'

    parser.add_argument('-user', required=True, help='Username for MinIO endpoint')
    parser.add_argument('-pw', required=True, help='Password for MinIO endpoint')
    parser.add_argument('uuid_id', type=str,
                        help='MinIO collection UUID to fix (use - to fix all collections)')
    parser.add_argument('--dry-run', action='store_true',
                        help='Report what would be changed without uploading anything')
    parser.add_argument('--workers', type=int, default=DEFAULT_NUMBER_WORKERS,
                        help='Number of worker threads used to process collections and '
                             f'upload folders concurrently (default: {DEFAULT_NUMBER_WORKERS})')

    args = parser.parse_args()

    return args.uuid_id if args.uuid_id != '-' else None, args.user, args.pw, args.dry_run, \
           args.workers


def write_error(msg: str) -> None:
    """Writes error messages
    Arguments:
        msg - the message to write
    """
    print(msg, file=sys.stderr, flush=True)


def new_stats() -> dict:
    """Creates a zeroed stats dict covering all rows checked and every fixable field
    Return:
        A dict with a "checked" key and one key per entry in FIELD_FIXES, all set to 0
    """
    stats = {"checked": 0}
    stats.update({key: 0 for key, _, _, _, _ in FIELD_FIXES})
    return stats


def add_stats(totals: dict, stats: dict) -> None:
    """Accumulates one upload's stats into a running total, in place
    Arguments:
        totals - the running totals dict to update
        stats - the stats dict to add into totals
    """
    for key, value in stats.items():
        totals[key] += value


def backup_csv(bucket: str, csv_path: str, raw: bytes) -> str:
    """Writes a local backup of a CSV file before it's modified
    Arguments:
        bucket - the bucket the CSV came from
        csv_path - the CSV object path within the bucket
        raw - the original, unmodified bytes of the CSV file
    Return:
        The local path the backup was written to
    """
    local_path = os.path.join(bucket, csv_path)
    os.makedirs(os.path.dirname(local_path), exist_ok=True)
    with open(local_path, "wb") as out_file:
        out_file.write(raw)

    return local_path


def download_object_bytes(minio: Minio, bucket: str, path: str) -> bytes:
    """Downloads a MinIO object into memory
    Arguments:
        minio - the MinIO client instance to use
        bucket - the bucket the object is in
        path - the path of the object
    Return:
        The object's raw bytes, or None if the object doesn't exist
    """
    response = None
    try:
        response = minio.get_object(bucket, path)
        return response.read()
    except S3Error as ex:
        if ex.code == "NoSuchKey":
            return None
        raise
    finally:
        if response is not None:
            response.close()
            response.release_conn()


def index_media_by_id(media_rows: list) -> dict:
    """Indexes media.csv rows by mediaID
    Arguments:
        media_rows - the parsed media.csv rows
    Return:
        A dict mapping mediaID to its media.csv row
    """
    by_media_id = {}
    for row in media_rows:
        if len(row) <= MEDIA_COL_MEDIA_ID or not row[MEDIA_COL_MEDIA_ID]:
            continue
        by_media_id[row[MEDIA_COL_MEDIA_ID]] = row

    return by_media_id


def fix_observation_row(row: list, media_row: list, media_index: int) -> dict:
    """Backfills the gaps in a single observations.csv row
    Arguments:
        row - the observations.csv row to update in place
        media_row - the matching media.csv row to pull data from, or None if there isn't one
        media_index - this row's 0-based position among all observations.csv rows that
                     share its mediaID, used to build a blank observationID
    Return:
        A dict keyed by each entry in FIELD_FIXES, True where that field was changed
    """
    fixed = {key: False for key, _, _, _, _ in FIELD_FIXES}

    if not row[OBS_COL_OBSERVATION_ID].strip():
        # Matches defaultObservationId() in sparcd-explorer-gpt's
        # packages/camtrap/src/index.ts: "<mediaID>:<index>"
        row[OBS_COL_OBSERVATION_ID] = f"{row[OBS_COL_MEDIA_ID]}:{media_index}"
        fixed["observation_id"] = True

    if media_row is not None:
        if not row[OBS_COL_TIMESTAMP].strip() and media_row[MEDIA_COL_TIMESTAMP].strip():
            row[OBS_COL_TIMESTAMP] = media_row[MEDIA_COL_TIMESTAMP]
            fixed["timestamp"] = True

        media_sequence_id = media_row[MEDIA_COL_SEQUENCE_ID]
        if media_sequence_id.strip() and row[OBS_COL_SEQUENCE_ID] != media_sequence_id:
            row[OBS_COL_SEQUENCE_ID] = media_sequence_id
            fixed["sequence"] = True

    if (len(row) > OBS_COL_CLASSIFICATION_CONFIDENCE
            and not row[OBS_COL_CLASSIFICATION_CONFIDENCE].strip()):
        has_species = (len(row) > OBS_COL_SCIENTIFIC_NAME
                       and bool(row[OBS_COL_SCIENTIFIC_NAME].strip()))
        row[OBS_COL_CLASSIFICATION_CONFIDENCE] = "1.0" if has_species else "0.0"
        fixed["confidence"] = True

    if len(row) > OBS_COL_COUNT and not row[OBS_COL_COUNT].strip():
        row[OBS_COL_COUNT] = "0"
        fixed["count"] = True

    if len(row) > OBS_COL_COUNT_NEW and not row[OBS_COL_COUNT_NEW].strip():
        row[OBS_COL_COUNT_NEW] = "0"
        fixed["count_new"] = True

    return fixed


def fix_observations_csv(minio: Minio, bucket: str, uploads_base: str, dry_run: bool) -> dict:
    """Backfills gaps in a single upload's observations.csv using its media.csv
    Arguments:
        minio - the MinIO client instance to use
        bucket - the bucket the upload is in
        uploads_base - the path to the upload's folder
        dry_run - if True, don't upload the corrected observations.csv or back up the original
    Return:
        A stats dict (see new_stats()) describing what was checked and fixed
    """
    media_path = os.path.join(uploads_base, MEDIA_CSV)
    obs_path = os.path.join(uploads_base, OBSERVATIONS_CSV)

    stats = new_stats()

    raw_obs = download_object_bytes(minio, bucket, obs_path)
    if raw_obs is None:
        return stats

    raw_media = download_object_bytes(minio, bucket, media_path)
    media_by_id = index_media_by_id(
        list(csv.reader(io.StringIO(raw_media.decode("utf-8"))))) if raw_media else {}

    rows = list(csv.reader(io.StringIO(raw_obs.decode("utf-8"))))
    media_row_counts = {}
    for row in rows:
        if len(row) <= OBS_COL_TIMESTAMP:
            continue

        media_id = row[OBS_COL_MEDIA_ID]
        media_index = media_row_counts.get(media_id, 0)
        media_row_counts[media_id] = media_index + 1

        media_row = media_by_id.get(media_id)
        if media_row is None and media_id.strip():
            write_error(f"    WARNING: no media.csv entry found for mediaID '{media_id}'")

        fixed = fix_observation_row(row, media_row, media_index)

        for key, col, label, dry_verb, verb in FIELD_FIXES:
            if fixed[key]:
                stats[key] += 1
                action = dry_verb if dry_run else verb
                print(f"    {action} {label} for '{row[OBS_COL_MEDIA_ID]}' -> "
                     f"{row[col]}", flush=True)

    stats["checked"] = len(rows)
    changed = any(stats[key] for key, _, _, _, _ in FIELD_FIXES)
    if changed and not dry_run:
        backup_path = backup_csv(bucket, obs_path, raw_obs)
        print(f"    Backed up original to '{backup_path}'", flush=True)

        out = io.StringIO()
        csv.writer(out, lineterminator="\n").writerows(rows)
        data = out.getvalue().encode("utf-8")
        minio.put_object(bucket, obs_path, io.BytesIO(data), len(data), content_type="text/csv")

    return stats


def list_collection_uploads(minio: Minio, minio_id: str) -> list:
    """Finds the upload folders belonging to a collection
    Arguments:
        minio - the MinIO client instance to use
        minio_id - the collection ID on MinIO to look up
    Return:
        A list of (bucket, uploads_base) tuples, one per upload folder found. Empty if the
        collection's bucket doesn't exist
    """
    dest_bucket = "sparcd-" + minio_id
    dest_uploads_folder = os.path.join("Collections", minio_id, "Uploads")

    print(f"  [{minio_id}] Checking if bucket '{dest_bucket}' exists", flush=True)
    if not minio.bucket_exists(dest_bucket):
        write_error(f"ERROR: bucket not found {dest_bucket}")
        return []

    return [(dest_bucket, one_result.object_name)
            for one_result in minio.list_objects(dest_bucket, dest_uploads_folder + "/")
            if one_result.is_dir]


def process_upload(minio: Minio, minio_id: str, bucket: str, uploads_base: str,
                   dry_run: bool) -> dict:
    """Worker entry point: backfills observations.csv gaps for a single upload folder
    Arguments:
        minio - the MinIO client instance to use
        minio_id - the collection ID the upload belongs to (used for logging only)
        bucket - the bucket the upload is in
        uploads_base - the path to the upload's folder
        dry_run - if True, don't upload the corrected observations.csv or back up the original
    Return:
        A stats dict (see new_stats()) describing what was checked and fixed
    """
    print(f"  [{minio_id}] Processing upload '{uploads_base}'", flush=True)
    stats = fix_observations_csv(minio, bucket, uploads_base, dry_run)

    parts = [f"{stats[key]} {label}(s)" for key, _, label, _, _ in FIELD_FIXES if stats[key]]
    if parts:
        action = "would fix" if dry_run else "fixed"
        print(f"  [{minio_id}] {action} " + " and ".join(parts) +
              f" of {stats['checked']} observation entries in '{uploads_base}'", flush=True)
    else:
        print(f"  [{minio_id}] Unchanged '{uploads_base}' "
             f"({stats['checked']} observation entries checked)", flush=True)

    return stats


def get_sparcd_ids(minio: Minio) -> list:
    """Returns the list of SPARCd MinIO collection IDs
    Arguments:
        minio - the MinIO client instance to use
    """
    prefix = "sparcd-"
    return [one_bucket.name[len(prefix):] for one_bucket in minio.list_buckets()
            if one_bucket.name.startswith(prefix)]


def process(minio_id: str, user: str, pw: str, dry_run: bool,
           workers: int = DEFAULT_NUMBER_WORKERS) -> None:
    # pylint: disable=invalid-name
    """Fixes missing observations.csv gaps on MinIO
    Arguments:
        minio_id - (optional) corresponding MinIO collection ID to fix
        user - username for MinIO endpoint
        pw - password for MinIO endpoint
        dry_run - if True, don't upload any corrected observations.csv files or back up
                 originals
        workers - number of worker threads to process collections and upload folders with
    """
    print(f"Connecting to MinIO as user {user} '{MINIO_ENDPOINT}'", flush=True)
    minio = Minio(MINIO_ENDPOINT, access_key=user, secret_key=pw)

    if dry_run:
        print("Dry run: no changes will be uploaded", flush=True)

    minio_ids = get_sparcd_ids(minio) if minio_id is None else [minio_id]

    # Discover every upload folder up front (across all collections) so that collections and
    # their upload folders are all processed concurrently by a single worker pool
    print(f"Discovering upload folders for {len(minio_ids)} collection(s) "
          f"using {workers} worker thread(s)", flush=True)
    totals = {one_id: new_stats() for one_id in minio_ids}
    tasks = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=workers) as executor:
        discover_futures = {executor.submit(list_collection_uploads, minio, one_id): one_id
                            for one_id in minio_ids}
        for future in concurrent.futures.as_completed(discover_futures):
            one_id = discover_futures[future]
            try:
                for bucket, uploads_base in future.result():
                    tasks.append((one_id, bucket, uploads_base))
            # pylint: disable=broad-exception-caught
            except Exception as ex:
                write_error(f"ERROR: failed listing uploads for collection '{one_id}': {ex}")

        print(f"Processing {len(tasks)} upload folder(s) using {workers} worker thread(s)",
              flush=True)
        upload_futures = {
            executor.submit(process_upload, minio, one_id, bucket, uploads_base, dry_run):
                (one_id, bucket, uploads_base)
            for one_id, bucket, uploads_base in tasks
        }
        for future in concurrent.futures.as_completed(upload_futures):
            one_id, _, uploads_base = upload_futures[future]
            try:
                stats = future.result()
            # pylint: disable=broad-exception-caught
            except Exception as ex:
                write_error(f"ERROR: failed processing upload '{uploads_base}' "
                           f"in collection '{one_id}': {ex}")
                continue
            add_stats(totals[one_id], stats)

    action = "would fix" if dry_run else "fixed"
    for one_id in minio_ids:
        stats = totals[one_id]
        parts = [f"{stats[key]} {label}(s)" for key, _, label, _, _ in FIELD_FIXES]
        print(f"Collection '{one_id}': checked {stats['checked']} observation entries, "
              f"{action} " + ", ".join(parts[:-1]) + f", and {parts[-1]}", flush=True)
    print("Done", flush=True)


if __name__ == '__main__':
    m_minio_id, m_user, m_pw, m_dry_run, m_workers = get_params()
    process(m_minio_id, m_user, m_pw, m_dry_run, m_workers)
