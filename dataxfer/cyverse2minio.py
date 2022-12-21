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
MINIO_ENDPOINT = "sandbox.sparcd.arizona.edu:443"
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
    parser.add_argument('json_file', type=argparse.FileType('r'),
                        help='The json file with CyVerse entries')
    parser.add_argument('uuid_id', type=str,
                        help='MinIO collection UUID to upload to (use - to skip)')

    parser.epilog = "Assumes that iRODS commands have been installed and configured"

    args = parser.parse_args()

    return args.json_file.name, \
           args.uuid_id if args.uuid_id != "-" else None, \
           args.user, \
           args.pw


def get_deployment_id(image_data: dict) -> str:
    """Returns the deployment ID for this image
    Arguments:
        image_data - the image's data
    """
    return image_data["collectionID"] + ':' + image_data["locationID"]


def write_error(msg: str) -> None:
    """Writes error messages
    Arguments:
        msg - the message to write
    """
    print(msg, file=sys.stderr)


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
            print(res)
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

    coll_json["idProperty"] = minio_id
    if "bucketProperty" not in coll_json:
        coll_json["bucketProperty"] = bucket
        return upload_json(coll_json, minio, bucket, minio_path)

    upload_local_files([basename], minio, bucket, os.path.dirname(minio_path))
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


def add_camtrap_deployment(camtrap: dict, image_path: str, image_data: dict) -> str:
    # pylint: disable=unused-argument
    """Adds the image data for camtrap deployment as a single CSV line
    Arguments:
        camtrap - the CamTrap data
        image_path - the path of the image's data
        image_data - the image's data
    Return:
        The formatted CSV entry
    """
    cur_depl = camtrap[CAMTRAP_DEPLOYMENT]
    depl_id = get_deployment_id(image_data)

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


def add_camtrap_media(camtrap: dict, image_path: str, image_data: dict) -> str:
    """Adds the image data for camtrap media as a single CSV line
    Arguments:
        camtrap - the CamTrap data
        image_path - the path of the image's data
        image_data - the image's data
    Return:
        The formatted CSV entry
    """
    cur_media = camtrap[CAMTRAP_MEDIA]
    depl_id = get_deployment_id(image_data)

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


def add_camtrap_observation(camtrap: dict, image_path: str, image_data: dict) -> str:
    """Adds the image data for camtrap observation as a single CSV line
    Arguments:
        camtrap - the CamTrap data
        image_path - the path of the image's data
        image_data - the image's data
    Return:
        The formatted CSV entry
    """
    cur_obs = camtrap[CAMTRAP_OBSERVATIONS]
    depl_id = get_deployment_id(image_data)

    found_obs = None
    for one_obs in cur_obs:
        if image_path in one_obs:
            found_obs = one_obs
            break

    if not found_obs:
        timestamp = datetime.datetime.fromtimestamp(int(image_data["dateTimeTaken"])/1000.0)
        common_name = image_data["metaSpeciesCommonName"]
        found_obs = ",".join((
            "",         # observation ID
            depl_id,
            "",         # sequence ID
            image_path,
            timestamp.isoformat(),
            "",         # observation type
            "false",    # camera setup
            "",         # taxon ID
            common_name,
            image_data["metaSpeciesCount"],
            "0",        # count new
            "",         # life stage
            "",         # sex
            "",         # behavior
            "",         # individual ID
            "",         # classification method
            "",         # classified by
            "",         # classification timestamp
            "1.0",      # classification confidence
            f"[COMMONNAME:{common_name}]"
        ))
        cur_obs.append(found_obs)
        camtrap[CAMTRAP_OBSERVATIONS] = cur_obs
        camtrap["modified"] = True

    return found_obs


def update_camtrap(camtrap: dict, minio_path: str, image_md: tuple) -> None:
    """Updates the camtrap data with the image's data
    Arguments:
        camtrap - the CamTrap data
        minio_path - the path to the image on MinIO
        image_md - the metadata associated with the image
    """
    # Reformat the image data to be more easily accessible
    image_data = {}
    for one_md in image_md:
        image_data[one_md["Attribute"]] = one_md["Value"]

    add_camtrap_deployment(camtrap, minio_path, image_data)
    add_camtrap_media(camtrap, minio_path, image_data)
    add_camtrap_observation(camtrap, minio_path, image_data)


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


