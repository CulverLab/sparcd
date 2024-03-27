#!/usr/bin/env python3
"""Python script for transferring data from CyVerse to MinIO
"""

import argparse
import os
import shutil
import subprocess
import sys
import tempfile
from typing import Optional
from minio import Minio, S3Error

# The endpoint to upload to
MINIO_ENDPOINT = "wildcats.sparcd.arizona.edu:443"

# MinIO CSV files to be updated
DEPLOYMENT_CSV = "deployments.csv"
MEDIA_CSV = "media.csv"
OBSERVATIONS_CSV = "observations.csv"
MINIO_CAMTRAP_FILES = [DEPLOYMENT_CSV, MEDIA_CSV, OBSERVATIONS_CSV]

# Camtrap Dict entries
CAMTRAP_DEPLOYMENT = "deployment"
CAMTRAP_MEDIA = "media"
CAMTRAP_OBSERVATIONS = "observations"

EXIF_CODE_SPECIES = "Exif_0x0228"
EXIF_CODE_LOCATION = "Exif_0x0229"

def get_params() -> tuple:
    """Get the paths to the files
    Returns:
        A tuple containing the two paths
    """
    parser = argparse.ArgumentParser(description='Fix multiple species Camtrap on MinIO')
    parser.description = f'Looks into images on MinIO for multiple species ({MINIO_ENDPOINT})'

    parser.add_argument('-user', required=True,
                        help='Username for minio endpoint')
    parser.add_argument('-pw', required=True, help='Password for minio endpoint')
    parser.add_argument('uuid_id', type=str,
                        help='MinIO collection UUID to upload to (use - to skip)')

    args = parser.parse_args()

    return args.uuid_id if args.uuid_id != '-' else None, \
           args.user, \
           args.pw


def write_error(msg: str) -> None:
    """Writes error messages
    Arguments:
        msg - the message to write
    """
    print(msg, file=sys.stderr, flush=True)


def write_camtrap(camtrap: dict, folder: str) -> None:
    """Writes CamTrap data to the specified folder
    Arguments:
        camtrap - the CamTrap dict to write
        folder - the folder to write
    """
    with open(os.path.join(folder, DEPLOYMENT_CSV), "w", encoding="utf-8") as out_file:
        for one_line in camtrap[CAMTRAP_DEPLOYMENT]:
            out_file.writelines(f"{one_line}\n")
    with open(os.path.join(folder, MEDIA_CSV), "w", encoding="utf-8") as out_file:
        for one_line in camtrap[CAMTRAP_MEDIA]:
            out_file.writelines(f"{one_line}\n")
    with open(os.path.join(folder, OBSERVATIONS_CSV), "w", encoding="utf-8") as out_file:
        for one_line in camtrap[CAMTRAP_OBSERVATIONS]:
            out_file.writelines(f"{one_line}\n")


def upload_local_files(files: list, minio: Minio, bucket: str, dest_basepath: str,
                       content_type: str = "application/json") -> None:
    """Uploads files to MinIO
    Arguments:
        files - the list of local files to load
        minio - the MinIO client instance to use
        bucket - the bucket to upload to
        dest_basepath - the base path to load files to
        content_type - the content type these uploads represent
    Notes:
        The files are loaded in a flat hierarchy regardless of how they are stored locally
    """
    for one_file in files:
        dest_path = os.path.join(dest_basepath, os.path.basename(one_file))
        minio.fput_object(bucket, dest_path, one_file, content_type=content_type)


def get_sparcd_ids(minio: Minio) -> list:
    """Returns the list of SPARCd MinIO IDs
    Arguments:
        minio - the MinIO client instance to use
    """
    buckets = minio.list_buckets()

    found_ids = []
    prefix = "sparcd-"

    for one_bucket in buckets:
        if one_bucket.name.startswith(prefix):
            found_ids.append(one_bucket.name[len(prefix):])

    return found_ids


def download_minio_files(minio: Minio, bucket: str, files: list, local_dir: str) -> bool:
    """Downloads files from MinIO
    Arguments:
        minio - the MinIO client instance to use
        bucket - the bucket to download from
        files - the list of files to download
        local_dir - where to place the files (in a flat hierarchy)
    """
    success = 0
    for one_file in files:
        dest_file = os.path.join(local_dir, os.path.basename(one_file))
        try:
            minio.fget_object(bucket, one_file, dest_file)
        except S3Error as ex:
            if ex.code != "NoSuchKey":
                raise ex

        if os.path.exists(dest_file):
            success = success + 1

    return success == len(files)


