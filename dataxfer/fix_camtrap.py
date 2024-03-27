#!/usr/bin/env python3
"""Python script for transferring data from CyVerse to MinIO
"""

import argparse
import datetime
import io
import json
import os
import shutil
import subprocess
import sys
import tempfile
import uuid
from minio import Minio, S3Error

# The endpoint to upload to
MINIO_ENDPOINT = "wildcats.sparcd.arizona.edu:443"
# JSON key where the files to move are found
UPLOADS_KEYWORD = "Uploads"
# JSON key where the base CyVerse path is found
BASEPATH_KEYWORD = "BasePath"
# JSON key for collection upload subpath
COLL_UPLOAD_KEYWORD = "Upload"
# JSON key for collection images
COLL_IMAGES_KEYWORD = "Images"

# CyVerse files that are transferred
COLL_COLLECTION_FILE = "collection.json"
COLL_PERMISSIONS_FILE = "permissions.json"
COLL_UPLOADMETA_FILE = "UploadMeta.json"

# MinIO CSV files to be updated
DEPLOYMENT_CSV = "deployments.csv"
MEDIA_CSV = "media.csv"
OBSERVATIONS_CSV = "observations.csv"
MINIO_CAMTRAP_FILES = [DEPLOYMENT_CSV, MEDIA_CSV, OBSERVATIONS_CSV]

# Camtrap Dict entries
CAMTRAP_DEPLOYMENT = "deployment"
CAMTRAP_MEDIA = "media"
CAMTRAP_OBSERVATIONS = "observations"

UPLOAD_LOG_FILENAME = "upload_log.csv"

class ImageTrack:
    """Class used to track image progress"""
    image_name = ""
    cyverse_basepath = ""
    minio_bucket = ""
    minio_basepath = ""
    progress = []

    def __init__(self, cyverse_basepath: str, minio_bucket: str, minio_basepath: str,
                 image_data: dict):
        """Initialize class instance with parameters
        Argument:
            cyverse_basepath - the base path we're pulling from
            minio_bucket - the destination bucket
            minio_basepath - the base path on minio
            image_data - the data associated with the image
        """
        self.cyverse_basepath = cyverse_basepath
        self.minio_bucket = minio_bucket
        self.minio_basepath = minio_basepath
        self.image_name = image_data["RelativePath"]
        self.progress = []

    def add_progress(self, msg: str) -> None:
        """Adds a progress string to the results
        Arguments:
            msg - the string to add
        """
        self.progress.append(msg)

    def complete(self, folder_path: str = None, msg: str = None) -> None:
        """Called to complete the entry
        Arguments:
            folder_path - the path to write the entry to
            msg - a final message to write
        """
        if msg:
            self.progress.append(msg)

        if folder_path:
            filename = os.path.join(folder_path, UPLOAD_LOG_FILENAME)
        else:
            filename = os.path.join(os.getcwd(), UPLOAD_LOG_FILENAME)

        open_flags = "a"
        if not os.path.exists(filename):
            open_flags = "w"

        with open(filename, open_flags, encoding="utf-8") as o_file:
            if open_flags == "w":
                o_file.write(",".join(("Image", "CyVerse path", "Bucket", "MinIO path", "Progress")) + "\n")
            o_file.write(",".join((self.image_name, self.cyverse_basepath, self.minio_bucket,
                                  self.minio_basepath, '"' + "\r".join(self.progress) + '"')) + "\n")


def get_params() -> tuple:
    """Get the paths to the files
    Returns:
        A tuple containing the two paths
    """
    parser = argparse.ArgumentParser(description='Moves files from CyVerse to MinIO')
    parser.description = f'Moves SPARCd data from CyVerse to MinIO ({MINIO_ENDPOINT})'

    parser.add_argument('-user', required=True,
                        help='Username for minio endpoint')
    parser.add_argument('-pw', required=True, help='Password for minio endpoint')
    parser.add_argument('uuid_id', type=str,
                        help='MinIO collection UUID to upload to (use - to skip)')

    parser.epilog = "Assumes that iRODS commands have been installed and configured"

    args = parser.parse_args()

    return args.uuid_id if args.uuid_id != '-' else None, \
           args.user, \
           args.pw


def get_deployment_id(collection_id: str, image_data: dict) -> str:
    """Returns the deployment ID for this image
    Arguments:
        collection_id - the ID of the collection
        image_data - the image's data
    """
    return collection_id + ':' + image_data["locationID"]


