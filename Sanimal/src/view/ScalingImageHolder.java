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
	private Double baseScaleX = 1.0;
	private Double baseScaleY = 1.0;

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

	private Integer currentZoom = 0;
	private Double SCALE_FACTOR = 0.1D;

	public ScalingImageHolder()
	{
		this.addMouseWheelListener(new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent event)
			{
				if (event.getWheelRotation() > 0)
					currentZoom = currentZoom - 1;
				else
					currentZoom = currentZoom + 1;
				if (currentZoom < 0)
					currentZoom = 0;
				Double percentAcrossImageX = (event.getX() - panX) / (source.getWidth() * scaleX);
				Double percentAcrossImageY = (event.getY() - panY) / (source.getHeight() * scaleY);
				Integer widthScaled = (int) (source.getWidth() * scaleX);
				Integer heightScaled = (int) (source.getHeight() * scaleY);
				scaleX = baseScaleX + SCALE_FACTOR * currentZoom;
				scaleY = baseScaleY + SCALE_FACTOR * currentZoom;
				Integer widthDifference = widthScaled - (int) (source.getWidth() * scaleX);
				Integer heightDifference = heightScaled - (int) (source.getHeight() * scaleY);
				panX = (int) (panX + widthDifference * percentAcrossImageX);
				panY = (int) (panY + heightDifference * percentAcrossImageY);
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
			if (this.scaleX < this.scaleY)
				this.scaleY = this.scaleX;
			else
				this.scaleX = this.scaleY;
			this.baseScaleX = this.scaleX;
			this.baseScaleY = this.scaleY;
			this.currentZoom = 0;
		}
	}

	public void setPanX(Integer panX)
	{
		this.panX = panX;
		this.repaint();
	}

	public void setPanY(Integer panY)
	{
		this.panY = panY;
		this.repaint();
	}

	public void setSource(BufferedImage source)
	{
		if (source != null)
		{
			if (source.getColorModel() instanceof IndexColorModel)
			{
				System.out.println("Invalid image");
			}
			else
			{
				if (this.source != null)
					this.source.flush();
				this.source = source;
				this.updateEdited();
				ScalingImageHolder.this.repaint();
			}
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
