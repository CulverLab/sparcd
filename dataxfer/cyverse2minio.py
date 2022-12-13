#!/usr/bin/env python3
"""Python script for transferring data from CyVerse to MinIO
"""

import argparse
import io
import json
from minio import Minio
import os
import shutil
import subprocess
import sys
import tempfile
import uuid

# The endpoint to upload to
MINIO_ENDPOINT = "https://admin-sandbox.sparcd.arizona.edu/"
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

def get_params() -> tuple:
    """Get the paths to the files
    Returns:
        A tuple containing the two paths
    """
    parser = argparse.ArgumentParser(description='Moves files from CyVerse to MinIO')
    parser.description = f'Moves SPARCd data from CyVerse to MinIO ({MINIO_ENDPOINT})'

    parser.add_argument('-user', required=True, help='Username for minio endpoint');
    parser.add_argument('-pw', required=True, help='Password for minio endpoint');
    parser.add_argument('json_file', type=argparse.FileType('r'), help='The json file with CyVerse entries')
    parser.add_argument('mapping_id', type=str, required=False, help='MinIO collection ID to upload to (optional)')

    args = parser.parse_args()

    return args.json_file.name, args.mapping_id, args.user, args.pw


def write_error(msg: str) -> None:
    """Writes error messages
    Arguments:
        msg - the message to write
    """
    print(msg, file=sys.stderr)


def transfer_file(minio: Minio, cyverse_path: str, bucket: str, minio_path: str, print_errs: bool = False) -> bool:
    """Attempts to transfer a file from CyVerse to MinIO
    Arguments:
        minio - the MinIO client instance to use
        cyverse_path - the CyVerse path to the file
        bucket - the destination bucket
        minio_path - the destination path
        print_errs - flag to indicate errors should be reported
    """
    basename = os.path.basename(cyverse_path)

    cmd = ['iget', '-K', f"'{cyverse_path}'"]
    res = subprocess.run(cmd, capture_output=True)
    if res.returncode != 0:
        if print_errs:
            write_error(f"Failed to retrieve file '{cyverse_path}")
        return False

    if not os.path.exists(basename):
        if print_errs:
            write_error(f"Not able to find downloaded file {basename}")
        return False

    minio.fput_object(bucket, minio_path, basename)
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
        "descriptionProperty": f"Collection ID#\n{coll_id}}\n",
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
    minio.put_object(bucket, minio_path, io.BytesIO(str.encode(json.dumps(the_json, indent=4))))

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
        minio.fget_object(bucket, one_file, dest_file)
        if os.path.exists(dest_file):
            success = success + 1

    return success == len(files)


def upload_local_files(files: list, minio: Minio, bucket: str, dest_basepath: str) -> None:
    """Uploads files to MinIO
    Arguments:
        files - the list of local files to load
        minio - the MinIO client instance to use
        bucket - the bucket to upload to
        dest_basepath - the base path to load files to
    Notes:
        The files are loaded in a flat hierarchy regardless of how they are stored locally
    """
    for one_file in files:
        dest_path = os.path.join(dest_basepath, os.path.basename(one_file))
        minio.fput_object(bucket, dest_path, one_file)


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


def format_camtrap_deployment(depl: list, image_path: str, image_data: dict) -> str:
    """Formats the image data for camtrap deployment as a single CSV line
    Arguments:
        depl - the list of current deployments
        image_path - the path of the image's data
        image_data - the image's data
    Return:
        The formatted CSV entry
    """


def format_camtrap_media(media: list, image_path: str, image_data: dict) -> str:
    """Formats the image data for camtrap media as a single CSV line
    Arguments:
        media - the current list of media
        image_path - the path of the image's data
        image_data - the image's data
    Return:
        The formatted CSV entry
    """


def format_camtrap_observation(obs: list, image_path: str, image_data: dict) -> str:
    """Formats the image data for camtrap observation as a single CSV line
    Arguments:
        obs - the current list of observations
        image_path - the path of the image's data
        image_data - the image's data
    Return:
        The formatted CSV entry
    """


