/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class ImageEntry
{
	private File imagePath;
	private Date dateTaken;
	private Location locationTaken;
	private List<SpeciesEntry> speciesPresent = new ArrayList<SpeciesEntry>();

	public ImageEntry(File file)
	{
		this.imagePath = file;
		try
		{
			Metadata metadata = ImageMetadataReader.readMetadata(file);
			ExifSubIFDDirectory fileMetadata = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			this.dateTaken = fileMetadata.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
		}
		catch (ImageProcessingException | IOException e)
		{
			System.out.println("Error reading file metadata: " + file.getAbsolutePath() + "\nError is:\n" + e.toString());
		}
		System.out.println("\n");
	}

	public File getImagePath()
	{
		return this.imagePath;
	}

	public String getDateTakenFormatted()
	{
		return dateTaken.toString();
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
		this.speciesPresent.add(new SpeciesEntry(species, amount));
	}

	public List<SpeciesEntry> getSpeciesPresent()
	{
		return speciesPresent;
	}

	public ImageIcon createIcon(int width, int height)
	{
		ImageIcon icon = new ImageIcon(this.imagePath.getAbsolutePath());
		return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_FAST));
	}
}
