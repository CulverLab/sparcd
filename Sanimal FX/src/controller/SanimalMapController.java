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

public class SanimalMapController implements Initializable
{
	@FXML
	public GoogleMapView googleMapView;

	private GoogleMap googleMap;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.googleMapView.addMapInializedListener(() ->
		{
			MapOptions mapOptions = new MapOptions();
			mapOptions
					.center(new LatLong(32.272951, -110.836367))
					.mapType(MapTypeIdEnum.SATELLITE)
					.overviewMapControl(true)
					.panControl(true)
					.rotateControl(true)
					.scaleControl(true)
					.streetViewControl(false)
					.scrollWheel(true)
					.zoomControl(true)
					.zoom(12);

			this.googleMap = this.googleMapView.createMap(mapOptions);

			MarkerOptions markerOptions = new MarkerOptions();

			markerOptions
					.position(new LatLong(32.272951, -110.836367))
					.visible(true)
					.title("My House!");

			Marker marker = new Marker(markerOptions);

			this.googleMap.addMarker(marker);

			InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
			infoWindowOptions.content("Test123").position(new LatLong(32.272951, -110.836367));
			InfoWindow testWindow = new InfoWindow(infoWindowOptions);
			testWindow.open(googleMap, marker);



			LatLong arcC = new LatLong(32.272951, -110.836367);
			double startBearing = 0;
			double endBearing = 30;
			double radius = 30000;

			MVCArray path = ArcBuilder.buildArcPoints(arcC, startBearing, endBearing, radius);
			path.push(arcC);

			Polygon arc = new Polygon(new PolygonOptions()
					.paths(path)
					.strokeColor("blue")
					.fillColor("lightBlue")
					.fillOpacity(0.3)
					.strokeWeight(2)
					.editable(false));

			this.googleMap.addMapShape(arc);
			this.googleMap.addUIEventHandler(arc, UIEventType.click, (JSObject obj) -> {
				arc.setEditable(!arc.getEditable());
			});
		});
	}
}
