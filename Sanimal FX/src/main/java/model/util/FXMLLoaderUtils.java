package model.util;

import controller.Sanimal;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

/**
 * Utility class for loading FXML files
 */
public class FXMLLoaderUtils
{
	/**
	 * Given a file name of a file in ./view/<FILENAME> this function loads it
	 *
	 * @param FXMLFileName The file name in /view/<FILENAME> to load
	 * @return The FXMLLoader representing that FXML file
	 */
	public static FXMLLoader loadFXML(String FXMLFileName)
	{
		// Create the loader
		FXMLLoader loader = new FXMLLoader(Sanimal.class.getClass().getResource("/view/" + FXMLFileName));

		// Attempt to load the file. If we get an error throw an exception
		try
		{
			loader.load();
		}
		catch (IOException exception)
		{
			System.err.println("Could not load the FXML file for the file " + FXMLFileName + "!");
			exception.printStackTrace();
			System.exit(-1);
		}

		// Return the result
		return loader;
	}
}
