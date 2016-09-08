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
	private JLabel lblCurrentDate;

	public MapPanel()
	{
		this.setBounds(679, 11, 638, 607);
		this.setBorder(new LineBorder(Color.BLACK));
		this.setLayout(null);

		this.zoomTimer = new Timer(500, event ->
		{
			MapPanel.this.mapViewer.rescaleBasedOnZoom();
		});

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
		mapViewer.setBounds(0, 60, 637, 463);
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

		btnTop = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Top2.png")));
		btnTop.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnTop.setBounds(10, 537, 20, 20);
		this.add(btnTop);

		btnBackwards = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Backward2.png")));
		btnBackwards.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnBackwards.setBounds(40, 537, 20, 20);
		this.add(btnBackwards);

		btnPrevious = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Previous2.png")));
		btnPrevious.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnPrevious.setBounds(70, 537, 20, 20);
		this.add(btnPrevious);

		btnStop = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Stop2.png")));
		btnStop.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnStop.setBounds(100, 537, 20, 20);
		this.add(btnStop);

		btnNext = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Next2.png")));
		btnNext.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnNext.setBounds(130, 537, 20, 20);
		this.add(btnNext);

		btnForward = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Forward2.png")));
		btnForward.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnForward.setBounds(160, 537, 20, 20);
		this.add(btnForward);

		btnBottom = new JButton(new ImageIcon(ImageLoadingUtils.retrieveImageResource("Bottom2.png")));
		btnBottom.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnBottom.setBounds(190, 537, 20, 20);
		this.add(btnBottom);

		sldSpeed = new JSlider(SwingConstants.HORIZONTAL);
		sldSpeed.setMinorTickSpacing(1);
		sldSpeed.setValue(ArrayUtils.indexOf(SLIDER_SPEED_MULTIPLIERS, 1.0D));
		sldSpeed.setPaintTicks(true);
		sldSpeed.setSnapToTicks(true);
		sldSpeed.setBounds(220, 534, 68, 23);
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
		this.add(sldSpeed);

		lblSpeed = new JLabel(SLIDER_SPEED_MULTIPLIERS[sldSpeed.getValue()] + "x");
		lblSpeed.setBounds(292, 534, 43, 23);
		this.add(lblSpeed);
		lblSpeed.setFont(new Font("Tahoma", Font.PLAIN, 16));

		prgDataShow = new JProgressBar(SwingConstants.HORIZONTAL);
		prgDataShow.setBounds(10, 568, 618, 23);
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
		this.add(prgDataShow);

		this.lblCurrentDate = new JLabel();
		lblCurrentDate.setBounds(345, 534, 283, 23);
		lblCurrentDate.setFont(new Font("Tahoma", Font.PLAIN, 16));
		this.add(lblCurrentDate);
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
		this.lblCurrentDate.setText(date.toString());
		this.lblCurrentDate.setToolTipText(date.toString());
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