def write_error(msg: str) -> None:
    """Writes error messages
    Arguments:
        msg - the message to write
    """
    print(msg, file=sys.stderr, flush=True)


def exists_minio(minio: Minio, bucket: str, minio_path: str) -> bool:
    """Checks if an object exists on Minio
    Arguments:
        minio - the MinIO client instance to use
        bucket - the destination bucket
        minio_path - the destination path
    """
    try:
        if minio.stat_object(bucket, minio_path):
            return True
    except S3Error as ex:
        if ex.code != "NoSuchKey":
            raise ex

    return False


def get_cyverse_file(cyverse_path: str, print_errs: bool = False) -> bool:
    """Tries to pull down a CyVerse file to the current folder
    Arguments:
        cyverse_path - the CyVerse path to the file
        print_errs - flag to indicate errors should be reported
    """
    cmd = ['iget', '-K', "-f", cyverse_path]
    res = subprocess.run(cmd, capture_output=True, check=False)
    if res.returncode != 0:
        if print_errs:
            print(res, flush=True)
        return False

    return True


def transfer_file(minio: Minio, cyverse_path: str, bucket: str, minio_path: str,
                  print_errs: bool = False, content_type: str = None) -> bool:
    """Attempts to transfer a file from CyVerse to MinIO
    Arguments:
        minio - the MinIO client instance to use
        cyverse_path - the CyVerse path to the file
        bucket - the destination bucket
        minio_path - the destination path
        print_errs - flag to indicate errors should be reported
    """
    basename = os.path.basename(cyverse_path)

    if not get_cyverse_file(cyverse_path, print_errs):
        if print_errs:
            write_error(f"Failed to retrieve file '{cyverse_path}")
        return False

    if not os.path.exists(basename):
        if print_errs:
            write_error(f"Not able to find downloaded file {basename}")
        return False

    if content_type is None:
        content_type = "application/octet-stream"
    minio.fput_object(bucket, minio_path, basename, content_type=content_type)

    os.unlink(basename)
    return True


def fix_collection_file(minio: Minio, minio_id: str, bucket: str,
                        minio_path: str, local_dir: str, print_errs: bool = False) -> bool:
    """Fixes up the collection file that exists on MinIO
    Arguments:
        minio - the MinIO client instance to use
        minio_id - ID associated with this collection
        bucket - the destination bucket
        minio_path - the destination path
        local_dir - local folder for storing data
        print_errs - flag to indicate errors should be reported
    """
    basename = os.path.basename(minio_path)
    if not download_minio_files(minio, bucket, [minio_path], local_dir):
        if print_errs:
            write_error(f"Not able to find downloaded collection file {minio_path}")
        return False

    local_path = os.path.join(local_dir, basename)
    with open(local_path, "r", encoding="utf-8") as in_file:
        coll_json = json.load(in_file)
    os.unlink(local_path)

    if minio_id not in coll_json["descriptionProperty"]:
        coll_json["descriptionProperty"] = f"\nCollection ID  {minio_id}"
        upload_json(coll_json, minio, bucket, minio_path)

    return True


def transfer_collection_file(minio: Minio, minio_id: str, cyverse_path: str, bucket: str,
                             minio_path: str, print_errs: bool = False) -> bool:
    """Transfers a collection file from CyVerse to MinIO
    Arguments:
        minio - the MinIO client instance to use
        minio_id - ID associated with this collection
        cyverse_path - the CyVerse path to the collection file
        bucket - the destination bucket
        minio_path - the destination path
        print_errs - flag to indicate errors should be reported
    """
    basename = os.path.basename(cyverse_path)
    if not get_cyverse_file(cyverse_path, print_errs):
        if print_errs:
            write_error(f"Not able to find downloaded file {basename}")
        return False

    with open(basename, "r", encoding="utf-8") as in_file:
        coll_json = json.load(in_file)
    os.unlink(basename)

    coll_json["idProperty"] = minio_id
    coll_json["descriptionProperty"] = f"\nCollection ID  {minio_id}"
    if "bucketProperty" not in coll_json:
        coll_json["bucketProperty"] = bucket
        return upload_json(coll_json, minio, bucket, minio_path)

    upload_json(coll_json, minio, bucket, minio_path)
    return True


