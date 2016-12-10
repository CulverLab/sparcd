package view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

import model.image.ImageEntry;

public class ImageView extends JFrame
{
	private ScalingImageHolder lblImage;
	private Timer resizeStopwatch = new Timer(250, new ResizeListener());
	private boolean imageIsHovered = false;

	public ImageView()
	{
		SpringLayout layout = new SpringLayout();
		this.getContentPane().setLayout(layout);
		this.setTitle("Image Viewer");
		this.setSize(800, 500);
		resizeStopwatch.setRepeats(false);

		lblImage = new ScalingImageHolder();
		layout.putConstraint(SpringLayout.NORTH, lblImage, 0, SpringLayout.NORTH, getContentPane());
		layout.putConstraint(SpringLayout.WEST, lblImage, 0, SpringLayout.WEST, getContentPane());
		layout.putConstraint(SpringLayout.SOUTH, lblImage, 0, SpringLayout.SOUTH, getContentPane());
		layout.putConstraint(SpringLayout.EAST, lblImage, 0, SpringLayout.EAST, getContentPane());
		lblImage.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(lblImage);

		this.addComponentListener(new ComponentListener()
		{
			@Override
			public void componentShown(ComponentEvent event)
			{
			}

			@Override
			public void componentResized(ComponentEvent event)
			{
				if (resizeStopwatch.isRunning())
					resizeStopwatch.restart();
				else
					resizeStopwatch.start();
			}

			@Override
			public void componentMoved(ComponentEvent event)
			{
			}

			@Override
			public void componentHidden(ComponentEvent event)
			{
			}
		});

		lblImage.addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseMoved(MouseEvent event)
			{
				if (ImageView.this.imageIsHovered)
				{

				}
			}

			@Override
			public void mouseDragged(MouseEvent event)
			{
			}
		});

		lblImage.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseReleased(MouseEvent event)
			{
			}

			@Override
			public void mousePressed(MouseEvent event)
			{
			}

			@Override
			public void mouseExited(MouseEvent event)
			{
				imageIsHovered = false;
			}

			@Override
			public void mouseEntered(MouseEvent event)
			{
				imageIsHovered = true;
			}

			@Override
			public void mouseClicked(MouseEvent event)
			{
			}
		});

		this.setLocationRelativeTo(null);
	}

	public void setDisplayImage(ImageEntry image)
	{
		if (image != null)
			try
			{
				this.lblImage.setSource(ImageIO.read(image.getFile()));
				this.resetImage();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		else
			this.lblImage.setSource(null);
	}

	public void resetImage()
	{
		this.lblImage.setPanX(0);
		this.lblImage.setPanY(0);
		this.lblImage.setScaleToFit(this.getContentPane().getWidth(), this.getContentPane().getHeight());
	}

	public void setImageBrightness(Double brightness)
	{
		this.lblImage.setBrightness(brightness);
	}

	public void setImageContrast(Double contrast)
	{
		this.lblImage.setContrast(contrast);
	}

	private class ResizeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			resetImage();
		}
	}
}
