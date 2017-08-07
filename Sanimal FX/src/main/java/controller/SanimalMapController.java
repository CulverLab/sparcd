package controller;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.shapes.ArcBuilder;
import com.lynden.gmapsfx.shapes.Polygon;
import com.lynden.gmapsfx.shapes.PolygonOptions;
import com.sun.prism.PhongMaterial;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import netscape.javascript.JSObject;

import java.net.URL;
import java.sql.Connection;
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
			LatLong tucson = new LatLong(32.2217, 110.9265);
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

			///
			/// Everything after this is temporary for exploring map capabilities
			///

			// Create a temporary marker to test with
			MarkerOptions markerOptions = new MarkerOptions();

			// Set the position to tucson
			markerOptions
					.position(tucson)
					.visible(true)
					.title("Test option!");

			// Create the marker and add it to the map
			Marker marker = new Marker(markerOptions);

			this.googleMap.addMarker(marker);

			// Create a test window
			InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
			infoWindowOptions.content("Test Point").position(tucson);
			InfoWindow testWindow = new InfoWindow(infoWindowOptions);
			testWindow.open(googleMap, marker);

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
		});
	}
}
