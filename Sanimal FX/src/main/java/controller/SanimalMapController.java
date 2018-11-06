package controller;

import controller.mapView.LayeredMap;
import controller.mapView.LocationPopOverController;
import controller.mapView.MapLayers;
import fxmapcontrol.ImageFileCache;
import fxmapcontrol.MapNode;
import fxmapcontrol.MapTileLayer;
import fxmapcontrol.TileImageLoader;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import library.AlignedMapNode;
import model.SanimalData;
import model.analysis.SanimalAnalysisUtils;
import model.constant.MapProviders;
import model.location.Location;
import model.location.UTMCoord;
import model.util.FXMLLoaderUtils;
import model.util.RoundingUtils;
import model.util.SettingsData;
import org.controlsfx.control.HyperlinkLabel;
import org.controlsfx.control.PopOver;
import org.fxmisc.easybind.EasyBind;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller class for the map page
 */
public class SanimalMapController
{
	///
	/// FXML bound fields start
	///

	// The primary map object to display sites and images on
	@FXML
	public LayeredMap map;

	// A box containing any map specific settings (not query filters)
	@FXML
	public GridPane gpnMapSettings;

	// A combo-box of possible map providers like OSM or Esri
	@FXML
	public ComboBox<MapProviders> cbxMapProvider;

	// The label containing any map credits
	@FXML
	public HyperlinkLabel lblMapCredits;

	// The map scale label
	@FXML
	public Label lblScale;
	// The map scale box
	@FXML
	public HBox hbxScale;

	// Current mouse location latitude and longitude
	@FXML
	public Label lblMouseLocation;

	///
	/// FXML bound fields end
	///

