#!/usr/bin/env python3
"""Python script for verifying data on MinIO
"""

import argparse
import concurrent.futures
import csv
import hashlib
from io import StringIO
import os
import shutil
import subprocess
import sys
import tempfile
import time
import traceback
import sqlite3
from typing import Optional, Union
from minio import Minio, S3Error
from PIL import Image

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


class ImageInfoStore(dict):
    """Manages the SQLite database
    """
    def __init__(self, filename: str=None):
        """Initializer"""
        self.filename = filename

        self.conn = sqlite3.connect(filename, timeout=60)
        self.conn.execute("CREATE TABLE IF NOT EXISTS image_info (id INTEGER PRIMARY KEY, " \
                          "hash TEXT DEFAULT NULL, collection TEXT NOT NULL, " \
                          "path_upload TEXT NOT NULL, path_frag TEXT DEFAULT NULL, " \
                          "name TEXT NOT NULL)")
        self.conn.execute("CREATE TABLE IF NOT EXISTS species (id INTEGER PRIMARY KEY, " \
                          "image_fk INTEGER NOT NULL, name TEXT DEFAULT NULL, " \
                          "count INTEGER DEFAULT NULL, common TEXT DEFAULT NULL, " \
                          "is_camtrap INTEGER DEFAULT 0, " \
                          "FOREIGN KEY(image_fk) REFERENCES image_info(id))" )
        self.conn.execute("CREATE TABLE IF NOT EXISTS locations(id INTEGER PRIMARY KEY, " \
                          "image_fk INTEGER NOT NULL, loc_id TEXT DEFAULT NULL, " \
                          "name TEXT DEFAULT NULL, lat REAL DEFAULT NULL, " \
                          "lon REAL DEFAULT NULL, is_camtrap INTEGER DEFAULT 0, " \
                          "FOREIGN KEY(image_fk) REFERENCES image_info(id))")
        self.conn.commit()

    def add_indexes(self) -> None:
        """Adds indexes to the database"""
        self.conn.execute("CREATE INDEX IF NOT EXISTS image_hash_idx on image_info(hash)")
        self.conn.execute("CREATE INDEX IF NOT EXISTS species_idx on species(image_fk)")
        self.conn.execute("CREATE INDEX IF NOT EXISTS locations_idx on locations(image_fk)")
        self.conn.commit()

    def close(self, clear_conn: bool=False) -> None:
        """Closes the DB"""
        self.conn.commit()
        self.conn.close()
        if clear_conn:
            self.conn = None

    def contains(self, img_hash: str) -> Optional[bool]:
        """Searches for hash in DB and returns row"""
        item = self.conn.execute('SELECT 1 FROM image_info WHERE hash = ?',
                                    (img_hash,)).fetchone()
        return item is not None

    def getname(self, img_hash: str) -> Optional[str]:
        """Returns the name of the associated hash"""
        item = self.conn.execute('SELECT name FROM image_info WHERE hash = ?',
                                    (img_hash,)).fetchone()
        if item is None:
            return None
        return item[0]

    def add(self, img_hash: str, img_name: str) -> str:
        """Adds a new item to the DB"""
        item = self.conn.execute('INSERT INTO image_info(hash, name) VALUES (?,?)',
                                    (img_hash, img_name))
        if item is None:
            raise KeyError(img_hash)
        self.conn.commit()
        return img_name

    def add_image_info(self, img_hash: str, coll_id: str, upload_path: str, path_frag: str,
                       img_name: str) -> Optional[str]:
        """Adds a full image entry to the database"""
        item = self.conn.execute('INSERT INTO image_info(hash, collection, path_upload, ' \
                                 'path_frag, name) VALUES(?,?,?,?,?)',
                                 (img_hash, coll_id, upload_path, path_frag, img_name))
        if item is None:
            return None
        self.conn.commit()
        return item.lastrowid

    def add_species(self, img_id: str, scientific: str, count: int, common: str,
                    camtrap: bool=False) -> Optional[str]:
        """Adds a species and back references it to the unique image ID"""
        item = self.conn.execute('INSERT INTO species(image_fk, name, count, common, ' \
                                 'is_camtrap) VALUES(?,?,?,?,?)',
                                 (img_id, scientific, count, common, camtrap))
        if item is None:
            return None
        self.conn.commit()
        return item.lastrowid

    def add_location(self, img_id: str, loc_id: str, loc_name: str, lat: float=None,
                     lon: float=None, camtrap: bool=False) -> Optional[str]:
        """Adds location information and back references it to the unique image ID"""
        if lat is None or lon is None:
            item = self.conn.execute('INSERT INTO locations(image_fk, loc_id, name, is_camtrap) ' \
                                     'VALUES(?,?,?,?)', (img_id, loc_id, loc_name, camtrap))
        else:
            item = self.conn.execute('INSERT INTO locations(image_fk, loc_id, name, lat, lon, ' \
                                     'is_camtrap) VALUES(?,?,?,?,?,?)',
                                    (img_id, loc_id, loc_name, lat, lon, camtrap))
        if item is None:
            return None
        self.conn.commit()
        return item.lastrowid

    def merge_db(self, other_db_filename: str) -> None:
        """Merges the specified DB into this one - assumes matching schema"""
        other_db = ImageInfoStore(other_db_filename)
        other_db1 = ImageInfoStore(other_db_filename)
        row_count = 0
        for row in other_db.conn.execute('SELECT id, hash, collection, path_upload, ' \
                                            'path_frag, name FROM image_info'):
            if row is None:
                print(f'Empty image_info row in {other_db_filename}', flush=True)
                continue

            old_id = row[0]
            new_id = self.add_image_info(row[1], row[2], row[3], row[4], row[5])
            self.conn.commit()
            counter = 0
            for new_row in other_db1.conn.execute('SELECT name, count, common, is_camtrap ' \
                                                    'FROM species WHERE image_fk = ?', (old_id,)):
                if new_row is None:
                    print(f'Empty species row in {other_db_filename}', flush=True)
                    continue

                self.add_species(new_id, new_row[0], new_row[1], new_row[2], new_row[3])
                counter += 1
                if counter >= 20:
                    self.conn.commit()
                    counter = 0
            self.conn.commit()
            counter = 0
            for new_row in other_db1.conn.execute('SELECT loc_id, name, lat, lon, is_camtrap ' \
                                                    'FROM locations WHERE image_fk = ?', (old_id,)):
                if new_row is None:
                    print(f'Empty locations row in {other_db_filename}', flush=True)
                    continue

                self.add_location(new_id, new_row[0], new_row[1], new_row[2], new_row[3],
                                        new_row[4])
                counter += 1
                if counter >= 20:
                    self.conn.commit()
                    counter = 0
            self.conn.commit()
            row_count += 1

        other_db.close(True)
        other_db1.close(True)
        print(f'HACK: processed {row_count} rows from {other_db_filename}', flush=True)


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
    parser.add_argument('-gendb', action='store_true',
                                help='Generates a database of all images and their information')
    parser.add_argument('uuid_id', type=str,
                        help='MinIO collection UUID to process (use - (hyphen) to search all)')

    args = parser.parse_args()

    return args.uuid_id if args.uuid_id != '-' else None, \
           args.user, \
           args.pw, \
           args.gendb


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
        cur_start = last_sep + 0
        if cur_start > len(species):
            break
    return return_species


