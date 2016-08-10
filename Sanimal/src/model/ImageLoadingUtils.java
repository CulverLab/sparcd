package model;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;

/**
 * A utility class to load images and free memory
 * 
 * @author David Slovikosky
 */
public class ImageLoadingUtils
{
	/**
	 * Creates a scaled image icon from a given path and frees the memory
	 * 
	 * @param path
	 *            The path to the image file
	 * @param width
	 *            The width to scale the image to
	 * @param height
	 *            The Height to scale the image to
	 * @param imageResizeAlgorithm
	 *            The resize algorithm (Image.Algorithm)
	 * @return A scaled image icon
	 */
	public static ImageIcon createImageIcon(File path, Integer width, Integer height, Integer imageResizeAlgorithm)
	{
		Image image = Toolkit.getDefaultToolkit().getImage(path.getAbsolutePath());
		ImageIcon toReturn = new ImageIcon(image.getScaledInstance(width, height, imageResizeAlgorithm));
		image.flush();
		return toReturn;
	}

	/**
	 * Creates a scaled image icon from a given other image icon and frees the memory if asked to
	 * 
	 * @param other
	 *            The image icon to get the original image from
	 * @param width
	 *            The width to scale the image to
	 * @param height
	 *            The Height to scale the image to
	 * @param imageResizeAlgorithm
	 *            The resize algorithm (Image.Algorithm)
	 * @param preserveOriginal
	 *            If false, this will free the memory associated with "other"
	 * @return A scaled image icon
	 */
	public static ImageIcon resizeImageIcon(ImageIcon other, Integer width, Integer height, Integer imageResizeAlgorithm, Boolean preserveOriginal)
	{
		Image image = other.getImage();
		ImageIcon toReturn = new ImageIcon(image.getScaledInstance(width, height, imageResizeAlgorithm));
		if (!preserveOriginal)
			image.flush();
		return toReturn;
	}
}
