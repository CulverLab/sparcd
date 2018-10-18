package model.util;

import model.SanimalData;
import model.constant.SanimalMetadataFields;
import model.image.ImageEntry;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class containing utils for writing & reading metadata
 */
public class MetadataUtils
{
	/**
	 * Reads the output set from a given image entry which contains metadata
	 *
	 * @param imageEntry The image entry to read from
	 *
	 * @return The output set containing metadata of the image entry
	 *
	 * @throws ImageWriteException If something went wrong reading the file...
	 * @throws IOException If something went wrong reading the file...
	 * @throws ImageReadException If something went wrong reading the file...
	 */
	public static TiffOutputSet readOutputSet(ImageEntry imageEntry) throws ImageWriteException, IOException, ImageReadException
	{
		// Grab the tiff output set which we write the metadata to, or create a new one if it's empty
		TiffOutputSet outputSet = null;

		// Grab the image metadata to read from
		TiffImageMetadata tiffImageMetadata = MetadataUtils.readImageMetadata(imageEntry.getFile());

		// Check if it's not null, create the output set to write to
		if (tiffImageMetadata != null)
			outputSet = tiffImageMetadata.getOutputSet();

		// If we don't have an output set, the image doesn't have any metadata so create one
		if (outputSet == null)
			outputSet = new TiffOutputSet();

		return outputSet;
	}

	/**
	 * Write the output set to a given image entry
	 *
	 * @param outputSet The metadata output set
	 * @param imageEntry The image entry to write to
	 *
	 * @throws ImageWriteException If something went wrong reading the file...
	 * @throws IOException If something went wrong reading the file...
	 * @throws ImageReadException If something went wrong reading the file...
	 */
	public static void writeOutputSet(TiffOutputSet outputSet, ImageEntry imageEntry) throws IOException, ImageWriteException, ImageReadException
	{
		// Write the new metadata back to the image, first we write it to a temporary file
		File tempToWriteTo = SanimalData.getInstance().getTempDirectoryManager().createTempFile("sanimalTMP.jpg");
		// Copy the current image file to the temporary file
		FileUtils.copyFile(imageEntry.getFile(), tempToWriteTo);
		// Then we create an output stream to that file
		if (tempToWriteTo.exists())
		{
			try (OutputStream outputStream = new FileOutputStream(tempToWriteTo))
			{
				// And perform the write to the temporary file
				new ExifRewriter().updateExifMetadataLossless(imageEntry.getFile(), outputStream, outputSet);
				// Then copy the temporary file over top of the current file to update it
				FileUtils.forceDelete(imageEntry.getFile());
				FileUtils.moveFile(tempToWriteTo, imageEntry.getFile());
			}
		}
	}

	/**
	 * Finds the sanimal EXIF directory or creates it if it is not present yet
	 *
	 * @param outputSet The output set to search for the sanimal directory
	 *
	 * @return The sanimal directory, cannot be null
	 *
	 * @throws ImageWriteException If something went wrong reading the directory...
	 */
	public static TiffOutputDirectory getOrCreateSanimalDirectory(TiffOutputSet outputSet) throws ImageWriteException
	{
		// Sanimal directory type goes in 3? Currently -4, -3, -2, -1, 0, 1, and 2 are used by the JPEG format. Anything less than -4 is NOT allowed
		Integer sanimalDirIndex = 1;

		// Ensure we have a root directory
		outputSet.getOrCreateRootDirectory();

		TiffOutputDirectory sanimalDir = null;

		// Loop while we don't have a sanimal directory yet
		for (; sanimalDir == null; sanimalDirIndex++)
		{
			// Find the directory at the current index
			TiffOutputDirectory current = outputSet.findDirectory(sanimalDirIndex);
			// If the directory is null, we have an empty slot, so create the directory
			if (current == null)
			{
				sanimalDir = new TiffOutputDirectory(sanimalDirIndex, outputSet.byteOrder);
				sanimalDir.add(SanimalMetadataFields.SANIMAL, (short) 1);
				outputSet.addDirectory(sanimalDir);
			}
			// Otherwise, we check if we have the sanimal field, and if we do return it!
			else
			{
				if (current.findField(SanimalMetadataFields.SANIMAL) != null)
				{
					sanimalDir = current;
				}
			}
		}

		return sanimalDir;
	}

	/**
	 * Returns the tiff image metadata which we can read sanimal data from
	 *
	 * @param imageFile The image to read the metadata from
	 *
	 * @return The Image's metadata or null if no metadata was found (this probably means it's not a jpeg image...)
	 *
	 * @throws ImageReadException If something went wrong reading the image...
	 * @throws IOException If something went wrong reading the image...
	 */
	public static TiffImageMetadata readImageMetadata(File imageFile) throws ImageReadException, IOException
	{
		// Read the image's metadata
		ImageMetadata metadata = Imaging.getMetadata(imageFile);

		// Grab the tiff metadata to read from, or return null
		if (metadata instanceof JpegImageMetadata)
			return ((JpegImageMetadata) metadata).getExif();
		else
			return null;
	}
}
