package model.util;

import controller.Sanimal;
import javafx.fxml.FXMLLoader;
import model.SanimalData;
import org.apache.commons.lang.exception.ExceptionUtils;

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
			SanimalData.getInstance().getErrorDisplay().printError("Could not load the FXML file for the file " + FXMLFileName + "!\n" + ExceptionUtils.getStackTrace(exception));
			System.exit(-1);
		}

		// Return the result
		return loader;
	}
}
