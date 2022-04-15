package model.util;

import javafx.scene.control.Alert;
import model.SanimalData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class used in managing temporary files created by SANIMAL
 */
public class TempDirectoryManager
{
	// The temporary folder to put all temporary sanimal files into
	private File sanimalTempDir;

	/**
	 * Constructor initializes the temporary directory
	 */
	public TempDirectoryManager(ErrorDisplay errorDisplay)
	{
		try
		{
			// We need to delete it after exiting!
			this.sanimalTempDir = Files.createTempDirectory("Sanimal").toFile();
			this.sanimalTempDir.deleteOnExit();
		}
		catch (IOException e)
		{
			errorDisplay.showPopup(
					Alert.AlertType.ERROR,
					null,
					"Error",
					"Directory error",
					"Error creating a temporary SANIMAL directory!\n" + ExceptionUtils.getStackTrace(e),
					false);
		}
	}

	/**
	 * The method that creates a new temp file
	 *
	 * @param fileName The name of the file to create
	 * @return A reference to the temporary file we created
	 */
	public File createTempFile(String fileName)
	{
		// The temporary file will have the temp directory as a parent and the same name except with 10 random alphanumeric characters tagged onto the end
		File tempFile = FileUtils.getFile(this.sanimalTempDir, FilenameUtils.getBaseName(fileName) + RandomStringUtils.randomAlphanumeric(10) + "." + FilenameUtils.getExtension(fileName));
		// If it exists, try again to ensure we get a unique file
		while (tempFile.exists())
			tempFile = FileUtils.getFile(this.sanimalTempDir, FilenameUtils.getBaseName(fileName) + RandomStringUtils.randomAlphanumeric(10) + "." + FilenameUtils.getExtension(fileName));

		// Delete the file when we exit
		tempFile.deleteOnExit();
		return tempFile;
	}

	/**
	 * The method that create a new temporary folder
	 * 
	 * @param folderNane The name of the folder to create
	 * @return A reference to the temporary folder we created
	 * @throws IOException If a problem ocurrs when accessing the folder
	 */
	public File createTempFolder(String folderName) throws IOException
	{
		// The temporary folder will have the temp directory as a parent and the same name except with 10 random alphanumeric characters tagged onto the end
		String tempPath = String.join("/", this.sanimalTempDir.getAbsolutePath(), folderName + RandomStringUtils.randomAlphanumeric(10));
		File tempDir = Files.createDirectory(Paths.get(tempPath)).toFile();

		// Delete the file when we exit
		tempDir.deleteOnExit();
		return tempDir;
	}
}
