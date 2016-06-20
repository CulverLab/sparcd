/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ImageData
{
	private List<ImageEntry> images = new ArrayList<ImageEntry>();

	public List<ImageEntry> getImages()
	{
		return images;
	}

	public void readAndAddImages(File location, boolean recursive)
	{
		this.addImages(this.readImages(location, recursive));
	}

	private void addImages(List<File> images)
	{
		this.images = new ArrayList<ImageEntry>();
		for (File file : images)
			if (!this.listContainsImage(file))
				this.images.add(new ImageEntry(file));
	}

	private boolean listContainsImage(File image)
	{
		for (ImageEntry entry : this.images)
			if (entry.getImagePath().getAbsolutePath().equals(image.getAbsolutePath()))
				return true;
		return false;
	}

	private List<File> readImages(File location, boolean recursive)
	{
		List<File> resultList = readFiles(location, recursive);

		Iterator<File> iterator = resultList.iterator();
		while (iterator.hasNext())
		{
			File next = iterator.next();
			String result = null;
			try
			{
				result = Files.probeContentType(next.toPath());
			}
			catch (IOException e)
			{
			}
			if (result == null || !result.startsWith("image"))
				iterator.remove();
		}

		return resultList;
	}

	private List<File> readFiles(File location, boolean includeSubdirectories)
	{
		List<File> resultList = new ArrayList<File>();

		if (location.isDirectory())
		{
			File[] subfiles = location.listFiles();
			resultList.addAll(Arrays.<File> asList(subfiles));
			if (includeSubdirectories)
				for (File file : subfiles)
					resultList.addAll(readFiles(file, includeSubdirectories));
		}
		else
		{
			resultList.add(location);
		}

		return resultList;
	}
}