def transfer_uploadmeta_file(minio: Minio, cyverse_path: str, bucket: str,
                             minio_path: str, print_errs: bool = True) -> bool:
    """Transfers a MetaData file from CyVerse to MinIO
    Arguments:
        minio - the MinIO client instance to use
        cyverse_path - the CyVerse path to the collection file
        bucket - the destination bucket
        minio_path - the destination path
        print_errs - flag to indicate errors should be reported
    """
    basename = os.path.basename(cyverse_path)
    if not get_cyverse_file(cyverse_path, print_errs):
        if print_errs:
            write_error(f"Not able to find downloaded file {basename}")
        return False

    with open(basename, "r", encoding="utf-8") as in_file:
        meta_json = json.load(in_file)
    os.unlink(basename)

    modified = False
    if "bucket" not in meta_json:
        meta_json["bucket"] = bucket
        modified = True

    if "uploadIRODSPath" in meta_json:
        del meta_json["uploadIRODSPath"]
        meta_json["uploadPath"] = os.path.dirname(minio_path)
        modified = True

    if modified:
        return upload_json(meta_json, minio, bucket, minio_path)

    upload_local_files([basename], minio, bucket, os.path.dirname(minio_path))
    return True


def upload_new_collection_json(minio: Minio, bucket: str, minio_path: str, coll_id: str) -> bool:
    """Creates a new collection file to upload
    Arguments:
        minio - the MinIO client instance to use
        bucket - the bucket to upload to
        minio_path - path of file on server
        coll_id - the ID of the collection
    """
    coll_json = {
        "bucketProperty": bucket,
        "nameProperty": "",
        "organizationProperty": "",
        "contactInfoProperty": "",
        "descriptionProperty": f"Collection ID#\n{coll_id}\n",
        "idProperty": coll_id
        }

    return upload_json(coll_json, minio, bucket, minio_path)


def upload_empty_json(minio: Minio, bucket: str, minio_path: str) -> bool:
    """Uploads empty JSON to MinIO
    Arguments:
        minio - the MinIO client instance to use
        bucket - the bucket to upload to
        minio_path - path of file on server
    """
    return upload_json({}, minio, bucket, minio_path)


def upload_json(the_json: dict, minio: Minio, bucket: str, minio_path: str) -> bool:
    """Uploads the JSON as a MinIO object
    Arguments:
        the_json - the JSON object to load (as a dict)
        minio - the MinIO client instance to use
        bucket - the bucket to upload to
        minio_path - path of file on server
    """
    # Upload the JSON
    data = io.BytesIO(str.encode(json.dumps(the_json, indent=4)))
    minio.put_object(bucket, minio_path, data, len(data.getvalue()),
                     content_type="application/json")

    return True


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


def image_found_camtrap(image_path: str, camtrap: dict) -> bool:
    """Looks for an image's entry in CamTrap data
    Arguments:
        image_path - the path to check
        camtrap - the camtrap data to look through
    """
    for one_line in camtrap[CAMTRAP_MEDIA]:
        if one_line.startswith(image_path):
            return True

    return False


def add_camtrap_deployment(camtrap: dict, collection_id: str, image_path: str, image_data: dict) -> str:
    # pylint: disable=unused-argument
    """Adds the image data for camtrap deployment as a single CSV line
    Arguments:
        camtrap - the CamTrap data
        collection_id - the collection ID
        image_path - the path of the image's data
        image_data - the image's data
    Return:
        The formatted CSV entry
    """
    cur_depl = camtrap[CAMTRAP_DEPLOYMENT]
    depl_id = get_deployment_id(collection_id, image_data)

    found_depl = None
    for one_depl in cur_depl:
        if one_depl.startswith(depl_id):
            found_depl = one_depl
            break

    if not found_depl:
        found_depl = ','.join((depl_id,
                             image_data["locationID"],
                             image_data["locationName"],
                             image_data["locationLongitude"],
                             image_data["locationLatitude"],
                             "0",# coordinateUncertainty
                             "", # deployment start
                             "", # deployment end
                             "", # setup by
                             "", # camera ID
                             "", # camera model
                             "0",# camera interval
                             image_data["locationElevation"],
                             "0.0",# camera tilt
                             "0",# camera heading
                             "false", # timestamp issues
                             "", # bait use
                             "", # session
                             "", # array
                             "", # feature type
                             "", # habitat
                             "", # tags
                             ""  # notes
                             ))
        cur_depl.append(found_depl)
        camtrap[CAMTRAP_DEPLOYMENT] = cur_depl
        camtrap["modified"] = True

    return found_depl


