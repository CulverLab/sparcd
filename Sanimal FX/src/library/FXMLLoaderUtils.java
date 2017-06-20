package library;

import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class FXMLLoaderUtils
{
	public static FXMLLoader loadFXML(String FXMLFileName)
	{
		FXMLLoader loader = new FXMLLoader(FXMLLoaderUtils.class.getResource("/view/" + FXMLFileName));

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
		return loader;
	}
}
