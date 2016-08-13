package view.map;

import javax.swing.JComponent;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Class to draw a swing component onto the map
 * 
 * @author David Slovikosky
 */
public class SwingComponentOverlay extends DefaultWaypoint
{
	// The component to draw
	protected final JComponent component;

	/**
	 * Constructor for the swing component
	 * 
	 * @param coord
	 *            The coordinate to draw at
	 * @param component
	 *            The component to draw
	 */
	public SwingComponentOverlay(GeoPosition coord, JComponent component)
	{
		super(coord);
		this.component = component;
	}

	/**
	 * @return The drawn component
	 */
	public JComponent getComponent()
	{
		return this.component;
	}
}
