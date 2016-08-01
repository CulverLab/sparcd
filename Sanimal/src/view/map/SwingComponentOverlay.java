/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view.map;

import javax.swing.JComponent;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

public class SwingComponentOverlay extends DefaultWaypoint
{
	protected final JComponent component;

	public SwingComponentOverlay(GeoPosition coord, JComponent component)
	{
		super(coord);
		this.component = component;
	}

	public JComponent getComponent()
	{
		return this.component;
	}
}
