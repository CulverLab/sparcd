package model.constant;

import fxmapcontrol.MapTileLayer;

/**
 * Utility enum with a list of possible map tile providers
 */
public enum MapProviders
{
	OpenStreetMaps("Open Street Map", "https://www.openstreetmap.org/", MapTileLayer.getOpenStreetMapLayer()),
	OpenTopoMap("Open Topo Map", "https://opentopomap.org/about", new MapTileLayer("OpenTopoMap", "https://{c}.tile.opentopomap.org/{z}/{x}/{y}.png", 0, 17)),
	EsriWorldStreetMap("Esri World Street Map", "https://www.esri.com/en-us/home", new MapTileLayer("EsriWorldStreetMap", "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}", 0, 19)),
	EsriWorldTopoMap("Esri World Topo Map", "https://www.esri.com/en-us/home", new MapTileLayer("EsriWorldTopoMap", "https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}", 0, 19)),
	EsriWorldImagery("Esri World Imagery", "https://www.esri.com/en-us/home", new MapTileLayer("EsriWorldImagery", "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", 0, 19));

	// The user-friendly name of the provider
	private String name;
	// The link to the website crediting this map tile providers
	private String creditURL;
	// The JavaFX map tile layer node
	private MapTileLayer mapTileProvider;

	/**
	 * Constructor just initializes fields
	 *
	 * @param name The user-friendly name of the provider
	 * @param creditURL The URL to the credits for this map tile layer
	 * @param mapTileProvider The JavaFX map tile layer node
	 */
	MapProviders(String name, String creditURL, MapTileLayer mapTileProvider)
	{
		this.name = name;
		this.creditURL = creditURL;
		this.mapTileProvider = mapTileProvider;
	}

	/**
	 * @return Just returns the name of the provider
	 */
	@Override
	public String toString()
	{
		return this.name;
	}

	/**
	 * @return A URL crediting this map tile provider
	 */
	public String getCreditURL()
	{
		return this.creditURL;
	}

	/**
	 * @return Returns the map tile provider for use on the map
	 */
	public MapTileLayer getMapTileProvider()
	{
		return this.mapTileProvider;
	}
}
