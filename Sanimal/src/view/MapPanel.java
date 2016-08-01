/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import library.ComboBoxFullMenu;

public class MapPanel extends JPanel
{
	private JLabel lblMapProvider;
	private ComboBoxFullMenu<String> cbxMapProviders;
	private SanimalMap mapViewer;
	private JLabel lblZoomLevel;
	private String zoomLevelBase = "Zoom Level: ";
	private JLabel lblCurrentLat;
	private String currentLatBase = "Latitude: ";
	private JLabel lblCurrentLng;
	private String currentLngBase = "Longitude: ";

	private JButton btnTop;
	private JButton btnBackwards;
	private JButton btnPrevious;
	private JButton btnStop;
	private JButton btnNext;
	private JButton btnForward;
	private JButton btnBottom;
	private JSlider sldSpeed;
	private JLabel lblSpeed;
	private JProgressBar prgDataShow;

	public MapPanel()
	{
		this.setBounds(679, 11, 638, 666);
		this.setBorder(new LineBorder(Color.BLACK));
		this.setLayout(null);

		lblMapProvider = new JLabel("Map Provider:");
		lblMapProvider.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblMapProvider.setBounds(10, 10, 95, 14);
		this.add(lblMapProvider);

		cbxMapProviders = new ComboBoxFullMenu<String>();
		cbxMapProviders.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cbxMapProviders.setBounds(115, 6, 167, 23);
		this.add(cbxMapProviders);

		mapViewer = new SanimalMap(cbxMapProviders);
		mapViewer.setLayout(null);
		mapViewer.setBounds(0, 60, 637, 564);
		mapViewer.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		mapViewer.addMouseWheelListener(new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent event)
			{
				lblZoomLevel.setText(zoomLevelBase + mapViewer.getZoom());
				lblCurrentLat.setText(currentLatBase + String.format("%7.6f", mapViewer.getCenterPosition().getLatitude()));
				lblCurrentLng.setText(currentLngBase + String.format("%7.6f", mapViewer.getCenterPosition().getLongitude()));
				double maxZoom = (double) mapViewer.getTileFactory().getInfo().getMaximumZoomLevel();
				double currZoom = (double) mapViewer.getZoom();
				mapViewer.setMarkerScale((maxZoom - currZoom) / maxZoom);
			}
		});
		mapViewer.addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
			}

			@Override
			public void mouseDragged(MouseEvent event)
			{
				lblCurrentLat.setText(currentLatBase + String.format("%7.6f", mapViewer.getCenterPosition().getLatitude()));
				lblCurrentLng.setText(currentLngBase + String.format("%7.6f", mapViewer.getCenterPosition().getLongitude()));
			}
		});
		this.add(mapViewer);

		lblZoomLevel = new JLabel(zoomLevelBase + mapViewer.getZoom());
		lblZoomLevel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblZoomLevel.setBounds(10, 35, 105, 14);
		this.add(lblZoomLevel);

		lblCurrentLat = new JLabel(currentLatBase + String.format("%7.6f", mapViewer.getCenterPosition().getLatitude()));
		lblCurrentLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCurrentLat.setBounds(292, 10, 159, 14);
		this.add(lblCurrentLat);

		lblCurrentLng = new JLabel(currentLngBase + String.format("%7.6f", mapViewer.getCenterPosition().getLongitude()));
		lblCurrentLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCurrentLng.setBounds(292, 35, 159, 14);
		this.add(lblCurrentLng);

		btnTop = new JButton(new ImageIcon(MapPanel.class.getResource("/images/Top2.png")));
		btnTop.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnTop.setBounds(10, 635, 20, 20);
		this.add(btnTop);

		btnBackwards = new JButton(new ImageIcon(MapPanel.class.getResource("/images/Backward2.png")));
		btnBackwards.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnBackwards.setBounds(40, 635, 20, 20);
		this.add(btnBackwards);

		btnPrevious = new JButton(new ImageIcon(MapPanel.class.getResource("/images/Previous2.png")));
		btnPrevious.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnPrevious.setBounds(70, 635, 20, 20);
		this.add(btnPrevious);

		btnStop = new JButton(new ImageIcon(MapPanel.class.getResource("/images/Stop2.png")));
		btnStop.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnStop.setBounds(100, 635, 20, 20);
		this.add(btnStop);

		btnNext = new JButton(new ImageIcon(MapPanel.class.getResource("/images/Next2.png")));
		btnNext.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnNext.setBounds(130, 635, 20, 20);
		this.add(btnNext);

		btnForward = new JButton(new ImageIcon(MapPanel.class.getResource("/images/Forward2.png")));
		btnForward.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnForward.setBounds(160, 635, 20, 20);
		this.add(btnForward);

		btnBottom = new JButton(new ImageIcon(MapPanel.class.getResource("/images/Bottom2.png")));
		btnBottom.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnBottom.setBounds(190, 635, 20, 20);
		this.add(btnBottom);

		sldSpeed = new JSlider(SwingConstants.HORIZONTAL);
		sldSpeed.setMinorTickSpacing(1);
		sldSpeed.setValue(0);
		sldSpeed.setPaintTicks(true);
		sldSpeed.setSnapToTicks(true);
		sldSpeed.setBounds(220, 632, 68, 23);
		sldSpeed.setMinimum(0);
		sldSpeed.setMaximum(5);
		sldSpeed.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent event)
			{
				lblSpeed.setText(sldSpeed.getValue() + "x");
			}
		});
		this.add(sldSpeed);

		lblSpeed = new JLabel("1x");
		lblSpeed.setBounds(292, 632, 26, 23);
		this.add(lblSpeed);
		lblSpeed.setFont(new Font("Tahoma", Font.PLAIN, 16));

		prgDataShow = new JProgressBar(SwingConstants.HORIZONTAL);
		prgDataShow.setBounds(328, 632, 300, 23);
		prgDataShow.setMinimum(0);
		prgDataShow.setMaximum(100);
		prgDataShow.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
			}

			@Override
			public void mousePressed(MouseEvent event)
			{
				double percentage = (double) event.getX() / (double) event.getComponent().getWidth();
				int newLoc = (int) Math.floor(prgDataShow.getMaximum() * percentage);
				prgDataShow.setValue(newLoc);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
			}
		});
		prgDataShow.addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
			}

			@Override
			public void mouseDragged(MouseEvent event)
			{
				double percentage = (double) event.getX() / (double) event.getComponent().getWidth();
				int newLoc = (int) Math.floor(prgDataShow.getMaximum() * percentage);
				prgDataShow.setValue(newLoc);
			}
		});
		this.add(prgDataShow);
	}

	public SanimalMap getMapViewer()
	{
		return this.mapViewer;
	}

	public JProgressBar getPrgDataShow()
	{
		return this.prgDataShow;
	}
}