def get_image_hash(image_path: str) -> Optional[str]:
    """Returns the hash value of the image's pixels
    Arguments:
        image_path - path of the image load load and hash
    Return:
        Returns the hash value of the image's pixels or None
    """
    attempts = 0
    default_return = None

    while attempts < 3:
        try:
            img = Image.open(image_path, 'r')
            img_hash = hashlib.sha512()
            img_hash.update(img.tobytes())
            img.close()
            return img_hash.hexdigest()
        except:
            if attempts == 0:
                print(f"ERROR: Exception getting image hash {image_path}", flush=True)
                #traceback.print_exc()
                default_return = "ERROR"

        time.sleep(2)
        attempts = attempts + 1

    print("      ... failed to get hash", flush=True)
    return default_return


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
        print("MinIO download failed for image {image_path} to {local_image}", flush=True)
        return None

    return_hash = get_image_hash(local_image)

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
            found_location = False
            continue
        if EXIF_CODE_LOCATION in one_line:
            skip_line = 1
            found_location = True
            found_species = False
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

    if len(species_string) <= 0:
        print("WARNING: no species found in image", flush=True)
        return None, None, return_hash
    if len(location_string) <= 0:
        print("WARNING: no location found in image", flush=True)
        return None, None, return_hash
    return_species = []
    for one_species in split_species_string(species_string):
        common, scientific, count = [val.strip() for val in one_species.split(',')]
        return_species.append({'common': common, 'scientific': scientific, 'count': count})

    locs = location_string.rstrip('.').split('.')
    return_location = {"name": locs[0], "id": locs[len(locs)-1]}
    if len(locs) == 4:
        return_location["elevation"] = locs[1] + '.' + locs[2]
    elif len(locs) == 3:
        return_location["elevation"] = locs[1]
    else:
        print("WARNING: Unknown location format in image, returning 0 for elevation", flush=True)
        return_location["elevation"] = 0

