package view.map;

import model.Location;
import view.SanimalMapMarker;

/**
 * Map Marker representing a location. This is not a swing component but a map "waypoint"
 * 
 * @author David Slovikosky
 */
public class SanimalMapMarkerOverlay extends SwingComponentOverlay
{
	private Location location;

	/**
	 * The constructor for the overlay
	 * 
	 * @param coord
	 *            The coordinate of the overlay
	 * @param marker
	 *            The marker to add
	 */
	public SanimalMapMarkerOverlay(Location coord, SanimalMapMarker marker)
	{
		super(coord.toGeoPosition(), marker);
	}

	/**
	 * Return the swing component
	 */
	@Override
	public SanimalMapMarker getComponent()
	{
		return (SanimalMapMarker) this.component;
	}

	/**
	 * @return This marker's location
	 */
	public Location getLocation()
	{
		return location;
	}
}
