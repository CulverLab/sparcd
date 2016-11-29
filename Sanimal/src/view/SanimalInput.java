package view;

import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang3.ArrayUtils;

import controller.Constants;
import model.analysis.SanimalAnalysisUtils;
import model.image.IImageContainer;
import model.location.Location;
import model.location.UTMCoord;
import model.species.Species;

/**
 * A class of utility methods that perform user input
 * 
 * @author David Slovikosky
 */
public class SanimalInput
{
	private static final Character[] INVALID_UTM_LETTERS = new Character[]
	{ 'A', 'B', 'I', 'O', 'Y', 'Z' };

	/**
	 * Given a list of invalid image containers, this function prompts the user to fix each invalid path
	 * 
	 * @param imageContainers
	 *            The list of invalid image containers
	 */
	public static void askUserToValidateProject(List<IImageContainer> imageContainers)
	{
		for (IImageContainer container : imageContainers)
		{
			final File invalidFile = container.getFile();
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			chooser.setDialogTitle("Where is the folder or file named '" + invalidFile.getName() + "'");
			chooser.setFileFilter(new FileFilter()
			{
				@Override
				public String getDescription()
				{
					return invalidFile.getName();
				}

				@Override
				public boolean accept(File file)
				{
					return file.isDirectory() || file.getName().equals(invalidFile.getName());
				}
			});
			File validFile = null;
			while (validFile == null)
			{
				chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
				int response = chooser.showOpenDialog(null);
				if (response == JFileChooser.APPROVE_OPTION)
				{
					validFile = chooser.getSelectedFile();
				}
			}
			container.setFile(validFile);
		}

		// To finish, clear the list since all containers are fixed
		imageContainers.clear();
	}

	/**
	 * Asks the user to input the number of animals in an image
	 * 
	 * @return Integer.MAX_VALUE if the user closes the input box, or the number of animals the user wants
	 */
	public static Integer askUserForNumberAnimals()
	{
		Integer numberOfAnimals = Integer.MAX_VALUE;
		while (numberOfAnimals == Integer.MAX_VALUE)
		{
			try
			{
				String numberOfAnimalsString = JOptionPane.showInputDialog("How many animals of this species are in the image?");
				if (numberOfAnimalsString == null)
					return Integer.MAX_VALUE;
				numberOfAnimals = Integer.parseInt(numberOfAnimalsString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		return numberOfAnimals;
	}

	/**
	 * Asks the user to input a new species.
	 * 
	 * @return The species entered or "null" if nothing was entered
	 */
	public static Species askUserForNewSpecies()
	{
		String name = "";
		while (name.isEmpty())
		{
			name = JOptionPane.showInputDialog("Enter the name of the new species");
			if (name == null)
				return null;
		}
		return new Species(name);
	}

	/**
	 * Asks the user to input a new location.
	 * 
	 * @return The new location or "null" if nothing was entered
	 */
	public static Location askUserForNewLocation()
	{
		// Input the name
		String name = "";
		while (name.isEmpty())
		{
			name = JOptionPane.showInputDialog("Enter the name of the new location");
			if (name == null)
				return null;
		}

		// Input lat/lng or UTM
		Integer result = JOptionPane.showOptionDialog(null, "Are the coordinates in UTM or Latitude/Longitude?", "Coordinate System Type", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]
		{ "Latitude/Longitude", "UTM" }, "Latitude/Longitude");
		if (result == JOptionPane.CLOSED_OPTION)
			return null;
		Boolean useLatLng = result == JOptionPane.YES_OPTION;

		Double latitude = Double.MAX_VALUE;
		Double longitude = Double.MAX_VALUE;

		// If we're using lat/lng, just input the coordinates
		if (useLatLng)
		{
			// Input that latitude
			while (latitude > 85 || latitude < -85)
			{
				try
				{
					String latitudeString = JOptionPane.showInputDialog("Enter the latitude (+/- 85) of location '" + name + "'");
					if (latitudeString == null)
						return null;
					latitude = Double.parseDouble(latitudeString);
				}
				catch (NumberFormatException exception)
				{
				}
			}
			// Input the longitude
			while (longitude > 180 || longitude < -180)
			{
				try
				{
					String longitudeString = JOptionPane.showInputDialog("Enter the longitude (+/- 180) of location '" + name + "'");
					if (longitudeString == null)
						return null;
					longitude = Double.parseDouble(longitudeString);
				}
				catch (NumberFormatException exception)
				{
				}
			}
		}
		// If we're using UTM, input the letter, zone, Northing, and Easting
		else
		{
			// Input the letter
			Character letter = Character.MAX_VALUE;
			while (!Character.isLetter(letter) || ArrayUtils.contains(INVALID_UTM_LETTERS, Character.toUpperCase(letter)))
			{
				String letterString = JOptionPane.showInputDialog("Enter UTM Letter (C to X excluding I and O) of location '" + name + "'");
				if (letterString == null)
					return null;
				if (letterString.length() == 1)
					letter = letterString.charAt(0);
			}

			// Input the zone
			Integer zone = Integer.MAX_VALUE;
			while (zone < 1 || zone > 60)
			{
				try
				{
					String zoneString = JOptionPane.showInputDialog("Enter the UTM zone (1 to 60) of location '" + name + "'");
					if (zoneString == null)
						return null;
					zone = Integer.parseInt(zoneString);
				}
				catch (NumberFormatException exception)
				{
				}
			}

			// Input the easting value
			Double easting = Double.MAX_VALUE;
			while (easting > 1000000 || easting < 0)
			{
				try
				{
					String eastingString = JOptionPane.showInputDialog("Enter the UTM easting (0m to 1000000m) of location '" + name + "'");
					if (eastingString == null)
						return null;
					easting = Double.parseDouble(eastingString);
				}
				catch (NumberFormatException exception)
				{
				}
			}

			// Input the northing value
			Double northing = Double.MAX_VALUE;
			while (northing > 10000000 || northing < 0)
			{
				try
				{
					String northingString = JOptionPane.showInputDialog("Enter the UTM northing (0m to 10000000m) of location '" + name + "'");
					if (northingString == null)
						return null;
					northing = Double.parseDouble(northingString);
				}
				catch (NumberFormatException exception)
				{
				}
			}

			UTMCoord utmCoord = new UTMCoord(easting, northing, zone, letter);
			Double[] latLng = SanimalAnalysisUtils.UTM2Deg(utmCoord);
			latitude = latLng[0];
			longitude = latLng[1];
		}

		// Input lat/lng or UTM
		result = JOptionPane.showOptionDialog(null, "Is the elevation in feet or meters?", "Distance units", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]
		{ "Feet", "Meters" }, "Feet");
		if (result == JOptionPane.CLOSED_OPTION)
			return null;
		Boolean useFeet = result == JOptionPane.YES_OPTION;

		// Input the elevation
		Double elevation = Double.MAX_VALUE;
		while (elevation == Double.MAX_VALUE)
		{
			try
			{
				String elevationString = JOptionPane.showInputDialog("Enter the elevation of location '" + name + "'");
				if (elevationString == null)
					return null;
				elevation = Double.parseDouble(elevationString);
			}
			catch (NumberFormatException exception)
			{
			}
		}

		if (!useFeet)
			elevation = elevation * Constants.METERS2FEET;

		// Return the location
		return new Location(name, latitude, longitude, elevation);
	}
}