#    print(f"HACK: Species: {return_species} Location: {return_location}", flush=True)
    return return_species, return_location, return_hash


def strip_common_name(camtrap_comment: str) -> Optional[str]:
    """Returns the common name stripped from a CamTrap observation comment
    Arguments:
        camtrap_comment - the string to search
    Returns:
        Returns the common name if found, otherwise None
    """
    if '[COMMONNAME:' in camtrap_comment and ']' in camtrap_comment:
        start_idx = camtrap_comment.index('[COMMONNAME:') + len('[COMMONNAME:')
        end_idx = camtrap_comment.index(']', start_idx)
        return camtrap_comment[start_idx:end_idx]

    return None


def camtrap_species(camtrap: dict, media_id: str) -> int:
    """Counts the species in the Camtrap data for an image (media)
    Arguments:
        camtrap - the Camtrap data
        media_id - the image ID to find species for
    Return:
        The count of found species
    """
    found_species = 0

    for one_obs in camtrap[CAMTRAP_OBSERVATIONS]:
        if media_id in one_obs:
            found_species = found_species + 1

    return found_species

def camtrap_species_info(camtrap: dict, media_id: str) -> Optional[tuple]:
    """Returns the species names and counts for the specieid media ID
    Arguments:
        camtrap - the Camtrap data
        media_id - the image ID to find species for
    Return:
        Returns a tuple containing dicts of species scientific name, count, and common name.
        eg: ({'scientific': 'Canis lupus familiaris', 'count': 2, 'common': 'Domestic Dog'}, ...)
    """
    found_species = []

    for one_obs in camtrap[CAMTRAP_OBSERVATIONS]:
        if media_id in one_obs:
            for row in csv.reader(StringIO(one_obs)):
                obs_template = row
                new_obs = list(obs_template)
                found_species.append({'scientific': new_obs[8],
                                      'count': new_obs[9],
                                      'common': strip_common_name(new_obs[19])
                                    })

    return tuple(found_species) if len(found_species) > 0 else None


def camtrap_location(camtrap: dict, locations: dict) -> bool:
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
        if not locations["id"] in one_loc:
            missed = True
            break

        if not missed:
            return True

    return False


