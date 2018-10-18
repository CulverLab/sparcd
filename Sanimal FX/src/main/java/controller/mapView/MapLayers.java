package controller.mapView;

/**
 * An enum with a set of map layers and their respective Z-layer
 */
public enum MapLayers
{
	TILE_PROVIDER(0),
	LOCATION_PINS(1);

	// The z-layer
	private Integer zLayer;

	/**
	 * Constructor just sets the z-layer field
	 *
	 * @param zLayer The map z-layer
	 */
	MapLayers(Integer zLayer)
	{
		this.zLayer = zLayer;
	}

	/**
	 * @return The Z layer that this enum represents
	 */
	public Integer getZLayer()
	{
		return this.zLayer;
	}
}
