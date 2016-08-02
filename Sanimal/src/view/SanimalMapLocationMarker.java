/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import model.ImageLoadingUtils;

public class SanimalMapLocationMarker extends JLabel
{
	private static final ImageIcon BASE_ICON_NORMAL = new ImageIcon(SanimalMapLocationMarker.class.getResource("/images/marker2.png"));
	private static final ImageIcon BASE_ICON_HIGHLIGHT = new ImageIcon(SanimalMapLocationMarker.class.getResource("/images/markerHighlight2.png"));
	private static final Integer BASE_WIDTH_HEIGHT = 75;
	private ImageIcon normalIcon = BASE_ICON_NORMAL;
	private ImageIcon highlightedIcon = BASE_ICON_HIGHLIGHT;
	private boolean hovered = false;
	private Integer originalX = 0;
	private Integer originalY = 0;
	private Double prevScale = 1.0;

	public SanimalMapLocationMarker()
	{
		super();
		this.setLayout(null);
		this.setBounds(0, 0, BASE_WIDTH_HEIGHT, BASE_WIDTH_HEIGHT);
		this.updateIconsByScale(1.0);
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
				SanimalMapLocationMarker.this.setIcon(normalIcon);
				hovered = false;
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				SanimalMapLocationMarker.this.setIcon(highlightedIcon);
				hovered = true;
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
			}
		});
		this.setVisible(true);
	}

	public void updateIconsByScale(double scale)
	{
		if (scale <= 1.0 && scale > 0.1)
		{
			normalIcon = ImageLoadingUtils.resizeImageIcon(BASE_ICON_NORMAL, (int) Math.round(BASE_WIDTH_HEIGHT * scale), (int) Math.round(BASE_WIDTH_HEIGHT * scale), Image.SCALE_SMOOTH, true);
			highlightedIcon = ImageLoadingUtils.resizeImageIcon(BASE_ICON_HIGHLIGHT, (int) Math.round(BASE_WIDTH_HEIGHT * scale), (int) Math.round(BASE_WIDTH_HEIGHT * scale), Image.SCALE_SMOOTH, true);

			if (hovered)
				this.setIcon(highlightedIcon);
			else
				this.setIcon(normalIcon);
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