def update_camtrap(camtrap: dict, minio_path: str, image_data: dict) -> None:
    """Updates the camtrap data with the image's data
    Arguments:
        camtrap - the CamTrap data
        minio_path - the path to the image on MinIO
        image_data - the data associated with the image
    """
    depl = format_camtrap_deployment(camtrap[CAMTRAP_DEPLOYMENT], minio_path, image_data)
    media = format_camtrap_media(camtrap[CAMTRAP_MEDIA], minio_path, image_data)
    obs = format_camtrap_observation(camtrap[CAMTRAP_OBSERVATIONS], minio_path, image_data)


def load_camtrap(folder: str) -> dict:
    """Loads camtrap data from local folder
    Arguments:
        folder - the path to the camtrp files
    Return:
        A dict containing the deployments, media, observations string lists (each line is not csv separated)
    """
    depl = None
    media = None
    obs = None

    cur_path = os.path.join(folder, DEPLOYMENT_CSV)
    if os.path.exists(cur_path):
        with open(cur_path, "r") as in_file:
            depl = in_file.readlines()
    else:
        depl = []

    cur_path = os.path.join(folder, MEDIA_CSV)
    if os.path.exists(cur_path):
        with open(cur_path, "r") as in_file:
            media = in_file.readlines()
    else:
        media = []

    cur_path = os.path.join(folder, OBSERVATIONS_CSV)
    if os.path.exists(cur_path):
        with open(cur_path, "r") as in_file:
            obs = in_file.readlines()
    else:
        obs = []

    return {CAMTRAP_DEPLOYMENT: depl, CAMTRAP_MEDIA: media, CAMTRAP_OBSERVATIONS: obs}


def write_camtrap(camtrap: dict, folder: str) -> None:
    """Writes CamTrap data to the specified folder
    Arguments:
        camtrap - the camtrap dict to write
        folder - the folder to write
    """
    with open(os.path.join(folder, DEPLOYMENT_CSV), "w") as out_file:
        out_file.writelines(camtrap[CAMTRAP_DEPLOYMENT])
    with open(os.path.join(folder, MEDIA_CSV), "w") as out_file:
        out_file.writelines(camtrap[CAMTRAP_MEDIA])
    with open(os.path.join(folder, OBSERVATIONS_CSV), "w") as out_file:
        out_file.writelines(camtrap[CAMTRAP_OBSERVATIONS])


def upload_update_image(minio: Minio, cyverse_basepath: str, bucket: str, minio_basepath: str,
                        image_data: dict, camtrap: dict) -> bool:
    """Upload the image to minio and update the camtrap data
    Arguments:
        minio - the MinIO client instance to use
        cyverse_basepath - the path on CyVerse to the collection
        bucket - the bucket to upload the image to
        minio_basepath - the base path to upload the images to
        image_data - dict containing the information on the image
        camtrap - the camtrap data to append the image to
    """
    # Don't overwrite data
    relative_path = image_data["RelativePath"]
    minio_path = os.path.join(minio_basepath, relative_path)
    if image_found_camtrap(minio_path, camtrap):
        write_error(f"Image '{relative_path}' was found in CamTrap data")
        return False
    if minio.stat_object(bucket, minio_path):
        write_error(f"Image '{relative_path}' was found on destination (but not in CamTrap data")
        return False

    cyverse_path = os.path.join(cyverse_basepath, relative_path)
    if not transfer_file(minio, cyverse_path, bucket, minio_path, True):
        pass

    update_camtrap(camtrap, minio_path, image_data)
    return True


