/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class SanimalMapMarker extends JPanel
{
	private static final Integer BASE_WIDTH_HEIGHT = 300;
	private List<SanimalMapImageMarker> imageMarkers = new ArrayList<SanimalMapImageMarker>();
	private SanimalMapLocationMarker centerMarker;

	public SanimalMapMarker()
	{
		super();
		this.setLayout(null);
		this.setBounds(0, 0, BASE_WIDTH_HEIGHT, BASE_WIDTH_HEIGHT);

		centerMarker = new SanimalMapLocationMarker();
		centerMarker.setLocation(BASE_WIDTH_HEIGHT / 2 - centerMarker.getWidth() / 2, BASE_WIDTH_HEIGHT / 2 - centerMarker.getHeight() / 2);
		this.add(centerMarker);
	}

	public void clearMarkers()
	{
		for (SanimalMapImageMarker marker : imageMarkers)
			this.remove(marker);
		imageMarkers.clear();
	}

	public void addMarker(SanimalMapImageMarker marker)
	{
		this.add(marker);
		this.imageMarkers.add(marker);
	}

	public void refreshLayout()
	{
		Integer numMarkers = imageMarkers.size();
		Double temp = 360.0 / numMarkers;
		for (int i = 0; i < numMarkers; i++)
		{
			SanimalMapImageMarker current = imageMarkers.get(i);
			Double degrees = i * temp;
			Integer newX = (int) Math.round(BASE_WIDTH_HEIGHT / 2 - current.getWidth() / 2 + BASE_WIDTH_HEIGHT / 3 * Math.sin(Math.toRadians(degrees)));
			Integer newY = (int) Math.round(BASE_WIDTH_HEIGHT / 2 - current.getHeight() / 2 + BASE_WIDTH_HEIGHT / 3 * -Math.cos(Math.toRadians(degrees)));
			current.setLocation(newX, newY);
		}
	}

	public void updateIconsByScale(double scale)
	{
		//this.centerMarker.updateIconsByScale(scale);
	}

	public SanimalMapLocationMarker getCenterMarker()
	{
		return centerMarker;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		//super.paintComponent(g);
	}
}
