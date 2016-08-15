package model.image;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;

/**
 * A class representing an image file
 * 
 * @author David Slovikosky
 */
public class ImageEntry implements Serializable
{
	// The format with which to print the date out in
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY MM dd hh mm ss");

	// The actual file 
	private File imageFile;
	// The date that the image was taken
	private Date dateTaken;
	// The location that the image was taken
	private Location locationTaken;
	// The species present in the image
	private List<SpeciesEntry> speciesPresent = new ArrayList<SpeciesEntry>();

	/**
	 * Create a new image entry with an image file
	 * 
	 * @param file
	 *            The file (must be an image file)
	 */
	public ImageEntry(File file)
	{
		this.imageFile = file;
		try
		{
			this.dateTaken = new Date(Files.readAttributes(file.toPath(), BasicFileAttributes.class).lastModifiedTime().toMillis());
		}
		catch (IOException e)
		{
		}
	}

	/**
	 * Get the image file
	 * 
	 * @return The image file
	 */
	public File getImageFile()
	{
		return this.imageFile;
	}

	/**
	 * Returns the date taken as a formatted string
	 * 
	 * @return The formatted date
	 */
	public String getDateTakenFormatted()
	{
		//this.validateDate();
		return dateTaken.toString();
	}

	/**
	 * Returns the date the image was taken
	 * 
	 * @return The date the image was taken
	 */
	public Date getDateTaken()
	{
		//this.validateDate();
		return dateTaken;
	}

	/**
	 * Set the location that the image was taken at
	 * 
	 * @param location
	 *            The location
	 */
	public void setLocationTaken(Location location)
	{
		this.locationTaken = location;
	}

	/**
	 * Return the location that the image was taken
	 * 
	 * @return The location
	 */
	public Location getLocationTaken()
	{
		return locationTaken;
	}

	/**
	 * Add a new species to the image
	 * 
	 * @param species
	 *            The species of the animal
	 * @param amount
	 *            The number of animals in the image
	 */
	public void addSpecies(Species species, Integer amount)
	{
		// Remove any other occurrences of the species from the image
		this.removeSpecies(species);
		this.speciesPresent.add(new SpeciesEntry(species, amount));
	}

	/**
	 * Remove a species from the list of image species
	 * 
	 * @param species
	 *            The species to remove
	 */
	public void removeSpecies(Species species)
	{
		this.speciesPresent.removeIf(entry ->
		{
			return entry.getSpecies() == species;
		});
	}

	/**
	 * Get the list of present species
	 * 
	 * @return A list of present species
	 */
	public List<SpeciesEntry> getSpeciesPresent()
	{
		return speciesPresent;
	}

	/**
	 * Create an image icon of this file with a given width and height
	 * 
	 * @param width
	 *            The width of the scaled icon
	 * @param height
	 *            The height of the scaled icon
	 * @return The image icon representing the scaled image
	 */
	public ImageIcon createIcon(Integer width, Integer height)
	{
		return ImageLoadingUtils.createImageIcon(imageFile, width, height, Image.SCALE_SMOOTH);
	}

	/**
	 * Renames the image file based on the formatted date
	 */
	public void renameByDate()
	{
		//this.validateDate();
		String newFilePath = imageFile.getParentFile() + File.separator;
		String newFileName = DATE_FORMAT.format(this.dateTaken);
		String newFileExtension = imageFile.getName().substring(imageFile.getName().lastIndexOf('.'));
		String newFileCompletePath = newFilePath + newFileName + newFileExtension;
		File newFile = new File(newFileCompletePath);
		int numberOfDuplicateFiles = 0;
		while (newFile.exists())
		{
			newFileCompletePath = newFilePath + newFileName + " (" + numberOfDuplicateFiles++ + ")" + newFileExtension;
			newFile = new File(newFileCompletePath);
		}
		boolean result = this.imageFile.renameTo(newFile);
		if (result == false)
			System.err.println("Error renaming file: " + this.imageFile.getAbsolutePath());
	}

	/**
	 * Returns the name of this image entry
	 */
	@Override
	public String toString()
	{
		return this.imageFile.getName();
	}

	//	private void validateDate()
	//	{
	//		if (this.dateTaken == null)
	//		{
	//			try
	//			{
	//				Metadata metadata = ImageMetadataReader.readMetadata(this.imageFile);
	//				FileMetadataDirectory fileMetadata = metadata.<FileMetadataDirectory> getFirstDirectoryOfType(FileMetadataDirectory.class);
	//				if (fileMetadata != null)
	//					dateTaken = fileMetadata.getDate(FileMetadataDirectory.TAG_FILE_MODIFIED_DATE);
	//			}
	//			catch (ImageProcessingException | IOException e)
	//			{
	//				System.out.println("Error reading file metadata: " + this.imageFile.getAbsolutePath() + "\nError is:\n" + e.toString());
	//			}
	//		}
	//	}
}