def add_camtrap_media(camtrap: dict, collection_id: str, image_path: str, image_data: dict) -> str:
    """Adds the image data for camtrap media as a single CSV line
    Arguments:
        camtrap - the CamTrap data
        collection_id - the collection ID
        image_path - the path of the image's data
        image_data - the image's data
    Return:
        The formatted CSV entry
    """
    cur_media = camtrap[CAMTRAP_MEDIA]
    depl_id = get_deployment_id(collection_id, image_data)

    found_media = None
    for one_media in cur_media:
        if one_media.startswith(image_path):
            found_media = one_media
            break

    if not found_media:
        found_media = ','.join((
                image_path, # media ID
                depl_id,
                image_path, # sequence ID
                "",         # capture method
                "",         # media timestamp
                image_path, # file path
                os.path.basename(image_path),
                "image/jpg",
                "",         # exif data
                "false",    # favorite
                ""          # comments
                ))
        cur_media.append(found_media)
        camtrap[CAMTRAP_MEDIA] = cur_media
        camtrap["modified"] = True

    return found_media


def add_camtrap_observation(camtrap: dict, collection_id: str, image_path: str, image_data: dict) -> str:
    """Adds the image data for camtrap observation as a single CSV line
    Arguments:
        camtrap - the CamTrap data
        collection_id - the collection ID
        image_path - the path of the image's data
        image_data - the image's data
    Return:
        The formatted CSV entry
    """
    cur_obs = camtrap[CAMTRAP_OBSERVATIONS]
    depl_id = get_deployment_id(collection_id, image_data)

    found_obs = None
    for one_obs in cur_obs:
        if image_path in one_obs:
            found_obs = one_obs
            break

    if not found_obs:
        if "dateTimeTaken" in image_data:
            timestamp = datetime.datetime.fromtimestamp(int(image_data["dateTimeTaken"])/1000.0)
        else:
            timestamp = datetime.datetime(int(image_data["dateYearTaken"]), 1, 1) + \
                        datetime.timedelta(int(image_data["dateDayOfYearTaken"]))
            if "dateHourTaken" in image_data:
                timestamp = timestamp.replace(hour=int(image_data["dateHourTaken"]))

        if "metaSpeciesCommonName" in image_data:
            common_name = image_data["metaSpeciesCommonName"]
            common_name_comment = f"[COMMONNAME:{common_name}]"
        else:
            common_name_comment = ""

        if "speciesScientificName" in image_data:
            scientific_name = image_data["speciesScientificName"]
        else:
            scientific_name = ""
        found_obs = ",".join((
            "",         # observation ID
            depl_id,
            "",         # sequence ID
            image_path,
            timestamp.isoformat(),
            "",         # observation type
            "false",    # camera setup
            "",         # taxon ID
            scientific_name,
            image_data["metaSpeciesCount"] if "metaSpeciesCount" in image_data else "0",
            "0",        # count new
            "",         # life stage
            "",         # sex
            "",         # behavior
            "",         # individual ID
            "",         # classification method
            "",         # classified by
            "",         # classification timestamp
            "1.0",      # classification confidence
            common_name_comment
        ))
        cur_obs.append(found_obs)
        camtrap[CAMTRAP_OBSERVATIONS] = cur_obs
        camtrap["modified"] = True

    return found_obs


def update_camtrap(camtrap: dict, collection_id: str, minio_path: str, image_md: tuple) -> None:
    """Updates the camtrap data with the image's data
    Arguments:
        camtrap - the CamTrap data
        collection_id - the collection ID
        minio_path - the path to the image on MinIO
        image_md - the metadata associated with the image
    """
    # Reformat the image data to be more easily accessible
    image_data = {}
    for one_md in image_md:
        image_data[one_md["Attribute"]] = one_md["Value"]

    add_camtrap_deployment(camtrap, collection_id, minio_path, image_data)
    add_camtrap_media(camtrap, collection_id, minio_path, image_data)
    add_camtrap_observation(camtrap, collection_id, minio_path, image_data)


def update_camtrap_unprocessed(camtrap: dict, collection_id: str, image_match_path: str,
                               minio_path: str, unprocessed_csv: tuple) -> bool:
    """Attempts to update the image's metadata from unprocessed CyVerse CSV files
    Arguments:
        camtrap - the CamTrap data
        collection_id - the collection ID
        image_match_path - the image path to match
        minio_path - the path to the image on MinIO
        unprocessed_csv - tuple containing found unprocessed image metadata
    Return:
        True is returned if the image's CamTrap data was updated, and False otherwise
    """
    found_line = None
    for one_line in unprocessed_csv:
        if one_line.find(image_match_path) >= 0:
            found_line = one_line
            break

    # If not found, return
    if not found_line:
        return False

    # Process the CSV file into a form we can work with
    parts = found_line.split(',')
    idx = 1
    image_data = {}
    while idx < len(parts):
        image_data[parts[idx]] = parts[idx + 1]
        idx += 3

    add_camtrap_deployment(camtrap, collection_id, minio_path, image_data)
    add_camtrap_media(camtrap, collection_id, minio_path, image_data)
    add_camtrap_observation(camtrap, collection_id, minio_path, image_data)

    return True


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


