package view;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.WaypointPainter;

import library.ComboBoxFullMenu;
import model.image.ImageEntry;
import view.map.GoogleMapsTileFactoryInfo;
import view.map.SanimalMapMarkerOverlay;
import view.map.SanimalMapMarkerOverlayPainter;
import view.map.SwingComponentOverlay;

public class SanimalMap extends JXMapViewer
{
	private WaypointPainter<SanimalMapMarkerOverlay> painter = new SanimalMapMarkerOverlayPainter();
	private Set<SanimalMapMarkerOverlay> locations = new HashSet<SanimalMapMarkerOverlay>();

	public SanimalMap(ComboBoxFullMenu<String> cbxMapProviders)
	{
		// Where to get map tiles from
		List<DefaultTileFactory> factories = new ArrayList<DefaultTileFactory>();

		// Google providers
		factories.add(new DefaultTileFactory(new GoogleMapsTileFactoryInfo(GoogleMapsTileFactoryInfo.MapType.HYBRID)));
		cbxMapProviders.addItem("Google Maps (Hybrid)");
		factories.add(new DefaultTileFactory(new GoogleMapsTileFactoryInfo(GoogleMapsTileFactoryInfo.MapType.TERRAIN)));
		cbxMapProviders.addItem("Google Maps (Terrain and roadmap)");
		factories.add(new DefaultTileFactory(new GoogleMapsTileFactoryInfo(GoogleMapsTileFactoryInfo.MapType.TERRAIN_ONLY)));
		cbxMapProviders.addItem("Google Maps (Terrain only)");
		factories.add(new DefaultTileFactory(new GoogleMapsTileFactoryInfo(GoogleMapsTileFactoryInfo.MapType.ALTERED_ROADMAP)));
		cbxMapProviders.addItem("Google Maps (Alternate roadmap)");
		factories.add(new DefaultTileFactory(new GoogleMapsTileFactoryInfo(GoogleMapsTileFactoryInfo.MapType.STANDARD_ROADMAP)));
		cbxMapProviders.addItem("Google Maps (Standard roadmap)");
		factories.add(new DefaultTileFactory(new GoogleMapsTileFactoryInfo(GoogleMapsTileFactoryInfo.MapType.ROADS_ONLY)));
		cbxMapProviders.addItem("Google Maps (Roads only)");

		// Virtual earth tile factory
		factories.add(new DefaultTileFactory(new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.HYBRID)));
		cbxMapProviders.addItem("Virtual Earth (Hybrid)");
		factories.add(new DefaultTileFactory(new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP)));
		cbxMapProviders.addItem("Virtual Earth (Map)");
		factories.add(new DefaultTileFactory(new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.SATELLITE)));
		cbxMapProviders.addItem("Virtual Earth (Satellite)");

		// OSM
		factories.add(new DefaultTileFactory(new OSMTileFactoryInfo()));
		cbxMapProviders.addItem("Open Street Map");

		for (DefaultTileFactory tileFactory : factories)
			tileFactory.setThreadPoolSize(8);

		this.setTileFactory(factories.get(0));

		cbxMapProviders.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent event)
			{
				if (event.getStateChange() == ItemEvent.SELECTED)
				{
					TileFactory newFactory = factories.get(cbxMapProviders.getSelectedIndex());
					TileFactory oldFactory = SanimalMap.this.getTileFactory();
					// Something -> Google maps
					if (newFactory.getInfo().getName().equals("GoogleMaps") && !oldFactory.getInfo().getName().equals("GoogleMaps"))
					{
						GeoPosition old = SanimalMap.this.getCenterPosition();
						SanimalMap.this.setTileFactory(newFactory);
						SanimalMap.this.setCenterPosition(old);
						SanimalMap.this.setZoom(SanimalMap.this.getZoom() + 3);
					}
					// Google maps -> Something else
					else if (!newFactory.getInfo().getName().equals("GoogleMaps") && oldFactory.getInfo().getName().equals("GoogleMaps"))
					{
						SanimalMap.this.setZoom(SanimalMap.this.getZoom() - 3);
						GeoPosition old = SanimalMap.this.getCenterPosition();
						SanimalMap.this.setTileFactory(newFactory);
						SanimalMap.this.setCenterPosition(old);
					}
					// Something -> Something or Google -> Google
					else
					{
						GeoPosition old = SanimalMap.this.getCenterPosition();
						SanimalMap.this.setTileFactory(newFactory);
						SanimalMap.this.setCenterPosition(old);
					}
				}
			}
		});

		// Cache files, completely optional (Not allowed with google maps)
		//File cacheDir = new File(System.getProperty("user.home") + File.separator + "Sanimal Map Tiles");
		//LocalResponseCache.installResponseCache(tileFactoryInfo.getBaseURL(), cacheDir, false);

		// Center on tucson
		GeoPosition tucson = new GeoPosition(32.272951, -110.836367);
		this.setZoom(5);
		this.setAddressLocation(tucson);

		// Allow map scrolling
		MouseInputListener listener = new PanMouseInputListener(this);
		SanimalMap.this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
		this.addMouseListener(new CenterMapListener(this));
		this.addMouseWheelListener(new ZoomMouseWheelListenerCursor(this));
		this.addKeyListener(new PanKeyListener(this));

		this.setOverlayPainter(painter);
	}

	public void addMarker(SanimalMapMarkerOverlay marker)
	{
		// If we can add the marker, add it, then add the component to the screen. Then set the waypoint painter
		if (locations.add(marker))
		{
			this.add(marker.getComponent());
			painter.setWaypoints(locations);
			SanimalMap.this.repaint();
		}
	}

	public void removeMarker(SanimalMapMarkerOverlay marker)
	{
		// IF we can remove this marker remove it, then remove the component from the screen
		if (locations.remove(marker))
		{
			this.remove(marker.getComponent());
			painter.setWaypoints(locations);
			SanimalMap.this.repaint();
		}
	}

	public void clearMarkers()
	{
		for (SwingComponentOverlay overlay : locations)
			this.remove(overlay.getComponent());
		locations.clear();
		painter.setWaypoints(locations);
		SanimalMap.this.repaint();
	}

	public void setMarkerScale(double scale)
	{
		if (scale <= 1.0 && scale >= 0.0)
			for (SanimalMapMarkerOverlay overlay : locations)
				overlay.getComponent().updateIconsByScale(scale);
	}

	public void setImagesDrawnOnMap(List<ImageEntry> images)
	{
		for (SanimalMapMarkerOverlay mapMarkerOverlay : locations)
		{
			SanimalMapMarker marker = mapMarkerOverlay.getComponent();
			marker.clearMarkers();
		}
		for (SanimalMapMarkerOverlay mapMarkerOverlay : locations)
		{
			SanimalMapMarker marker = mapMarkerOverlay.getComponent();
			for (ImageEntry image : images)
				marker.addMarker(new SanimalMapImageMarker(image));
			marker.refreshLayout();
		}
		this.repaint();
	}
}