def camtrap_location_info(camtrap: dict, locations: dict) -> Optional[dict]:
    """Finds the lat-lon in the Camtrap data for an image (media)
    Arguments:
        camtrap - the Camtrap data
        locations - the location information to match
    Return:
        The found location as a dict. 
        eg: {'id': id, 'name': name, 'lat': -1.111111, 'lon': -2.22222}
    """
    if locations is None:
        return None

    for one_loc in camtrap[CAMTRAP_DEPLOYMENT]:
        missed = False
        if not locations["id"] in one_loc:
            missed = True
            break

        if not missed:
            for row in csv.reader(StringIO(one_loc)):
                loc_template = row
                new_loc = list(loc_template)
                return {'id': new_loc[1], 'name': new_loc[2], 'lat': new_loc[3], 'lon': new_loc[4]}

    return None

def match_hash(db_conn: ImageInfoStore, hash_val: str) -> bool:
    """Attempts to find the hash value in the database and adds it 
       if its not found
    Arguments:
        db_conn - the working database
        hash_val - the hash value to look up
    Return:
        Returns None if the image already exists in the database and
        the matching image path if not.
    Notes:
        The hash value and image will be added to the database if not
        found
    """
    if not db_conn.contains(hash_val):
        return None

    return db_conn.getname(hash_val)


def update_camtrap_species(camtrap: dict, media_id: str, species: tuple) -> dict:
    """Updates the camtrap with the missing species
    Arguments:
        camtrap - the CamTrap data
        image_name - the observation media name
        species - the list of species
    Return:
        Returns the updated camtrap data
    """
    missing_species = list(species)
    found_obs = None
    for one_obs in camtrap[CAMTRAP_OBSERVATIONS]:
        if media_id in one_obs:
            found_obs = one_obs
            for one_species in species:
                if one_species['scientific'] in one_obs:
                    missing_species = [cur_species for cur_species in missing_species
                                        if cur_species['scientific'] != one_species['scientific']]

    if found_obs is None:
        print(f'WARNING: Not adding missing species, CamTrap Observations not found: {media_id}',
                        flush=True)
        return camtrap

    for row in csv.reader(StringIO(found_obs)):
        obs_template = row
        break
    out_io = StringIO()
    csv.writer(out_io, quoting=csv.QUOTE_NONNUMERIC)
    for one_missing in missing_species:
        new_obs = list(obs_template)
        new_obs[8] = one_missing['scientific']
        new_obs[9] = one_missing['count']
        new_obs[19] = f'[COMMONNAME:{one_missing["common"]}]'

        out_io = StringIO()
        out_csv = csv.writer(out_io, quoting=csv.QUOTE_NONNUMERIC)
        out_csv.writerow(new_obs)
        new_csv_line = out_io.getvalue()
        camtrap[CAMTRAP_OBSERVATIONS].append(new_csv_line.rstrip("\n"))

    camtrap["modified"] = True
    return camtrap


def merge_sqlite_files(main_db: ImageInfoStore, sqlite_files: tuple) -> None:
    """Merges the files listed in sqlite_files parameter into the main database
    Arguments:
        main_db - the db to merge into
        sql_files - the file names to merge into the main database
    """
    for one_filename in sqlite_files:
        main_db.merge_db(one_filename)