def upload_update_image(minio: Minio, cyverse_basepath: str, bucket: str, minio_basepath: str,
                        image_data: dict, camtrap: dict, track: ImageTrack) -> bool:
    # pylint: disable=too-many-arguments
    """Upload the image to minio and update the camtrap data
    Arguments:
        minio - the MinIO client instance to use
        cyverse_basepath - the path on CyVerse to the collection
        bucket - the bucket to upload the image to
        minio_basepath - the base path to upload the images to
        image_data - dict containing the information on the image
        camtrap - the camtrap data to append the image to
        track - used to track image upload progress
    """
    # Don't overwrite data
    relative_path = image_data["RelativePath"]
    minio_path = os.path.join(minio_basepath, relative_path)
    if image_found_camtrap(minio_path, camtrap):
        write_error(f"'{relative_path}' was found in CamTrap data")
        track.add_progress("Found in CamTrap data - not uploading")
        return False
    if exists_minio(minio, bucket, minio_path):
        write_error(f"'{relative_path}' was found on destination (but not CamTrap data - adding data)")
        update_camtrap(camtrap, minio_path, image_data["Metadata"])
        track.add_progress("Already on MinIO - not uploading")
        return False

    print(f"'{relative_path}' upload")
    cyverse_path = os.path.join(cyverse_basepath, relative_path)
    track.add_progress("Transferring from CyVerse to MinIO")
    track.add_progress(f"  from '{cyverse_path}' to '{bucket}:{minio_path}'")
    if not transfer_file(minio, cyverse_path, bucket, minio_path, True,
                         content_type="image/jpeg"):
        write_error(f"Unable to transfer image from '{cyverse_path}' to '{minio_path}'")
        track.add_progress("Transfer failed")
    else:
        track.add_progress("Image transferred")

    track.add_progress("Updating CamTrap data")
    update_camtrap(camtrap, minio_path, image_data["Metadata"])
    track.add_progress("Image transferred and CamTrap data updated")
    return True


def move_images(minio: Minio, cyverse_basepath: str, coll_subpath: str, minio_id: str,
                images: list) -> None:
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
    print(f"  Checking if bucket '{dest_bucket}' exists")
    if not minio.bucket_exists(dest_bucket):
        print(f" ... creating destination bucket {dest_bucket}")
        minio.make_bucket(dest_bucket)
    else:
        print(f" ... using bucket {dest_bucket}")

    # Get a temporary folder to work within
    work_dir = tempfile.mkdtemp(prefix="dsparcd_")

    # Upload collection.json and permissions.json
    dest_path = os.path.join(dest_coll_base, COLL_COLLECTION_FILE)
    print(f" ... transferring {COLL_COLLECTION_FILE} and {COLL_PERMISSIONS_FILE} from CyVerse")
    if exists_minio(minio, dest_bucket, dest_path):
        print(f"   ... {COLL_COLLECTION_FILE} already exists - skipping update")
    else:
        if not transfer_collection_file(minio, minio_id,
                                        os.path.join(cyverse_basepath, COLL_COLLECTION_FILE),
                                        dest_bucket, dest_path):
            if not upload_new_collection_json(minio, dest_bucket, dest_path, minio_id):
                write_error(f"Unable to upload new {COLL_COLLECTION_FILE} to {minio_id}")

    dest_path = os.path.join(dest_coll_base, COLL_PERMISSIONS_FILE)
    if exists_minio(minio, dest_bucket, dest_path):
        print(f"   ... {COLL_PERMISSIONS_FILE} already exists - skipping update")
    else:
        if not transfer_file(minio, os.path.join(cyverse_basepath, COLL_PERMISSIONS_FILE),
                             dest_bucket, dest_path, content_type="application/json"):
            if not upload_empty_json(minio, dest_bucket, dest_path):
                write_error(f"Unable to upload new {COLL_PERMISSIONS_FILE} to {minio_id}")

    # Create valid MinIO subpath under Uploads folder
    dest_uploads_base = os.path.join(dest_coll_base, "Uploads",
                                     coll_subpath.replace(' ', '.', 1).replace(' ', '_').
                                            replace('-', '.'))

    # Get UploadMeta.json from CyVerse and upload to MinIO
    cyverse_basepath = os.path.join(cyverse_basepath, "Uploads", coll_subpath)
    cyverse_path = os.path.join(cyverse_basepath, COLL_UPLOADMETA_FILE)
    dest_path = os.path.join(dest_uploads_base, COLL_UPLOADMETA_FILE)
    print(f" ... transferring upload {COLL_UPLOADMETA_FILE} from CyVerse")
    if exists_minio(minio, dest_bucket, dest_path):
        print(f"   ... {COLL_UPLOADMETA_FILE} already exists - skipping update")
    else:
        if not transfer_uploadmeta_file(minio, cyverse_path, dest_bucket, dest_path, True):
            write_error(f"{COLL_UPLOADMETA_FILE} not transferred from " +
                        f"'{cyverse_path}' to '{dest_path}'")

    # Get the deployments.csv, media.csv, and observations.csv files, and load them
    print(f" ... pulling camtrap files from '{work_dir}'")
    download_minio_files(minio, dest_bucket,
                         [os.path.join(dest_uploads_base, fn) for fn in MINIO_CAMTRAP_FILES],
                         work_dir)
    camtrap = load_camtrap(work_dir)

    # Upload each of the images and update CSV data
    for one_image in images:
        img_track = ImageTrack(cyverse_basepath, dest_bucket, dest_uploads_base, one_image)
        upload_update_image(minio, cyverse_basepath, dest_bucket, dest_uploads_base, one_image,
                            camtrap, img_track)
        img_track.complete(msg="Done")

    # Write the CamTrap data and upload the CSV files
    if camtrap["modified"]:
        print(" ... uploading camtrap files")
        write_camtrap(camtrap, work_dir)
        upload_local_files([os.path.join(work_dir, fn) for fn in MINIO_CAMTRAP_FILES], minio,
                           dest_bucket, dest_uploads_base, content_type="text/csv")
    else:
        print(" ... camtrap files not modified - skipping upload")

    # Clean up the temporary folder
    print(f" ... removing working folder {work_dir}")
    shutil.rmtree(work_dir)


