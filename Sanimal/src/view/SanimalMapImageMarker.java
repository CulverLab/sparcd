/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view;

import java.awt.Image;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import model.ImageEntry;

public class SanimalMapImageMarker extends JLabel
{
	private static final Integer BASE_WIDTH_HEIGHT = 75;

	public SanimalMapImageMarker(ImageEntry image)
	{
		super();
		this.setLayout(null);
		this.setBounds(0, 0, BASE_WIDTH_HEIGHT, BASE_WIDTH_HEIGHT);
		File file = image.getImageFile();
		if (file.exists())
		{
			this.setIcon(new ImageIcon(new ImageIcon(file.getAbsolutePath()).getImage().getScaledInstance(BASE_WIDTH_HEIGHT, BASE_WIDTH_HEIGHT, Image.SCALE_FAST)));
		}
	}
}