def fix_camtrap(camtrap: dict, replacements: dict = None) -> dict:
    """Fixes previous problems with generated CamTrap data
    Arguments:
        camtrap - the camtrap data to fix
        replacements - a dictionary of keys to be replaced with the associated values
    Comments:
        For a replacement of {'a': 'b'}, the column value matching the string 'a' will be replaced
        with the string 'b'
    """
    changed = False

    def replace_string(haystack: str, needle: str, new_value: str) -> str:
        """Replaces all column values exactly matching needle found in the haystack with the new value
        Arguments:
            haystack - the CSV string to check for a needle
            needle - the column value to match exactly
            new_value - the value to replace needle with
        """
        nonlocal changed
        cur_needles = {f"{needle}:": f"{new_value}:",
                       f"\"{needle}:": f"\"{new_value}:"}
        cur_haystack = haystack
        for cur_needle, cur_value in cur_needles.items():
            if cur_haystack.startswith(cur_needle):
                changed = True
                cur_haystack = cur_value + cur_haystack[len(cur_needle):]
        cur_needles = {f",{needle}:": f",{new_value}:",
                       f",\"{needle}:": f",\"{new_value}:",
                       f",\"{needle}\"": f",\"{new_value}\"",
                       }
        for cur_needle, cur_value in cur_needles.items():
            print(f"HACK:       |{cur_needle}| -> |{cur_value}|")
            while cur_haystack.find(cur_needle) >= 0:
                print("HACK:       FOUND")
                changed = True
                cur_haystack = cur_haystack.replace(cur_needle, cur_value)
                print(cur_haystack)
        cur_needles = {f",{needle}": f",{new_value}",
                       f",\"{needle}": f",\"{new_value}",
                       f",\"{needle}\"": f",\"{new_value}\""}
        for cur_needle, cur_value in cur_needles.items():
            if cur_haystack.endswith(cur_needle):
                changed = True
                cur_haystack = cur_haystack[:len(cur_haystack) - len(cur_needle)] + cur_value

        return cur_haystack

    def find_replace_string(haystack: str, find_str: str, needle: str, new_value: str) -> str:
        """If the string to find is found anywhere in the haystack, replaces all column values
           exactly matching needle found in the haystack with the new value
        Arguments:
            haystack - the CSV string to check for a needle
            find_str - the string to search for
            needle - the column value to match exactly
            new_value - the value to replace needle with
        """
        if haystack.find(find_str) >= 0:
            print(f"HACK: find_replace_string: FOUND STRING {find_str}")
            print(haystack)
            return replace_string(haystack, needle, new_value)

        return haystack

    updated_deployment = camtrap[CAMTRAP_DEPLOYMENT]
    updated_media = camtrap[CAMTRAP_MEDIA]
    updated_observations = camtrap[CAMTRAP_OBSERVATIONS]

    print("     ... looking for replacements", flush=True)
    if replacements:
        for one_needle in replacements:
            repl_value = replacements[one_needle]
            if isinstance(repl_value, dict):
                print("HACK: find " + one_needle + ": " + repl_value["key"] + " -> " + repl_value["value"])
                updated_deployment = [find_replace_string(dep, one_needle, repl_value["key"], repl_value["value"]) for dep in updated_deployment]
                updated_media = [find_replace_string(med, one_needle, repl_value["key"], repl_value["value"]) for med in updated_media]
                updated_observations = [find_replace_string(obs, one_needle, repl_value["key"], repl_value["value"]) for obs in updated_observations]
            else:
                updated_deployment = [replace_string(dep, one_needle, repl_value) for dep in updated_deployment]
                updated_media = [replace_string(med, one_needle, repl_value) for med in updated_media]
                updated_observations = [replace_string(obs, one_needle, repl_value) for obs in updated_observations]

    if changed:
        print("       ... made changes to camtrap data", flush=True)
        camtrap[CAMTRAP_DEPLOYMENT] = updated_deployment
        camtrap[CAMTRAP_MEDIA] = updated_media
        camtrap[CAMTRAP_OBSERVATIONS] = updated_observations
        camtrap["modified"] = True

    return camtrap


