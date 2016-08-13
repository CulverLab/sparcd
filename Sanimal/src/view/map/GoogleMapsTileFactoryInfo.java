package view.map;

import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 * Tile factory used to receive google maps tiles
 * 
 * @author David Slovikosky
 */
public class GoogleMapsTileFactoryInfo extends TileFactoryInfo
{
	private static final int max = 22;

	/**
	 * Map types provided by google maps. Each is represented by a single letter
	 * 
	 * @author David Slovikosky
	 */
	public enum MapType
	{
		ROADS_ONLY("h"),
		STANDARD_ROADMAP("m"),
		TERRAIN("p"),
		ALTERED_ROADMAP("r"),
		TERRAIN_ONLY("t"),
		HYBRID("y");

		private String typeIdentifier;

		private MapType(String typeIdentifier)
		{
			this.typeIdentifier = typeIdentifier;
		}

		public String getTypeIdentifier()
		{
			return typeIdentifier;
		}
	}

	/**
	 * Constructor for the tile factory
	 * 
	 * @param mapType
	 *            The type of map to pull data
	 */
	public GoogleMapsTileFactoryInfo(MapType mapType)
	{
		super("GoogleMaps", 1, max - 2, max, 256, true, true, // tile size is 256 and x/y orientation is normal
				"http://mt1.google.com/vt/lyrs=" + mapType.getTypeIdentifier(), "x", "y", "z"); // 5/15/10.png
	}

	/**
	 * Returns the url with which to get the tile at x, y, z
	 */
	@Override
	public String getTileUrl(int x, int y, int zoom)
	{
		zoom = max - zoom;
		String url = this.baseURL + "&x=" + x + "&y=" + y + "&z=" + zoom;
		return url;
	}
}
