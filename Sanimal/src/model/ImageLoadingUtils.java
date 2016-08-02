/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package model;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;

public class ImageLoadingUtils
{
	public static ImageIcon createImageIcon(File path, Integer width, Integer height, Integer imageResizeAlgorithm)
	{
		Image image = Toolkit.getDefaultToolkit().getImage(path.getAbsolutePath());
		ImageIcon toReturn = new ImageIcon(image.getScaledInstance(width, height, imageResizeAlgorithm));
		image.flush();
		return toReturn;
	}

	public static ImageIcon resizeImageIcon(ImageIcon other, Integer width, Integer height, Integer imageResizeAlgorithm, Boolean preserveOriginal)
	{
		Image image = other.getImage();
		ImageIcon toReturn = new ImageIcon(image.getScaledInstance(width, height, imageResizeAlgorithm));
		if (!preserveOriginal)
			image.flush();
		return toReturn;
	}
}