def get_cyverse_meta_csv(cyverse_path: str, print_errs: bool = True) -> tuple:
    """Downloads meta CSV files from CyVerse and loads the contents
    Arguments:
        cyverse_path - the folder to look for CSV files
        print_errs - flag to indicate errors should be reported
    Return:
        A tuple of any loaded CSV files with one string per CSV file row (unprocessed)
    """
    loaded_csv = []

    cmd = ['ils', cyverse_path]
    res = subprocess.run(cmd, capture_output=True, check=False, text=True)
    if res.returncode != 0:
        if print_errs:
            print(res, flush=True)
        return tuple(loaded_csv)

    for one_line in res.stdout.split("\n"):
        one_line = one_line.rstrip().lstrip()
        if one_line.startswith("meta-") and one_line.endswith(".csv"):
            meta_filename = one_line.strip("\n")
            meta_path = os.path.join(cyverse_path, meta_filename)
            if get_cyverse_file(meta_path, print_errs):
                with open(meta_filename, "r", encoding="utf-8") as infile:
                    cur_csv = [line.rstrip() for line in infile]
                    loaded_csv = loaded_csv + cur_csv
                os.unlink(meta_filename)

    return tuple(loaded_csv)


def fix_image_camtrap(camtrap: dict, image_data: dict) -> bool:
    """Fixes issues with CamTrap image-based data
    Arguments:
        camtrap - the camtrap data to append the image to
        image_data - dict containing the information on the image
    """

    image_rel_path = image_data["RelativePath"]

    image_md = image_data["Metadata"]
    image_attr = {}
    for one_md in image_md:
        image_attr[one_md["Attribute"]] = one_md["Value"]

    cur_obs = camtrap[CAMTRAP_OBSERVATIONS]
    for idx in range(0, len(cur_obs)):
        one_obs = cur_obs[idx]
        if one_obs.find(image_rel_path) >= 0 and \
                "metaSpeciesCommonName" in image_attr and "speciesScientificName" in image_attr:
            check_name = ',"' + image_attr["metaSpeciesCommonName"] + '",'
            replace_name = ',"' + image_attr["speciesScientificName"] + '",'
            if one_obs.find(check_name) >= 0:
                new_obs = one_obs.replace(check_name, replace_name, 1)
                cur_obs[idx] = new_obs
                camtrap[CAMTRAP_OBSERVATIONS] = cur_obs
                camtrap["modified"] = True
                return True

            check_name = ',' + image_attr["metaSpeciesCommonName"] + ','
            replace_name = ',' + image_attr["speciesScientificName"] + ','
            if one_obs.find(check_name) >= 0:
                new_obs = one_obs.replace(check_name, replace_name, 1)
                cur_obs[idx] = new_obs
                camtrap[CAMTRAP_OBSERVATIONS] = cur_obs
                camtrap["modified"] = True
                return True

            return False

    return False


