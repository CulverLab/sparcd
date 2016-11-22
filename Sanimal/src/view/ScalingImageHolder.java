package view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RescaleOp;

import javax.swing.JComponent;

public class ScalingImageHolder extends JComponent
{
	private Double scaleX = 1.0;
	private Double scaleY = 1.0;

	private Double brightness = 1.0;
	private Double contrast = 1.0;

	private Integer originalPanX = 0;
	private Integer originalPanY = 0;
	private Integer panX = 0;
	private Integer panY = 0;

	private Integer currentOffsetX = 0;
	private Integer currentOffsetY = 0;

	private BufferedImage source;
	private BufferedImage toDisplay;

	public ScalingImageHolder()
	{
		this.addMouseWheelListener(new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent event)
			{
				Integer widthDifference = (int) (source.getWidth() * scaleX);
				Integer heightDifference = (int) (source.getHeight() * scaleY);
				Double scaleFactor = (event.getWheelRotation() > 0 ? 0.9 : (1 / .9));
				scaleX = scaleX * scaleFactor;
				scaleY = scaleY * scaleFactor;
				widthDifference = widthDifference - (int) (source.getWidth() * scaleX);
				heightDifference = heightDifference - (int) (source.getHeight() * scaleY);
				Double percentAcrossX = event.getX() / (double) (ScalingImageHolder.this.getWidth());
				Double percentAcrossY = event.getY() / (double) (ScalingImageHolder.this.getHeight());
				System.out.println(percentAcrossX + ", " + percentAcrossY);
				panX = (int) (panX + widthDifference * percentAcrossX);
				panY = (int) (panY + heightDifference * percentAcrossY);
				repaint();
			}
		});

		this.addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseMoved(MouseEvent event)
			{
			}

			@Override
			public void mouseDragged(MouseEvent event)
			{
				panX = originalPanX + event.getX();
				panY = originalPanY + event.getY();

				repaint();
			}
		});

		this.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				originalPanX = panX;
				originalPanY = panY;
			}

			@Override
			public void mousePressed(MouseEvent event)
			{
				originalPanX = panX - event.getX();
				originalPanY = panY - event.getY();
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
			public void mouseClicked(MouseEvent event)
			{
			}
		});
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		if (toDisplay != null)
		{
			Graphics2D g2d = (Graphics2D) g;

			// Backup original transform
			AffineTransform originalTransform = g2d.getTransform();

			g2d.translate(panX, panY);
			g2d.scale(scaleX, scaleY);

			// paint the image here with no scaling
			g2d.drawImage(toDisplay, 0, 0, null);

			// Restore original transform
			g2d.setTransform(originalTransform);
		}
	}

	public void setScaleToFit(Integer width, Integer height)
	{
		if (source != null)
		{
			this.scaleX = ((double) width / (double) this.source.getWidth(null));
			this.scaleY = ((double) height / (double) this.source.getHeight(null));
		}
	}

	public void setPanX(Integer panX)
	{
		this.panX = panX;
		ScalingImageHolder.this.repaint();
	}

	public void setPanY(Integer panY)
	{
		this.panY = panY;
		ScalingImageHolder.this.repaint();
	}

	public void setSource(BufferedImage source)
	{
		if (this.source != null)
			this.source.flush();
		if (source.getColorModel() instanceof IndexColorModel)
		{
			System.out.println("Invalid image");
		}
		else
		{
			this.source = source;
			this.updateEdited();
			ScalingImageHolder.this.repaint();
		}
	}

	public void setBrightness(Double brightness)
	{
		this.brightness = brightness;
		this.updateEdited();
		this.repaint();
	}

	public void setContrast(Double contrast)
	{
		this.contrast = contrast;
		this.updateEdited();
		this.repaint();
	}

	public void updateEdited()
	{
		if (toDisplay != null)
			toDisplay.flush();
		// Contrast, Brightness, Hints
		RescaleOp filter = new RescaleOp(contrast.floatValue(), brightness.floatValue(), null);
		toDisplay = filter.filter(source, null);
	}
}