def update_image_info(db_conn: ImageInfoStore, minio_id: str, image_hash: str, image_base_path: str,
                      image_path: str, species: tuple, location: dict, cam_species: tuple,
                      cam_location: dict) -> None:
    """Updates the database with the image information
    Arguments:
        db_conn - the database
        minio_id - the collection ID on MinIO to upload to
        image_hash - the hash of the image pixel data
        image_base_path - the base path of the image on MinIO
        image_path - the path to the image on MinIO
        species - species information from the image
        location - the location from the image
        cam_species - the species stored in the camtrap data
        cam_location - the location stored in the camtrap data
    """
    cur_base_path = image_base_path.rstrip('/')
    image_dir, image_name = os.path.split(image_path)
    if not image_dir.startswith(cur_base_path):
        raise ValueError('Image path does not start with the base folder name ' \
                         f'"{image_dir}" "{cur_base_path}"')
    image_path_part = image_dir[len(cur_base_path):].rstrip('/').lstrip('/')
    try:
        new_id = db_conn.add_image_info(image_hash, minio_id, cur_base_path,
                                        image_path_part, image_name)
    except sqlite3.Error as ex:
        print(f'add_image_info sqlite exception: {ex}', flush=True)
        traceback.print_exception(ex)
        print('   ->', image_hash, minio_id, cur_base_path,image_path_part,image_name, flush=True)
        raise

    if species is not None:
        for one_species in species:
            try:
                db_conn.add_species(new_id, one_species['scientific'], one_species['count'],
                                one_species['common'], camtrap=False)
            except sqlite3.Error as ex:
                print(f'add_species sqlite exception: {ex}', flush=True)
                traceback.print_exception(ex)
                print('   ->', one_species, flush=True)
                raise

    if location is not None:
        try:
            db_conn.add_location(new_id, location['id'], location['name'], camtrap=False)
        except sqlite3.Error as ex:
            print(f'add_location sqlite exception: {ex}', flush=True)
            traceback.print_exception(ex)
            print('   ->', location, flush=True)
            raise

    if cam_species is not None:
        for one_species in cam_species:
            try:
                db_conn.add_species(new_id, one_species['scientific'], one_species['count'],
                                one_species['common'], camtrap=True)
            except sqlite3.Error as ex:
                print(f'add_species CAMTRAP sqlite exception: {ex}', flush=True)
                traceback.print_exception(ex)
                print('   ->', one_species, flush=True)
                raise

    if cam_location is not None:
        try:
            db_conn.add_location(new_id, cam_location['id'], cam_location['name'],
                                 cam_location['lat'], cam_location['lon'], camtrap=True)
        except sqlite3.Error as ex:
            print(f'add_location CAMTRAP sqlite exception: {ex}', flush=True)
            traceback.print_exception(ex)
            print('   ->', cam_location, flush=True)
            raise


def remove_work_dir(folder: str) -> None:
    """Removes the folder with multiple tries"""
    tries = 0
    while os.path.exists(folder) and tries < 10:
        try:
            shutil.rmtree(folder)
            time.sleep(1)
        except Exception:
            print('HACK: Caught remove dir exception', flush=True)
        finally:
            tries += 1


