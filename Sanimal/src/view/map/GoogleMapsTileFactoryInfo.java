/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view.map;

import org.jxmapviewer.viewer.TileFactoryInfo;

public class GoogleMapsTileFactoryInfo extends TileFactoryInfo
{
	private static final int max = 22;

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
	 * Default constructor
	 */
	public GoogleMapsTileFactoryInfo(MapType mapType)
	{
		super("GoogleMaps", 1, max - 2, max, 256, true, true, // tile size is 256 and x/y orientation is normal
				"http://mt1.google.com/vt/lyrs=" + mapType.getTypeIdentifier(), "x", "y", "z"); // 5/15/10.png
	}

	@Override
	public String getTileUrl(int x, int y, int zoom)
	{
		zoom = max - zoom;
		String url = this.baseURL + "&x=" + x + "&y=" + y + "&z=" + zoom;
		return url;
	}
}
