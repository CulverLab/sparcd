package controller;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import model.SanimalData;
import model.location.Location;
import netscape.javascript.JSObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller class for the map page
 */
public class SanimalMapController implements Initializable
{
	///
	/// FXML bound fields start
	///

	@FXML
	public GoogleMapView googleMapView;

	///
	/// FXML bound fields end
	///

	// The map data which will be bound to the google map view
	private GoogleMap googleMap;

	// A map of location to the marker representing that location
	private Map<Location, Marker> locationMarkers = new HashMap<>();

	/**
	 * Initialize sets up the analysis window and bindings
	 *
	 * @param location ignored
	 * @param resources ignored
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// Setup the google map view with the map options class
		this.googleMapView.addMapInializedListener(() ->
		{
			// Create a map options class. Center it on tucson
			LatLong tucson = new LatLong(32.2217, -110.9265);
			MapOptions mapOptions = new MapOptions();
			// Set use satellite map view, allow map control, scale control, rotate control, but NO street view
			mapOptions
					.center(tucson)
					.mapType(MapTypeIdEnum.SATELLITE)
					.overviewMapControl(true)
					.panControl(true)
					.rotateControl(true)
					.scaleControl(true)
					.streetViewControl(false)
					.scrollWheel(true)
					.zoomControl(true)
					.zoom(12);

			// Initialize the google map
			this.googleMap = this.googleMapView.createMap(mapOptions);

			// When the location list changes, we put the locations onto the map display
			SanimalData.getInstance().getLocationList().addListener((ListChangeListener<Location>) c -> {
				// Iterate over changes
				while (c.next())
				{
					// If the item was updated
					if (c.wasUpdated())
					{
						// Loop over updated items
						for (int i = c.getFrom(); i < c.getTo(); ++i)
						{
							// Get the updated location
							Location changed = c.getList().get(i);
							// This also removes the old marker!
							this.addMarkerForLocation(changed);
						}
					}
					// If the item was removed
					else if (c.wasRemoved())
					{
						// Remove each of the removed location's markers from the map
						c.getRemoved().forEach(removedLoc -> {
							if (locationMarkers.containsKey(removedLoc))
								this.googleMap.removeMarker(locationMarkers.remove(removedLoc));
						});
					}
					// If the item was added
					else if (c.wasAdded())
					{
						// Add a marker for each new location element
						c.getAddedSubList().forEach(this::addMarkerForLocation);
					}
				}
			});

			///
			/// Everything after this is temporary for exploring map capabilities
			///

			/*


			// Create a test polygon
			double startBearing = 0;
			double endBearing = 30;
			double radius = 30000;

			MVCArray path = ArcBuilder.buildArcPoints(tucson, startBearing, endBearing, radius);
			path.push(tucson);

			Polygon arc = new Polygon(new PolygonOptions()
					.paths(path)
					.strokeColor("blue")
					.fillColor("lightBlue")
					.fillOpacity(0.3)
					.strokeWeight(2)
					.editable(false));

			// Add the test polygon
			this.googleMap.addMapShape(arc);
			this.googleMap.addUIEventHandler(arc, UIEventType.click, (JSObject obj) -> {
				arc.setEditable(!arc.getEditable());
			});

			*/
		});
	}

	/**
	 * Removes the current marker for the location if it exists, and adds a new one
	 *
	 * @param location The location to add/update a marker for
	 */
	private void addMarkerForLocation(Location location)
	{
		// If we already have a marker for the location, remove it
		if (locationMarkers.containsKey(location))
			this.googleMap.removeMarker(locationMarkers.remove(location));
		// Create the marker options which defines the title and position
		MarkerOptions options = new MarkerOptions()
				.title(location.getName())
				.position(new LatLong(location.getLat(), location.getLng()));
		Marker marker = new Marker(options);
		// Add a marker to the map
		this.googleMap.addMarker(marker);
		// Create an info window that shows the location details when opened
		InfoWindowOptions infoWindowOptions = new InfoWindowOptions()
				.content(location.getName())
				.position(new LatLong(location.getLat(), location.getLng()));
		InfoWindow infoWindow = new InfoWindow(infoWindowOptions);
		// When we click the map marker, open the info window
		this.googleMap.addUIEventHandler(marker, UIEventType.click, (JSObject obj) -> {
			infoWindow.open(googleMap, marker);
		});
		// Add the marker reference to the map
		locationMarkers.put(location, marker);
	}
}