def check_upload_tar_files(minio: Minio, cyverse_upload_path: str, upload_name: str, bucket: str,
                           minio_basepath: str, collection_id: str, camtrap: dict,
                           work_dir: str, print_errs: bool = True) -> None:
    """Checks CyVerse for matching upload TAR files and tries to resolve discrepancies
    Arguments:
        minio - the MinIO client instance to use
        cyverse_upload_path - the path on CyVerse to the collection's Upload folder
        bucket - the MinIO bucket to upload the images/data to
        minio_basepath - the base path to upload the images/data to
        collection_id - the ID of the collection
        camtrap - the camtrap data to append the image to
        work_dir - the working folder
        print_errs - flag to indicate errors should be reported
    """
    cmd = ['ils', cyverse_upload_path]
    res = subprocess.run(cmd, capture_output=True, check=False, text=True)
    if res.returncode != 0:
        if print_errs:
            print(res, flush=True)
        return

    # Get a temporary folder to work within
    tar_dir = tempfile.mkdtemp(prefix="tar_", dir=work_dir)
    cur_dir = os.getcwd()

    # Define a cleanup function
    def cleanup_files() -> None:
        """Embedded function to clean up files
        """
        for cur_file in os.listdir(tar_dir):
            file_path = os.path.join(tar_dir, cur_file)
            try:
                if os.path.isfile(file_path) or os.path.islink(file_path):
                    os.unlink(file_path)
                elif os.path.isdir(file_path):
                    shutil.rmtree(file_path)
            except Exception as ex:
                print('Failed to delete %s. Reason: %s' % (file_path, ex))

    # Process TAR files (if there are any relevant ones)
    os.chdir(tar_dir)
    for one_line in res.stdout.split("\n"):
        one_line = one_line.rstrip().lstrip()
        print(f"HACK: CHECKING TAR NAME {one_line}", flush=True)
        if one_line.startswith(upload_name + "-") and one_line.endswith(".tar"):
            cyverse_tar_path = os.path.join(cyverse_upload_path, one_line)
            print(f"HACK: GETTING TAR FROM {cyverse_tar_path}", flush=True)
            if get_cyverse_file(cyverse_tar_path, print_errs):
                print(f"HACK: LOCAL TAR FILE \"{one_line}\"", flush=True)
                print("   cwd: " + os.getcwd(), flush=True)
                for x in os.listdir(os.getcwd()):
                    print(f"      {x}", flush=True)
                cmd = ['tar', "-xf", f"{one_line}"]
                print(cmd, flush=True)
                res = subprocess.run(cmd, capture_output=True, check=False, text=True)
                if res.returncode != 0:
                    if print_errs:
                        print(res, flush=True)
                    cleanup_files()
                    continue

                # Loop through looking for CSV files and image folders
                loaded_csv = []
                upload_folders = []
                for one_file in os.listdir(tar_dir):
                    if one_file.startswith("meta-") and one_file.endswith(".csv"):
                        csv_filename = os.path.join(tar_dir, one_file)
                        with open(csv_filename, "r", encoding="utf-8") as infile:
                            cur_csv = [line.rstrip() for line in infile]
                            loaded_csv = loaded_csv + cur_csv
                    elif os.path.isdir(one_file):
                        upload_folders.append(one_file)
                if not loaded_csv:
                    print(f"ERROR: NO CSV FILES IN TAR : {cyverse_tar_path}", flush=True)
                    cleanup_files()
                    continue
                if not upload_folders:
                    print(f"ERROR: NO IMAGE FOLDER IN TAR : {cyverse_tar_path}", flush=True)
                    cleanup_files()
                    continue

                # Loop through folders and images and upload any missing (that have metadata)
                for one_folder in upload_folders:
                    images_dir = os.path.join(tar_dir, one_folder)
                    for one_file in os.listdir(images_dir):
                        image_file = os.path.join(images_dir, one_file)
                        print(f"HACK: FOUND IMAGE FILE: {image_file}", flush=True)
                        if os.path.isdir(image_file):
                            subpath = os.path.join(one_folder, one_file)
                            print(f"     ... Actually is a DIR - adding to dir list {subpath}")
                            upload_folders.append(subpath)
                            continue

                        found_meta = None
                        meta_check = os.path.join(one_folder, one_file)
                        for csv_line in loaded_csv:
                            if csv_line.startswith(meta_check):
                                found_meta = csv_line
                                break
                        if not found_meta:
                            print(f"ERROR: NO METADATA {meta_check}", flush=True)
                            continue
                        print("     ... FOUND METADATA", flush=True)

                        parts = found_meta.split(',')
                        idx = 1
                        image_data = {}
                        while idx < len(parts):
                            image_data[parts[idx]] = parts[idx + 1]
                            idx += 3

                        # Check if the image is already on MinIO, continue if it is
                        minio_path = os.path.join(minio_basepath, one_folder, one_file)
                        print(f"    MINIO PATH: {minio_path}", flush=True)
                        if exists_minio(minio, bucket, minio_path):
                            print("     ... already exists on minio", flush=True)
                            # Check if the metadata can be found for the image and add it if not
                            if not image_found_camtrap(minio_path, camtrap):
                                print(f"     METADATA NOT FOUND - ADDING NOW '{collection_id}'", flush=True)
                                add_camtrap_deployment(camtrap, collection_id, minio_path, image_data)
                                add_camtrap_media(camtrap, collection_id, minio_path, image_data)
                                add_camtrap_observation(camtrap, collection_id, minio_path, image_data)
                            continue

                        # Upload the image and update CamTrap data
                        print("       NEED TO UPLOAD AND UPDATE", flush=True)
                        print(f"    '{image_file}' to '{minio_path}'", flush=True)

                        minio.fput_object(bucket, minio_path, image_file, content_type="image/jpeg")

                        add_camtrap_deployment(camtrap, collection_id, minio_path, image_data)
                        add_camtrap_media(camtrap, collection_id, minio_path, image_data)
                        add_camtrap_observation(camtrap, collection_id, minio_path, image_data)

            cleanup_files()

    os.chdir(cur_dir)
    shutil.rmtree(tar_dir)

    print("HACK: DONE", flush=True)