def move_images(minio: Minio, cyverse_basepath: str, coll_subpath: str, minio_id: str, images: list) -> None:
    """Moves images from CyVerse to MinIO
    Arguments:
        minio - the MinIO client instance to use
        cyverse_basepath - the path on CyVerse to the collection
        coll_subpath - the upload subpath for images
        minio_id - the collection ID on MinIO to upload to
        images - the list of images to upload
    """
    dest_bucket = "sparcd-" + minio_id
    dest_coll_base = os.path.join("Collections", minio_id)

    # Check if the collection already exists and create it if not
    if not minio.bucket_exists(dest_bucket):
        minio.make_bucket(dest_bucket)

    # Upload collection.json and permissions.json
    if not transfer_file(minio, os.path.join(cyverse_basepath, COLL_COLLECTION_FILE),
                         dest_bucket, os.path.join(dest_coll_base, COLL_COLLECTION_FILE)):
        if not upload_new_collection_json(minio, dest_bucket, os.path.join(dest_coll_base, COLL_COLLECTION_FILE),
                                          minio_id):
            write_error(f"Unable to upload new {COLL_COLLECTION_FILE} to {minio_id}")
    if not transfer_file(minio, os.path.join(cyverse_basepath, COLL_PERMISSIONS_FILE),
                         dest_bucket, os.path.join(dest_coll_base, COLL_PERMISSIONS_FILE)):
        if not upload_empty_json(minio, dest_bucket, os.path.join(dest_coll_base, COLL_PERMISSIONS_FILE)):
            write_error(f"Unable to upload new {COLL_PERMISSIONS_FILE} to {minio_id}")

    # Create valid MinIO subpath under Uploads folder
    dest_uploads_base = os.path.join(dest_coll_base, "Uploads",
                                     coll_subpath.replace(' ', '.', 1).replace(' ', '_').replace('-', '.'))

    # Get UploadMeta.json from CyVerse and upload to MinIO
    if not transfer_file(minio, cyverse_path, dest_bucket, os.path.join(dest_uploads_base, COLL_UPLOADMETA_FILE)):
        write_error(f"{COLL_UPLOADMETA_FILE} not transferred from {cyverse_path}")

    # Get a temporary folder to work within
    work_dir = tempfile.mkdtemp(prefix="dsparcd_")

    # Get the deployments.csv, media.csv, and observations.csv files, and load them
    # TODO: use the difference between a new collection and an empty collection to detect errors
    download_minio_files(minio, dest_bucket,
                         [os.path.join(dest_uploads_base, fn) for fn in MINIO_CAMTRAP_FILES], work_dir)
    camtrap = load_camtrap(work_dir)

    # Upload each of the images and update CSV data
    for one_image in images:
        upload_update_image(one_image, camtrap)

    # Write the CamTrap data and upload the CSV files
    write_camtrap(camtrap, work_dir)
    upload_local_files([os.path.join(work_dir, fn) for fn in MINIO_CAMTRAP_FILES], minio, dest_bucket,
                       dest_uploads_base)

    # Clean up the temporary folder
    shutil.rmtree(work_dir)


def move_data(json_file: str, minio_id: str, user: str, pw: str) -> None:
    """Moves the data from CyVerse to MinIO
    Arguments:
        json_file - name of the CyVerse exported JSON file containing entries to pull from
        minio_id - (optional) corresponding MinIO collection ID to upload to
        user - username for MinIO endpoint
        pw - password for MinIO endpoint
    Notes:
        If a MinIO ID is not specified, one will be generated and the CyVerse entries uploaded to that collection
    """

    # Load the JSON source file
    with open(json_file, "r") as in_file:
        from_json = json.load(in_file)

    if UPLOADS_KEYWORD not in from_json:
        write_error(f"JSON file is missing keyword '{UPLOADS_KEYWORD}' - make sure this is a valid file")
        return

    # Create the MinIO Client instance
    minio = Minio(MINIO_ENDPOINT, access_key=user, secret_key=pw)

    # Get the source collection ID
    cyverse_id = os.path.basename(from_json[BASEPATH_KEYWORD])

    # Determine what MinIO ID we should be using
    if not minio_id:
        use_minio_id = str(uuid.uuid4())
    else:
        use_minio_id = minio_id

    # Print out what we're doing
    print(f"Processing file '{json_file}'")
    print(f"    MinIO endpoint is {MINIO_ENDPOINT}")
    print(f"    CyVerse collection {cyverse_id} to {use_minio_id}" + (" (new)" if use_minio_id != minio_id else ""))
    print(f"    Processing " + str(len(from_json[UPLOADS_KEYWORD])) + " entries")

    # Loop through the entries
    for one_entry in from_json[UPLOADS_KEYWORD]:
        move_images(minio, from_json[BASEPATH_KEYWORD], one_entry[COLL_UPLOAD_KEYWORD], use_minio_id,
                    one_entry[COLL_IMAGES_KEYWORD])


if __name__ == '__main__':
    m_json_file, m_minio_id, m_user, m_pw = get_params()
    move_data(m_json_file, m_minio_id, m_user, m_pw)
