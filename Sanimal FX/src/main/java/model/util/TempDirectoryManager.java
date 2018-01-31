package model.util;

import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TempDirectoryManager
{
	private File sanimalTempDir;

	public TempDirectoryManager()
	{
		try
		{
			this.sanimalTempDir = Files.createTempDirectory("Sanimal").toFile();
			this.sanimalTempDir.deleteOnExit();
		}
		catch (IOException e)
		{
			System.err.println("Error creating a temporary Sanimal directory!");
			e.printStackTrace();
		}
	}

	public File createTempFile(String fileName)
	{
		File tempFile = FileUtils.getFile(this.sanimalTempDir, FilenameUtils.getBaseName(fileName) + RandomStringUtils.randomAlphanumeric(10) + "." + FilenameUtils.getExtension(fileName));
		while (tempFile.exists())
			tempFile = FileUtils.getFile(this.sanimalTempDir, FilenameUtils.getBaseName(fileName) + RandomStringUtils.randomAlphanumeric(10) + "." + FilenameUtils.getExtension(fileName));

		tempFile.deleteOnExit();
		return tempFile;
	}
}
