package model.image;

import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoShort;

public class SanimalMetadataFields
{
	public static final TagInfoShort SANIMAL = new TagInfoShort("Sanimal Flag", 551, 1, TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);
	public static final TagInfoAscii SPECIES_ENTRY = new TagInfoAscii("Species Entry", 552, -1, TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);
}