	/**
	 * Initialize sets up the analysis window and bindings
	 */
	public void initialize()
	{
		///
		/// Store image tiles inside of the user's home directory
		///

		TileImageLoader.setCache(new ImageFileCache(SanimalData.getInstance().getTempDirectoryManager().createTempFile("SanimalMapCache").toPath()));//new File(System.getProperty("user.home") + File.separator + "SanimalMapCache").toPath()));

		///
		/// Setup the tile providers
		///

		// Add the default tile layer to the background, use OpenStreetMap by default
		this.map.addChild(MapProviders.OpenStreetMaps.getMapTileProvider(), MapLayers.TILE_PROVIDER);
		// Setup our map provider combobox, first set the items to be an unmodifiable list of enums
		this.cbxMapProvider.setItems(FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(MapProviders.values())));
		// Select OSM as the default map provider
		this.cbxMapProvider.getSelectionModel().select(MapProviders.OpenStreetMaps);
		// When we select a new map provider, swap tile providers
		this.cbxMapProvider.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			// This should always be true...
			if (newValue != null && oldValue != null)
			{
				// Grab the old and new tile providers
				MapTileLayer oldMapTileProvider = oldValue.getMapTileProvider();
				MapTileLayer newMapTileProvider = newValue.getMapTileProvider();
				// Remove the old provider, add the new one
				this.map.removeChild(oldMapTileProvider);
				this.map.addChild(newMapTileProvider, MapLayers.TILE_PROVIDER);
			}
		});
		// Update the credits label whenever the map provider changes
		this.lblMapCredits.textProperty().bind(EasyBind.monadic(this.cbxMapProvider.getSelectionModel().selectedItemProperty()).map(mapProvider -> "Map tiles by [" + mapProvider.toString() + "]"));
		// Update the action to reflect the currently selected map provider
		this.lblMapCredits.onActionProperty().bind(EasyBind.monadic(this.cbxMapProvider.getSelectionModel().selectedItemProperty()).map(mapProvider -> event -> { try { Desktop.getDesktop().browse(new URI(mapProvider.getCreditURL())); } catch (URISyntaxException | IOException ignored) {} }));

		///
		/// Setup the map scale indicator on the bottom left
		///

		EasyBind.subscribe(this.map.zoomLevelProperty(), newValue ->
		{
			// The minimum size in pixels of the scale in the bottom left
			final double MIN_SIZE = 100;
			// The maximum size in pixels of the scale in the bottom left
			final double MAX_SIZE = 300;
			// Pixels per meter to start with here
			double pixelsPerPowerOf10 = this.map.getProjection().getMapScale(this.map.getCenter()).getY();
			// Iterate up to 25 times (or 10^25)
			for (int currentPowerOf10 = 0; currentPowerOf10 < 25; currentPowerOf10++)
			{
				// If the pixels per meter is greater than the minimum, we stop and draw it at that size
				if (pixelsPerPowerOf10 > MIN_SIZE)
				{
					// Compute the scale based on the power of 10
					long scale = Math.round(Math.pow(10, currentPowerOf10));
					// Test if we want it to use KM or M based on the power of 10.
					boolean willUseKM = currentPowerOf10 > 3;

					// If the pixels per power of 10 is bigger than our max size, draw it at 1/2 size
					if (pixelsPerPowerOf10 < MAX_SIZE)
					{
						// Set the text based on if it's KM or M
						this.lblScale.setText(willUseKM ? Long.toString(scale / 1000) + " km" : Long.toString(scale) + " m");
						// Force the HBox width
						this.hbxScale.setMinWidth(pixelsPerPowerOf10);
						this.hbxScale.setMaxWidth(pixelsPerPowerOf10);
					}
					else
					{
						// Set the text based on if it's KM or M. We divide by 2 to ensure it's not too large
						this.lblScale.setText(willUseKM ? Long.toString(scale / 2000) + " km" : Long.toString(scale / 2) + " m");
						// Force the HBox width
						this.hbxScale.setMinWidth(pixelsPerPowerOf10 / 2);
						this.hbxScale.setMaxWidth(pixelsPerPowerOf10 / 2);
					}
					return;
				}
				// Increment pixels by a power of 10
				pixelsPerPowerOf10 = pixelsPerPowerOf10 * 10;
			}
		});

		///
		/// Setup the pop-over which is shown if a site pin is clicked
		///

		// Add a popover that we use to display location specifics
		PopOver popOver = new PopOver();
		popOver.setHeaderAlwaysVisible(false);
		popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_CENTER);
		popOver.setArrowSize(20);
		popOver.setCloseButtonEnabled(true);
		// Load the content of the popover from the FXML file once and store it
		FXMLLoader fxmlLoader = FXMLLoaderUtils.loadFXML("mapView/LocationPopOver.fxml");
		// Grab the controller for use later
		LocationPopOverController locationPopOverController = fxmlLoader.getController();
		// Store the content into the popover
		popOver.setContentNode(fxmlLoader.getRoot());

		///
		/// Setup the location icons
		///

		// Mapping of location to map node pins
		Map<Location, MapNode> locationToPin = new HashMap<>();
		// Location pin image
		Image locationPinImage = new Image("/images/mapWindow/locationPin.png", 64, 64, false, true);
		// The glow when hovering a location pin
		Glow hoverGlow = new Glow();
		// When our location list changes we add a new pin
		SanimalData.getInstance().getLocationList().addListener((ListChangeListener<Location>) c ->
		{
			while (c.next())
				// If we added locations, add the map pin icon
				if (c.wasAdded())
				{
					// Iterate over locations
					for (Location location : c.getAddedSubList())
					{
						// Create a map pin to render the site's center point when zoomed out
						MapNode mapPin = new AlignedMapNode(Pos.TOP_CENTER);
						// Set the pin's center to be the node's center
						mapPin.setLocation(new fxmapcontrol.Location(location.getLat(), location.getLng()));
						// Add a new imageview to the pin
						ImageView pinImageView = new ImageView();
						// Make sure the image represents if the pin is hovered or not
						pinImageView.setImage(locationPinImage);
						// Add the image to the pin
						mapPin.getChildren().add(pinImageView);
						// When we click a pin, show the popover
						mapPin.setOnMouseClicked(event ->
						{
							// Call our controller's update method and then show the popup
							locationPopOverController.updateLocation(location);
							popOver.show(mapPin);
							event.consume();
						});
						// Set the pin to glow when hovered
						mapPin.setOnMouseEntered(event ->
						{
							mapPin.setEffect(hoverGlow);
							event.consume();
						});
						// Set the pin to not glow when hovered
						mapPin.setOnMouseExited(event ->
						{
							mapPin.setEffect(null);
							event.consume();
						});

						// Store the mapping of location to pin in our hashmap
						locationToPin.put(location, mapPin);
						// Add the pin to our map
						this.map.addChild(mapPin, MapLayers.LOCATION_PINS);
					}
				}
				// If a location was removed remove the pin
				else if (c.wasRemoved())
				{
					// For each location...
					for (Location location : c.getRemoved())
						// This should always be true....
						if (locationToPin.containsKey(location))
							// Remove the pin from the map
							this.map.removeChild(locationToPin.remove(location));
				}
		});

		// When we move the mouse update the bottom label
		this.map.setOnMouseMoved(event ->
		{
			// Get the mouse location
			fxmapcontrol.Location locationOfMouse = this.map.getProjection().viewportPointToLocation(new Point2D(event.getX(), event.getY()));
			// Get the format for location
			SettingsData.LocationFormat locationFormat = SanimalData.getInstance().getSettings().getLocationFormat();
			// Set the location format text to be in either lat/long or utms
			if (locationFormat == SettingsData.LocationFormat.LatLong)
				this.lblMouseLocation.setText(RoundingUtils.round(locationOfMouse.getLatitude(), 5) + ", " + RoundingUtils.round(locationOfMouse.getLongitude(), 5));
			else if (locationFormat == SettingsData.LocationFormat.UTM)
			{
				UTMCoord utmCoord = SanimalAnalysisUtils.Deg2UTM(locationOfMouse.getLatitude(), locationOfMouse.getLongitude());
				this.lblMouseLocation.setText(utmCoord.getZone().toString() + utmCoord.getLetter().toString() + " - " + Math.round(utmCoord.getEasting()) + "E, " + Math.round(utmCoord.getNorthing()) + "N");
			}
		});
	}
}