def load_camtrap(folder: str) -> dict:
    """Loads camtrap data from local folder
    Arguments:
        folder - the path to the camtrp files
    Return:
        A dict containing the deployments, media, observations string lists
        (each line is not csv separated)
    """
    depl = None
    media = None
    obs = None

    cur_path = os.path.join(folder, DEPLOYMENT_CSV)
    if os.path.exists(cur_path):
        with open(cur_path, "r", encoding="utf-8") as in_file:
            depl = [med.strip("\n") for med in in_file.readlines()]
    else:
        depl = []

    cur_path = os.path.join(folder, MEDIA_CSV)
    if os.path.exists(cur_path):
        with open(cur_path, "r", encoding="utf-8") as in_file:
            media = [med.strip("\n") for med in in_file.readlines()]
    else:
        media = []

    cur_path = os.path.join(folder, OBSERVATIONS_CSV)
    if os.path.exists(cur_path):
        with open(cur_path, "r", encoding="utf-8") as in_file:
            obs = [med.strip("\n") for med in in_file.readlines()]
    else:
        obs = []

    return {CAMTRAP_DEPLOYMENT: depl,
            CAMTRAP_MEDIA: media,
            CAMTRAP_OBSERVATIONS: obs,
            "modified": False}


def split_species_string(species: str) -> tuple:
    """Splits the EXIF string into an array of species information
    Arguments:
        species - the EXIT species string
    Returns:
        A tuple of species information strings
    """
    return_species = []
    working_str = species
    last_sep = 0
    cur_start = 0
    while True:
        cur_sep = working_str.find(',', last_sep)
        if cur_sep == -1:
            break
        last_sep = cur_sep + 1
        cur_sep = working_str.find(',', last_sep)
        if cur_sep == -1:
            break
        last_sep = cur_sep + 1
        cur_sep = working_str.find('.', last_sep)
        if cur_sep == -1:
            break
        last_sep = cur_sep + 1
        return_species.append(working_str[cur_start:cur_sep])
        cur_start = last_sep + 2
        if cur_start > len(species):
            break
    return return_species


def get_image_info(minio: Minio, bucket: str, image_path: str, work_dir: str) -> Optional[tuple]:
    """Pulls the image from minio and loads the embedded information
    Arguments:
        minio - the MinIO client instance to use
        bucket - the bucket the image is in
        image_path - the path of the image to check
        work_dir - working folder
    Returns:
        Retuns a tuple containing the species information in the image
    """
    local_image = os.path.join(work_dir, os.path.basename(image_path))

    download_minio_files(minio, bucket, (image_path,), work_dir)
    if not os.path.exists(local_image):
        print("MinIO download failed for image {image_path} to {local_image}")
        return None

    cmd = ["exiftool", "-U", "-v3", local_image]
    res = subprocess.run(cmd, capture_output=True, check=True)

    skip_line = 0
    found_species = False
    found_location = False
    species_string = ''
    location_string = ''
    all_lines = res.stdout.decode("utf-8").split('\n')
    for one_line in all_lines:
        if skip_line > 0:
            skip_line = skip_line - 1
            continue
        if EXIF_CODE_SPECIES in one_line:
            skip_line = 1
            found_species = True
            continue
        if EXIF_CODE_LOCATION in one_line:
            skip_line = 1
            found_location = True
            continue
        if found_species is True:
            if '[' in one_line:
                species_string = species_string + one_line[one_line.index('[') + 1:].rstrip(']')
            else:
                found_species = False
        if found_location is True:
            if '[' in one_line:
                location_string = location_string + one_line[one_line.index('[') + 1:].rstrip(']')
            else:
                found_location = False

#    print(f"HACK: Found info: {species_string} and {location_string}")
    if len(species_string) <= 0:
        print("WARNING: no species found in image")
        return None, None
    if len(location_string) <= 0:
        print("WARNING: no location found in image")
        return None, None
    return_species = []
    for one_species in split_species_string(species_string):
        common, scientific, count = [val.strip() for val in one_species.split(',')]
        return_species.append({'common': common, 'scientific': scientific, 'count': count})

    locs = location_string.rstrip('.').split('.')
    return_location = [locs[0], locs[len(locs)-1]]

#    print(f"HACK: Species: {return_species} Location: {return_location}")
    return return_species, return_location


def camtrap_species(camtrap: dict, media_id: str) -> int:
    """Counts the species in the Camtrap data for an image (media)
    Arguments:
        camtrap - the Camtrap data
        media_id - the image ID to find species for
    Return:
        The count of found species
    """
    found_species = 0

    for one_media in camtrap[CAMTRAP_OBSERVATIONS]:
        if media_id in one_media:
            found_species = found_species + 1

    return found_species

def camtrap_location(camtrap: dict, locations: tuple) -> bool:
    """Matches the lat-lon in the Camtrap data for an image (media)
    Arguments:
        camtrap - the Camtrap data
        locations - the location information to match
    Return:
        True is returned if the location information is found for one location, and
        False if not
    """
    for one_loc in camtrap[CAMTRAP_DEPLOYMENT]:
        missed = False
        for one_match in locations:
            if not one_match in one_loc:
                missed = True
                break

        if not missed:
            return True

    return False