def move_data(json_file: str, minio_id: str, user: str, pw: str) -> None:
    # pylint: disable=invalid-name
    """Moves the data from CyVerse to MinIO
    Arguments:
        json_file - name of the CyVerse exported JSON file containing entries to pull from
        minio_id - (optional) corresponding MinIO collection ID to upload to
        user - username for MinIO endpoint
        pw - password for MinIO endpoint
    Notes:
        If a MinIO ID is not specified, one will be generated and the CyVerse
        entries uploaded to that collection
    """

    # Load the JSON source file
    with open(json_file, "r", encoding="utf-8") as in_file:
        from_json = json.load(in_file)

    if UPLOADS_KEYWORD not in from_json:
        write_error(f"JSON file is missing keyword '{UPLOADS_KEYWORD}' " +
                    "- make sure this is a valid file")
        return

    # Create the MinIO Client instance
    print(f"Connecting to MinIO as user {user} '{MINIO_ENDPOINT}'")
    minio = Minio(MINIO_ENDPOINT, access_key=user, secret_key=pw)

    # Get the source collection ID
    cyverse_id = os.path.basename(from_json[BASEPATH_KEYWORD])

    # Determine what MinIO ID we should be using
    if not minio_id:
        use_minio_id = str(uuid.uuid4())
    else:
        use_minio_id = minio_id

    # Tracking how many uploads we're doing
    total_uploads = len(from_json[UPLOADS_KEYWORD])

    # Print out what we're doing
    print(f"Processing file '{json_file}'")
    print(f"    MinIO endpoint is {MINIO_ENDPOINT}")
    print(f"    CyVerse collection {cyverse_id} to {use_minio_id}" +
          (" (new)" if use_minio_id != minio_id else ""))
    print(f"    Processing {total_uploads} uploads")
    print(f"    CyVerse path: '{from_json[BASEPATH_KEYWORD]}'")

    # Loop through the entries
    cur_upload = 1
    for one_entry in from_json[UPLOADS_KEYWORD]:
        print(f"Upload {cur_upload} of {total_uploads}: {one_entry[COLL_UPLOAD_KEYWORD]} with " +
              str(len(one_entry[COLL_IMAGES_KEYWORD])) + " images")
        move_images(minio, from_json[BASEPATH_KEYWORD], one_entry[COLL_UPLOAD_KEYWORD],
                    use_minio_id, one_entry[COLL_IMAGES_KEYWORD])
        cur_upload = cur_upload + 1
    print("Done")


if __name__ == '__main__':
    m_json_file, m_minio_id, m_user, m_pw = get_params()
    move_data(m_json_file, m_minio_id, m_user, m_pw)