def upload_update_image(minio: Minio, cyverse_basepath: str, bucket: str, minio_basepath: str,
                        image_data: dict, camtrap: dict, minio_id: str, unprocessed_csv: tuple,
                        track: ImageTrack) -> bool:
    # pylint: disable=too-many-arguments
    """Upload the image to minio and update the camtrap data
    Arguments:
        minio - the MinIO client instance to use
        cyverse_basepath - the path on CyVerse to the collection
        bucket - the bucket to upload the image to
        minio_basepath - the base path to upload the images to
        image_data - dict containing the information on the image
        camtrap - the camtrap data to append the image to
        minio_id - the MinIO ID associatd with this upload
        unprocessed_csv - unprocessed metadata found on CyVerse
        track - used to track image upload progress
    """
    metadata_valid = "Metadata" in image_data and len(image_data["Metadata"]) > 0

    # Don't overwrite data
    relative_path = image_data["RelativePath"]
    minio_path = os.path.join(minio_basepath, relative_path)
    if image_found_camtrap(minio_path, camtrap):
        print(f"'{relative_path}' was found in CamTrap data", flush=True)
        track.add_progress("Found in CamTrap data - not uploading")
        return False
    if exists_minio(minio, bucket, minio_path):
        print(f"'{relative_path}' was found on destination (but not CamTrap data - adding data)", flush=True)
        if metadata_valid:
            update_camtrap(camtrap, minio_id, minio_path, image_data["Metadata"])
            track.add_progress("Already on MinIO - not uploading image")
        elif update_camtrap_unprocessed(camtrap, minio_id, relative_path, minio_path, unprocessed_csv):
            track.add_progress("Updated image metadata from unprocessed CyVerse CSV")
        else:
            print("  ... Missing or invalid metadata - CamTrap data not updated", flush=True)
            track.add_progress("Error: Missing or invalid image metadata")
        return False

    print(f"'{relative_path}' upload", flush=True)
    cyverse_path = os.path.join(cyverse_basepath, relative_path)
    track.add_progress("Transferring from CyVerse to MinIO")
    track.add_progress(f"  from '{cyverse_path}' to '{bucket}:{minio_path}'")
    if not transfer_file(minio, cyverse_path, bucket, minio_path, True,
                         content_type="image/jpeg"):
        write_error(f"Unable to transfer image from '{cyverse_path}' to '{minio_path}'")
        track.add_progress("Error: Transfer failed")
    else:
        track.add_progress("Image transferred")

    if metadata_valid:
        track.add_progress("Updating CamTrap data")
        update_camtrap(camtrap, minio_id, minio_path, image_data["Metadata"])
        track.add_progress("Image transferred and CamTrap data updated")
    else:
        track.add_progress("Error: Missing metadata, only image transferred")
    return True


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
    dest_uploads_folder = os.path.join(dest_coll_base, "Uploads")
    print(f"HACK: checking MinIO path '{dest_uploads_folder}'")
    for one_result in minio.list_objects(dest_bucket, dest_uploads_folder + "/"):
        if not one_result.is_dir:
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
        camtrap = fix_camtrap(camtrap,
                          {"[COMMONNAME:Raptor]": {"key": "Unknown", "value": "Falconiformes"},
                           "[COMMONNAME:Snake]": {"key": "Unknown", "value": "Serpentes"},
                           "[COMMONNAME:snake]": {"key": "Unknown", "value": "Serpentes"},
                          })
        camtrap = fix_camtrap(camtrap,
                          {"[COMMONNAME:Snake]": {"key": "Falconiformes", "value": "Serpentes"},
                           "[COMMONNAME:snake]": {"key": "Falconiformes", "value": "Serpentes"},
                          })

        # Write the CamTrap data and upload the CSV files
        if camtrap["modified"]:
            print(" ... uploading camtrap files", flush=True)
            write_camtrap(camtrap, work_dir)
            upload_local_files([os.path.join(work_dir, fn) for fn in MINIO_CAMTRAP_FILES], minio,
                           dest_bucket, dest_uploads_base, content_type="text/csv")
        else:
            print(" ... camtrap files not modified - skipping upload", flush=True)

        # Clean up the temporary folder
        print(f" ... removing working folder {work_dir}", flush=True)
        shutil.rmtree(work_dir)


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


def process_camtrap(minio_id: str, user: str, pw: str) -> None:
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
    print(f"    ID: {minio_id}", flush=True)

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
    process_camtrap(m_minio_id, m_user, m_pw)