def fix_camtrap_minio(minio: Minio, minio_id: str) -> None:
    """Performs the fixes to CamTrap
    Arguments:
        minio - the MinIO client instance to use
        minio_id - the collection ID on MinIO to upload to
    """
    dest_bucket = "sparcd-" + minio_id
    dest_coll_base = os.path.join("Collections", minio_id)

    # Check if the collection already exists and create it if not
    print(f"  Checking if bucket '{dest_bucket}' exists", flush=True)
    if not minio.bucket_exists(dest_bucket):
        write_error(f"ERROR: bucket not found {dest_bucket}")
        return

    # List MinIO subpaths under Uploads folder
    dest_uploads_folder = os.path.join(dest_coll_base, "Uploads/")
 #   print(f"HACK: checking MinIO path '{dest_uploads_folder}'")
    for one_result in minio.list_objects(dest_bucket, dest_uploads_folder):
        if not one_result.is_dir:
            continue
        if one_result.object_name == dest_uploads_folder:
            continue

        # Get a temporary folder to work within
        work_dir = tempfile.mkdtemp(prefix="dsparcd_")

        dest_uploads_base = one_result.object_name

        # Get the deployments.csv, media.csv, and observations.csv files, and load them
        print(f" ... pulling camtrap files from '{dest_uploads_base}' to '{work_dir}'", flush=True)
        download_minio_files(minio, dest_bucket,
                         [os.path.join(dest_uploads_base, fn) for fn in MINIO_CAMTRAP_FILES],
                         work_dir)
        camtrap = load_camtrap(work_dir)

        # Check lat-lon
        if len(camtrap[CAMTRAP_DEPLOYMENT]) > 0 and camtrap[CAMTRAP_DEPLOYMENT][0] and \
           len(camtrap[CAMTRAP_DEPLOYMENT][0]) > 0:
            dep_info = camtrap[CAMTRAP_DEPLOYMENT][0].split(",")
            lat = int(float(dep_info[3].strip('"')))
            lon = int(float(dep_info[4].strip('"')))
            if lat == 0 or lon == 0:
                print(f"FOUND: Lat-Lon values are not valid (missing integer portion)" \
                      f" {dep_info[3]}  {dep_info[4]}")
            if lat < -122 or lat > -100:
                print(f"FOUND: Lat-Lon values are not valid (Lon out of bounds) {dep_info[3]}" \
                      f"  {dep_info[4]}")
            if lon < 30 or lon > 40:
                print(f"FOUND: Lat-Lon values are not valid (Lat out of bounds) {dep_info[3]}" \
                      f"  {dep_info[4]}")
        else:
            print(f"FOUND: Missing Deployment data {camtrap[CAMTRAP_DEPLOYMENT]}")
            continue

        # Loop through the images
        print(f"HACK: Pulling image folders from {dest_uploads_base}", flush=True)
        for one_upload in minio.list_objects(dest_bucket, dest_uploads_base):
            if not one_upload.is_dir:
                continue
            base_image_dir = one_upload.object_name
            for one_image in minio.list_objects(dest_bucket, base_image_dir):
                if one_image.is_dir:
                    print(f"WARNING: FOUND SUBFOLDER: {one_image.object_name}", flush=True)
                    continue
                species, locations = get_image_info(minio, dest_bucket, one_image.object_name,
                                                    work_dir)
                num_species = camtrap_species(camtrap, one_image.object_name)
                species_len = len(species) if species else 0
                if num_species != species_len:
                    print("FOUND: Mimatched number of species", one_image.object_name,
                          "found", num_species, "vs", len(species) if species is not None else 0,
                          flush=True)
                if locations and not camtrap_location(camtrap, locations):
                    print("FOUND: Image location mismatch", one_image.object_name, " -> ",
                          locations, flush=True)
                elif not locations:
                    print("FOUND: Image missing locations", one_image.object_name, flush=True)

        # Write the CamTrap data and upload the CSV files
        if camtrap["modified"]:
            print(" ... uploading camtrap files", flush=True)
            write_camtrap(camtrap, work_dir)
            upload_local_files([os.path.join(work_dir, fn) for fn in MINIO_CAMTRAP_FILES], minio,
                           dest_bucket, dest_uploads_base, content_type="text/csv")
#        else:
#            print(" ... camtrap files not modified - skipping upload", flush=True)

        # Clean up the temporary folder
        print(f" ... removing working folder {work_dir}", flush=True)
        shutil.rmtree(work_dir)


def process_images(minio_id: str, user: str, pw: str) -> None:
    # pylint: disable=invalid-name
    """Fixes CamTrap data on MinIO
    Arguments:
        minio_id - (optional) corresponding MinIO collection ID to upload to
        user - username for MinIO endpoint
        pw - password for MinIO endpoint
    """

    # Create the MinIO Client instance
    print(f"Connecting to MinIO as user {user} '{MINIO_ENDPOINT}'", flush=True)
    minio = Minio(MINIO_ENDPOINT, access_key=user, secret_key=pw)

    # Print out what we're doing
    print(f"MinIO endpoint is {MINIO_ENDPOINT}", flush=True)

    if minio_id is None:
        minio_ids = get_sparcd_ids(minio)
    else:
        minio_ids = [minio_id]

    for one_id in minio_ids:
        print("Processing ID " + one_id, flush=True)
        fix_camtrap_minio(minio, one_id)
    print("Done", flush=True)


if __name__ == '__main__':
    m_minio_id, m_user, m_pw = get_params()
    process_images(m_minio_id, m_user, m_pw)
