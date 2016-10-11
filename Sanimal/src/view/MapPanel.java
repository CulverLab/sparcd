package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.ArrayUtils;

import library.ComboBoxFullMenu;
import model.image.ImageLoadingUtils;

/**
 * This class defines the map panel used in the main program
 * 
 * @author David Slovikosky
 */
public class MapPanel extends JPanel
{
	private static final Double[] SLIDER_SPEED_MULTIPLIERS = new Double[]
	{ 0.0, 0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 4.0, 10.0 };

	private final Timer zoomTimer;

	private JLabel lblMapProvider;
	private ComboBoxFullMenu<String> cbxMapProviders;
	private SanimalMap mapViewer;
	private JLabel lblZoomLevel;
	private String zoomLevelBase = "Zoom: ";
	private JLabel lblCurrentLat;
	private String currentLatBase = "Latitude: ";
	private JLabel lblCurrentLng;
	private String currentLngBase = "Longitude: ";

	private JPanel pnlBottomBar;
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
	private JLabel lblCurrentDate;

	public MapPanel()
	{
		this.setBounds(679, 11, 638, 607);
		this.setBorder(new LineBorder(Color.BLACK));
		SpringLayout layMapPanel = new SpringLayout();
		this.setLayout(layMapPanel);

		this.zoomTimer = new Timer(500, event ->
		{
			MapPanel.this.mapViewer.rescaleBasedOnZoom();
		});

		lblMapProvider = new JLabel("Map:");
		layMapPanel.putConstraint(SpringLayout.NORTH, lblMapProvider, 5, SpringLayout.NORTH, this);
		layMapPanel.putConstraint(SpringLayout.WEST, lblMapProvider, 10, SpringLayout.WEST, this);
		lblMapProvider.setFont(new Font("Tahoma", Font.PLAIN, 14));
		this.add(lblMapProvider);

		cbxMapProviders = new ComboBoxFullMenu<String>();
		layMapPanel.putConstraint(SpringLayout.NORTH, cbxMapProviders, 2, SpringLayout.NORTH, this);
		layMapPanel.putConstraint(SpringLayout.WEST, cbxMapProviders, 73, SpringLayout.WEST, this);
		layMapPanel.putConstraint(SpringLayout.EAST, lblMapProvider, -6, SpringLayout.WEST, cbxMapProviders);
		cbxMapProviders.setFont(new Font("Tahoma", Font.PLAIN, 14));
		this.add(cbxMapProviders);

		mapViewer = new SanimalMap(cbxMapProviders);
		layMapPanel.putConstraint(SpringLayout.NORTH, mapViewer, 35, SpringLayout.NORTH, this);
		layMapPanel.putConstraint(SpringLayout.WEST, mapViewer, 0, SpringLayout.WEST, this);
		layMapPanel.putConstraint(SpringLayout.SOUTH, mapViewer, -69, SpringLayout.SOUTH, this);
		layMapPanel.putConstraint(SpringLayout.EAST, mapViewer, 0, SpringLayout.EAST, this);
		mapViewer.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		// When scrolling set the zoom level lables accordingly
		mapViewer.addMouseWheelListener(new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent event)
			{
				lblZoomLevel.setText(zoomLevelBase + mapViewer.getZoom());
				lblCurrentLat.setText(currentLatBase + String.format("%7.6f", mapViewer.getCenterPosition().getLatitude()));
				lblCurrentLng.setText(currentLngBase + String.format("%7.6f", mapViewer.getCenterPosition().getLongitude()));
				if (!zoomTimer.isRunning())
					zoomTimer.start();
				else
					zoomTimer.restart();
			}
		});
		// When you drag the mouse, set lat/lng coords accordingly
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

		lblCurrentLat = new JLabel(currentLatBase + String.format("%7.6f", mapViewer.getCenterPosition().getLatitude()));
		layMapPanel.putConstraint(SpringLayout.NORTH, lblCurrentLat, 5, SpringLayout.NORTH, this);
		layMapPanel.putConstraint(SpringLayout.EAST, cbxMapProviders, -6, SpringLayout.WEST, lblCurrentLat);
		lblCurrentLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		this.add(lblCurrentLat);

		lblCurrentLng = new JLabel(currentLngBase + String.format("%7.6f", mapViewer.getCenterPosition().getLongitude()));
		layMapPanel.putConstraint(SpringLayout.NORTH, lblCurrentLng, 5, SpringLayout.NORTH, this);
		layMapPanel.putConstraint(SpringLayout.EAST, lblCurrentLat, -6, SpringLayout.WEST, lblCurrentLng);
		lblCurrentLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		this.add(lblCurrentLng);

		lblZoomLevel = new JLabel(zoomLevelBase + mapViewer.getZoom());
		layMapPanel.putConstraint(SpringLayout.NORTH, lblZoomLevel, 5, SpringLayout.NORTH, this);
		layMapPanel.putConstraint(SpringLayout.WEST, lblZoomLevel, 533, SpringLayout.WEST, this);
		layMapPanel.putConstraint(SpringLayout.EAST, lblZoomLevel, -11, SpringLayout.EAST, this);
		layMapPanel.putConstraint(SpringLayout.EAST, lblCurrentLng, -6, SpringLayout.WEST, lblZoomLevel);
		lblZoomLevel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		this.add(lblZoomLevel);

		pnlBottomBar = new JPanel();
		layMapPanel.putConstraint(SpringLayout.NORTH, pnlBottomBar, 0, SpringLayout.SOUTH, mapViewer);
		layMapPanel.putConstraint(SpringLayout.WEST, pnlBottomBar, 0, SpringLayout.WEST, this);
		layMapPanel.putConstraint(SpringLayout.SOUTH, pnlBottomBar, 0, SpringLayout.SOUTH, this);
		layMapPanel.putConstraint(SpringLayout.EAST, pnlBottomBar, 0, SpringLayout.EAST, this);
		SpringLayout layPnlBottomBar = new SpringLayout();
		pnlBottomBar.setLayout(layPnlBottomBar);
		this.add(pnlBottomBar);

		btnTop = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Top2.png")));
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, btnTop, 5, SpringLayout.NORTH, pnlBottomBar);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, btnTop, 5, SpringLayout.WEST, pnlBottomBar);
		layPnlBottomBar.putConstraint(SpringLayout.EAST, btnTop, 30, SpringLayout.WEST, pnlBottomBar);
		btnTop.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlBottomBar.add(btnTop);

		btnBackwards = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Backward2.png")));
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, btnBackwards, 0, SpringLayout.NORTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, btnBackwards, 6, SpringLayout.EAST, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.EAST, btnBackwards, 31, SpringLayout.EAST, btnTop);
		btnBackwards.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlBottomBar.add(btnBackwards);

		btnPrevious = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Previous2.png")));
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, btnPrevious, 0, SpringLayout.NORTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, btnPrevious, 6, SpringLayout.EAST, btnBackwards);
		layPnlBottomBar.putConstraint(SpringLayout.SOUTH, btnPrevious, 0, SpringLayout.SOUTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.EAST, btnPrevious, 31, SpringLayout.EAST, btnBackwards);
		btnPrevious.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlBottomBar.add(btnPrevious);

		btnStop = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Stop2.png")));
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, btnStop, 0, SpringLayout.NORTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, btnStop, 6, SpringLayout.EAST, btnPrevious);
		layPnlBottomBar.putConstraint(SpringLayout.EAST, btnStop, 31, SpringLayout.EAST, btnPrevious);
		btnStop.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlBottomBar.add(btnStop);

		btnNext = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Next2.png")));
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, btnNext, 0, SpringLayout.NORTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, btnNext, 6, SpringLayout.EAST, btnStop);
		layPnlBottomBar.putConstraint(SpringLayout.SOUTH, btnNext, 0, SpringLayout.SOUTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.EAST, btnNext, 31, SpringLayout.EAST, btnStop);
		btnNext.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlBottomBar.add(btnNext);

		btnForward = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Forward2.png")));
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, btnForward, 0, SpringLayout.NORTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, btnForward, 6, SpringLayout.EAST, btnNext);
		layPnlBottomBar.putConstraint(SpringLayout.EAST, btnForward, 31, SpringLayout.EAST, btnNext);
		btnForward.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlBottomBar.add(btnForward);

		btnBottom = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Bottom2.png")));
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, btnBottom, 0, SpringLayout.NORTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, btnBottom, 6, SpringLayout.EAST, btnForward);
		layPnlBottomBar.putConstraint(SpringLayout.EAST, btnBottom, 31, SpringLayout.EAST, btnForward);
		btnBottom.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlBottomBar.add(btnBottom);

		sldSpeed = new JSlider(SwingConstants.HORIZONTAL);
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, sldSpeed, 0, SpringLayout.NORTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, sldSpeed, 6, SpringLayout.EAST, btnBottom);
		sldSpeed.setMinorTickSpacing(1);
		sldSpeed.setValue(ArrayUtils.indexOf(SLIDER_SPEED_MULTIPLIERS, 1.0D));
		sldSpeed.setPaintTicks(true);
		sldSpeed.setSnapToTicks(true);
		sldSpeed.setMinimum(0);
		sldSpeed.setMaximum(SLIDER_SPEED_MULTIPLIERS.length - 1);
		sldSpeed.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent event)
			{
				lblSpeed.setText(SLIDER_SPEED_MULTIPLIERS[sldSpeed.getValue()] + "x");
			}
		});
		pnlBottomBar.add(sldSpeed);

		lblSpeed = new JLabel(SLIDER_SPEED_MULTIPLIERS[sldSpeed.getValue()] + "x");
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, lblSpeed, 0, SpringLayout.NORTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, lblSpeed, 6, SpringLayout.EAST, sldSpeed);
		layPnlBottomBar.putConstraint(SpringLayout.SOUTH, lblSpeed, 0, SpringLayout.SOUTH, btnTop);
		lblSpeed.setFont(new Font("Tahoma", Font.PLAIN, 16));
		pnlBottomBar.add(lblSpeed);

		prgDataShow = new JProgressBar(SwingConstants.HORIZONTAL);
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, prgDataShow, 6, SpringLayout.SOUTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, prgDataShow, 0, SpringLayout.WEST, pnlBottomBar);
		layPnlBottomBar.putConstraint(SpringLayout.SOUTH, prgDataShow, 0, SpringLayout.SOUTH, pnlBottomBar);
		layPnlBottomBar.putConstraint(SpringLayout.EAST, prgDataShow, 0, SpringLayout.EAST, pnlBottomBar);
		prgDataShow.setBounds(0, 39, 618, 23);
		prgDataShow.setMinimum(0);
		prgDataShow.setMaximum(100);
		// Clicking & Dragging allows for updating the progress bar
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
		pnlBottomBar.add(prgDataShow);

		lblCurrentDate = new JLabel();
		layPnlBottomBar.putConstraint(SpringLayout.NORTH, lblCurrentDate, 0, SpringLayout.NORTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.WEST, lblCurrentDate, 16, SpringLayout.EAST, lblSpeed);
		layPnlBottomBar.putConstraint(SpringLayout.SOUTH, lblCurrentDate, 0, SpringLayout.SOUTH, btnTop);
		layPnlBottomBar.putConstraint(SpringLayout.EAST, lblCurrentDate, -10, SpringLayout.EAST, pnlBottomBar);
		lblCurrentDate.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblCurrentDate.setText("");
		pnlBottomBar.add(lblCurrentDate);
	}

	public void addALToTop(ActionListener listener)
	{
		this.btnTop.addActionListener(listener);
	}

	public void addALToForward(ActionListener listener)
	{
		this.btnForward.addActionListener(listener);
	}

	public void addALToNext(ActionListener listener)
	{
		this.btnNext.addActionListener(listener);
	}

	public void addALToStop(ActionListener listener)
	{
		this.btnStop.addActionListener(listener);
	}

	public void addALToPrevious(ActionListener listener)
	{
		this.btnPrevious.addActionListener(listener);
	}

	public void addALToBackwards(ActionListener listener)
	{
		this.btnBackwards.addActionListener(listener);
	}

	public void addALToBottom(ActionListener listener)
	{
		this.btnBottom.addActionListener(listener);
	}

	public void addCLToSpeedSlider(ChangeListener listener)
	{
		this.sldSpeed.addChangeListener(listener);
	}

	public void setCurrentDateLabel(Date date)
	{
		this.lblCurrentDate.setText("Date: " + date.toString());
		this.lblCurrentDate.setToolTipText("Date: " + date.toString());
	}

	public Double getCurrentSliderSpeed()
	{
		return SLIDER_SPEED_MULTIPLIERS[this.sldSpeed.getValue()];
	}

	/**
	 * @return The map viewer instance
	 */
	public SanimalMap getMapViewer()
	{
		return this.mapViewer;
	}

	/**
	 * @return Return the progress bar used as the timeline
	 */
	public JProgressBar getPrgDataShow()
	{
		return this.prgDataShow;
	}
}
