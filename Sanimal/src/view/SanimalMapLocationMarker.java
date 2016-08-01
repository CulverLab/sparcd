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

public class SanimalMapLocationMarker extends JLabel
{
	private static final ImageIcon BASE_ICON_NORMAL = new ImageIcon(SanimalMapLocationMarker.class.getResource("/images/marker.png"));
	private static final ImageIcon BASE_ICON_HIGHLIGHT = new ImageIcon(SanimalMapLocationMarker.class.getResource("/images/markerHighlight.png"));
	private static final Integer BASE_WIDTH_HEIGHT = 75;
	private ImageIcon normalIcon = BASE_ICON_NORMAL;
	private ImageIcon highlightedIcon = BASE_ICON_HIGHLIGHT;
	private boolean hovered = false;

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
		if (scale <= 1.0 && scale > 0.0)
		{
			normalIcon = new ImageIcon(BASE_ICON_NORMAL.getImage().getScaledInstance((int) Math.round(BASE_WIDTH_HEIGHT * scale), (int) Math.round(BASE_WIDTH_HEIGHT * scale), Image.SCALE_SMOOTH));
			highlightedIcon = new ImageIcon(BASE_ICON_HIGHLIGHT.getImage().getScaledInstance((int) Math.round(BASE_WIDTH_HEIGHT * scale), (int) Math.round(BASE_WIDTH_HEIGHT * scale), Image.SCALE_SMOOTH));
		}
		if (hovered)
			this.setIcon(normalIcon);
		else
			this.setIcon(highlightedIcon);
		this.setBounds((int) Math.round(this.getBounds().getX()), (int) Math.round(this.getBounds().getY()), (int) Math.round(BASE_WIDTH_HEIGHT * scale), (int) Math.round(BASE_WIDTH_HEIGHT * scale));
	}
}
