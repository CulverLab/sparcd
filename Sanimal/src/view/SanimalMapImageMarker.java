/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.commons.lang3.StringUtils;

import model.ImageEntry;
import model.ImageLoadingUtils;

public class SanimalMapImageMarker extends JLabel
{
	private static final Integer BASE_WIDTH_HEIGHT = 75;
	private static final ImageIcon DEFAULT_ICON = ImageLoadingUtils.createImageIcon(new File(SanimalMapImageMarker.class.getResource("/images/loadingImageIcon.png").getFile()), BASE_WIDTH_HEIGHT, BASE_WIDTH_HEIGHT, Image.SCALE_SMOOTH);
	private boolean needsIcon = true;
	private final File file;
	private Double prevScale = 1.0;

	public SanimalMapImageMarker(ImageEntry image)
	{
		super();
		this.setLayout(null);
		this.setBounds(0, 0, BASE_WIDTH_HEIGHT, BASE_WIDTH_HEIGHT);
		this.file = image.getImageFile();

		this.setToolTipText(StringUtils.join(image.getSpeciesPresent(), ", "));

		if (file.exists())
		{
			this.setIcon(DEFAULT_ICON);
			SanimalIconLoader.getInstance().scheduleTask(() ->
			{
				if (needsIcon)
				{
					this.setIcon(ImageLoadingUtils.createImageIcon(file, BASE_WIDTH_HEIGHT, BASE_WIDTH_HEIGHT, Image.SCALE_SMOOTH));
				}
			});
		}
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));
		this.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
			}
		});
	}

	public void noLongerNeedsIcon()
	{
		this.needsIcon = false;
	}

	public void updateIconsByScale(double scale)
	{
		if (scale <= 1.0 && scale > 0.1)
		{
			SanimalIconLoader.getInstance().scheduleTask(() ->
			{
				if (needsIcon)
				{
					this.setIcon(ImageLoadingUtils.createImageIcon(file, (int) Math.round(BASE_WIDTH_HEIGHT * scale), (int) Math.round(BASE_WIDTH_HEIGHT * scale), Image.SCALE_FAST));
				}
			});
			Integer originalX = (int) Math.round(1.0 / prevScale * this.getX());
			Integer originalY = (int) Math.round(1.0 / prevScale * this.getY());
			this.setBounds((int) Math.round(originalX * scale), (int) Math.round(originalY * scale), (int) Math.round(BASE_WIDTH_HEIGHT * scale), (int) Math.round(BASE_WIDTH_HEIGHT * scale));
			prevScale = scale;
			this.setVisible(true);
		}
		else
		{
			this.setVisible(false);
		}
	}
}
