/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view.map;

import model.Location;
import view.SanimalMapMarker;

public class SanimalMapMarkerOverlay extends SwingComponentOverlay
{
	private Location location;

	public SanimalMapMarkerOverlay(Location coord, SanimalMapMarker marker)
	{
		super(coord.toGeoPosition(), marker);
	}

	@Override
	public SanimalMapMarker getComponent()
	{
		return (SanimalMapMarker) this.component;
	}

	public Location getLocation()
	{
		return location;
	}
}