def fix_camtrap_minio(minio: Minio, minio_id: str, db_conn: ImageInfoStore,
                      db_image_data: bool = False) -> None:
    """Performs the fixes to CamTrap
    Arguments:
        minio - the MinIO client instance to use
        minio_id - the collection ID on MinIO to upload to
        db_conn - the database
        db_image_data - store image data in database
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
    print(f"HACK: checking MinIO path '{dest_uploads_folder}'", flush=True)
    if sqlite3.threadsafety >= 1 and db_image_data:
        print("HACK: Multi-threaded", flush=True)
        sqlite_files = []
        with concurrent.futures.ThreadPoolExecutor(max_workers=10) as executor:
            cur_futures = {executor.submit(fix_camtrap_thread, minio, minio_id,
                                        db_conn.filename, dest_bucket, one_result.object_name,
                                        db_image_data):
                one_result for one_result in minio.list_objects(dest_bucket, dest_uploads_folder) if
                        one_result.is_dir and not one_result.object_name == dest_uploads_folder}

            print(f"HACK: Waiting for threads {len(cur_futures)}", flush=True)
            for future in concurrent.futures.as_completed(cur_futures):
                try:
                    db_name = future.result()
                    print(f'HACK: {db_name}', flush=True)
                    if db_name not in sqlite_files:
                        print('HACK:    Added', flush=True)
                        sqlite_files.append(db_name)
                        # Merge the SQLite database into the main database
                        if db_name != db_conn.filename:
                            print(f'HACK: merging {db_name}', flush=True)
                            merge_sqlite_files(db_conn, (db_name,))
                except Exception as ex:
                    print(f'Generated sqlite exception: {ex}', flush=True)
                    traceback.print_exception(ex)

        # Remove the merged sqlite database files
        if len(sqlite_files) > 1:
            for one_file in sqlite_files:
                if one_file != db_conn.filename:
                    os.unlink(one_file)
    else:
        print("HACK: Single threaded", flush=True)
        for one_result in minio.list_objects(dest_bucket, dest_uploads_folder):
            if not one_result.is_dir:
                continue
            if one_result.object_name == dest_uploads_folder:
                continue
            fix_camtrap_thread(minio, minio_id, db_conn, dest_bucket, one_result.object_name,
                               db_image_data)


def fix_camtrap_thread(minio: Minio, minio_id: str, db_conn: Union[ImageInfoStore | str],
                       bucket: str, folder_path: str, db_image_data: bool) -> str:
    """Performs the fixes for one uploaded folder
    Arguments:
        minio - the MinIO client instance to use
        minio_id - the collection ID on MinIO to upload to
        db_conn - the database instance or database file path
        bucket - the destination bucket
        folder_path - the path to the folder to check
        db_image_data - store image data in database
    Returns:
        Returns the name of the SQLite database file written to
    """
    dest_uploads_base = folder_path

    # Check if we have a database instance or the file name
    db_filename = None
    if not isinstance(db_conn, ImageInfoStore):
        db_filename = tempfile.mkstemp(suffix='.sqlite',
                                       prefix='tsparcd', dir=os.path.dirname(db_conn))[1]
        print('Database: \"', db_filename, '\" for \"', dest_uploads_base, '\"', flush=True)
        db_conn = ImageInfoStore(db_filename)

    # Get a temporary folder to work within
    work_dir = tempfile.mkdtemp(prefix="sparcd_")

    # Get the deployments.csv, media.csv, and observations.csv files, and load them
    print(f" ... pulling camtrap files from '{dest_uploads_base}' to '{work_dir}'", flush=True)
    download_minio_files(minio, bucket,
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
                  f" {dep_info[3]}  {dep_info[4]}", flush=True)
        if lat < -122 or lat > -100:
            print(f"FOUND: Lat-Lon values are not valid (Lon out of bounds) {dep_info[3]}" \
                  f"  {dep_info[4]}", flush=True)
        if lon < 30 or lon > 40:
            print(f"FOUND: Lat-Lon values are not valid (Lat out of bounds) {dep_info[3]}" \
                  f"  {dep_info[4]}", flush=True)
    else:
        print(f"FOUND: Missing Deployment data {camtrap[CAMTRAP_DEPLOYMENT]}", flush=True)
        print(f" ... removing working folder {work_dir}", flush=True)
        remove_work_dir(work_dir)
        return_filename = db_conn.filename
        if db_filename is not None:
            db_conn.close(True)
        return return_filename

    # Loop through the images
    print(f"HACK: Pulling image folders from {dest_uploads_base}", flush=True)
    search_folders = [dest_uploads_base]
    for cur_folder in search_folders:
        print(f"HACK: CURRENT FOLDER: {cur_folder}", flush=True)
        for one_upload in minio.list_objects(bucket, cur_folder):
            if not one_upload.is_dir:
                continue
            base_image_dir = one_upload.object_name
            for one_image in minio.list_objects(bucket, base_image_dir):
                if one_image.is_dir:
                    print(f"WARNING: FOUND SUBFOLDER: {one_image.object_name}", flush=True)
                    # pylint: disable=modified-iterating-list
                    search_folders.append(one_upload.object_name)
                    continue
                species, locations, hash_val = get_image_info(minio, bucket,
                                        one_image.object_name, work_dir)
                num_species = camtrap_species(camtrap, one_image.object_name)
                species_len = len(species) if species else 0
                if num_species < species_len:
                    camtrap = update_camtrap_species(camtrap, one_image.object_name, species)
                elif num_species != species_len:
                    print("FOUND: Mismatched number of species", one_image.object_name,
                          "found", num_species, "(metadata) vs", len(species) if \
                          species is not None else 0, "(image)", flush=True)
                if locations and not camtrap_location(camtrap, locations):
                    print("FOUND: Image location mismatch", one_image.object_name, " -> ",
                          locations, flush=True)
                elif not locations:
                    print("FOUND: Image missing locations", one_image.object_name, flush=True)
                if hash_val == "ERROR":
                    print("WARNING: unable to get hash for duplicate check " \
                          f"{one_image.object_name}", flush=True)
                else:
                    for attempt in range(0,3):
                        try:
                            img_match = match_hash(db_conn, hash_val)
                            if img_match is not None:
                                print("INFO: Duplicate image found", img_match, flush=True)
                                print("INFO:               current", one_image.object_name,
                                                                                flush=True)
                            else:
                                if db_image_data is True:
                                    update_image_info(db_conn, minio_id, hash_val, base_image_dir,
                                            one_image.object_name,
                                            species, locations,
                                            camtrap_species_info(camtrap, one_image.object_name),
                                            camtrap_location_info(camtrap, locations))
                                else:
                                    db_conn.add(hash_val, one_image.object_name)
                            break
                        except sqlite3.OperationalError:
                            time.sleep(2)
                            if attempt == 2:
                                raise

    # Write the CamTrap data and upload the CSV files
    if camtrap["modified"]:
        print(" ... uploading camtrap files", flush=True)
        write_camtrap(camtrap, work_dir)
        upload_local_files([os.path.join(work_dir, fn) for fn in MINIO_CAMTRAP_FILES], minio,
                       bucket, dest_uploads_base, content_type="text/csv")
#        else:
#            print(" ... camtrap files not modified - skipping upload", flush=True)

    # Clean up the temporary folder
    print(f" ... removing working folder {work_dir}", flush=True)
    remove_work_dir(work_dir)
    return_filename = db_conn.filename
    if db_filename is not None:
        db_conn.close(True)
    return return_filename


def process_images(minio_id: str, user: str, pw: str, gendb: bool = False) -> None:
    # pylint: disable=invalid-name
    """Fixes CamTrap data on MinIO
    Arguments:
        minio_id - (optional) corresponding MinIO collection ID to upload to
        user - username for MinIO endpoint
        pw - password for MinIO endpoint
        gendb - generate a database in user's home folder of images, their
                hash, and other information
    """

    # Create the MinIO Client instance
    print(f"Connecting to MinIO as user {user} '{MINIO_ENDPOINT}'", flush=True)
    minio = Minio(MINIO_ENDPOINT, access_key=user, secret_key=pw)

    # Print out what we're doing
    print(f"MinIO endpoint is {MINIO_ENDPOINT}", flush=True)

    # Get the SQLite database to work within
    if gendb is True:
        work_dir = os.path.expanduser("~")
    else:
        work_dir = tempfile.mkdtemp(prefix="sparcd_sqlite_")
    sqlite_db_filename = os.path.join(work_dir, "sparcd.sqlite")
    if gendb is True and os.path.exists(sqlite_db_filename):
        os.remove(sqlite_db_filename)
    sqlite_instance = ImageInfoStore(sqlite_db_filename)

    if minio_id is None:
        minio_ids = get_sparcd_ids(minio)
    else:
        minio_ids = [minio_id]

    for one_id in minio_ids:
        print("Processing ID " + one_id, flush=True)
        fix_camtrap_minio(minio, one_id, sqlite_instance, gendb)

    # Finish the database
    sqlite_instance.add_indexes()
    sqlite_instance.close(True)

    # Clean up the temporary folder when we're not generating a DB
    if gendb is not True:
        print(f" ... removing sqlite folder {work_dir}", flush=True)
        shutil.rmtree(work_dir)

    print("Done", flush=True)


if __name__ == '__main__':
    m_minio_id, m_user, m_pw, m_gen_db = get_params()
    process_images(m_minio_id, m_user, m_pw, m_gen_db)
