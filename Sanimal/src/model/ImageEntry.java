package model;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

public class ImageEntry
{
	private File imageFile;
	private Date dateTaken;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("YYYY MM dd hh mm ss");
	private Location locationTaken;
	private List<SpeciesEntry> speciesPresent = new ArrayList<SpeciesEntry>();

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

	public File getImageFile()
	{
		return this.imageFile;
	}

	public String getDateTakenFormatted()
	{
		//this.validateDate();
		return dateTaken.toString();
	}

	public Date getDateTaken()
	{
		//this.validateDate();
		return dateTaken;
	}

	public void setLocationTaken(Location location)
	{
		this.locationTaken = location;
	}

	public Location getLocationTaken()
	{
		return locationTaken;
	}

	public void addSpecies(Species species, Integer amount)
	{
		// Remove any other occurrences of the species from the image
		this.removeSpecies(species);
		this.speciesPresent.add(new SpeciesEntry(species, amount));
	}

	public void removeSpecies(Species species)
	{
		this.speciesPresent.removeIf(entry ->
		{
			return entry.getSpecies() == species;
		});
	}

	public List<SpeciesEntry> getSpeciesPresent()
	{
		return speciesPresent;
	}

	public ImageIcon createIcon(int width, int height)
	{
		return ImageLoadingUtils.createImageIcon(imageFile, width, height, Image.SCALE_SMOOTH);
	}

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
