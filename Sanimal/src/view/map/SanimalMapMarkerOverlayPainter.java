package view.map;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import javax.swing.JComponent;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointPainter;

/**
 * The painter for a Sanimal map marker
 * 
 * @author David Slovikosky
 */
public class SanimalMapMarkerOverlayPainter extends WaypointPainter<SanimalMapMarkerOverlay>
{
	/**
	 * Paint the component centered
	 */
	@Override
	protected void doPaint(Graphics2D g, JXMapViewer jxMapViewer, int width, int height)
	{
		for (SwingComponentOverlay cameraTrap : this.getWaypoints())
		{
			Point2D point = jxMapViewer.getTileFactory().geoToPixel(cameraTrap.getPosition(), jxMapViewer.getZoom());
			Rectangle rectangle = jxMapViewer.getViewportBounds();
			int componentX = (int) (point.getX() - rectangle.getX());
			int componentY = (int) (point.getY() - rectangle.getY());
			JComponent component = cameraTrap.getComponent();
			component.setLocation(componentX - component.getWidth() / 2, componentY - component.getHeight() / 2);
		}
	}
}
