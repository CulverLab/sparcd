package view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class SanimalMapMarker extends JComponent
{
	private static final Integer BASE_WIDTH_HEIGHT = 300;
	private List<SanimalMapImageMarker> imageMarkers = new ArrayList<SanimalMapImageMarker>();
	private SanimalMapLocationMarker centerMarker;
	private Double prevScale = 1.0;

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
		{
			marker.noLongerNeedsIcon();
			this.remove(marker);
		}
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
		if (scale <= 1.0 && scale > 0.1)
		{
			// Sanimal map scale
			Integer originalX = (int) Math.round(1.0 / prevScale * this.getX());
			Integer originalY = (int) Math.round(1.0 / prevScale * this.getY());
			this.setBounds((int) Math.round(originalX * scale), (int) Math.round(originalY * scale), (int) Math.round(BASE_WIDTH_HEIGHT * scale), (int) Math.round(BASE_WIDTH_HEIGHT * scale));

			// Update center marker 
			this.centerMarker.updateIconsByScale(scale);

			// Update icon sizes
			for (SanimalMapImageMarker imageMarker : imageMarkers)
				imageMarker.updateIconsByScale(scale);

			prevScale = scale;
			this.setVisible(true);
		}
		else
		{
			this.setVisible(false);
		}
	}

	public SanimalMapLocationMarker getCenterMarker()
	{
		return centerMarker;
	}
}
