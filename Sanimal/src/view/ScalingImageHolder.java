package view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

public class ScalingImageHolder extends JComponent
{
	private Double scaleX = 1.0;
	private Double scaleY = 1.0;

	private Integer panX = 0;
	private Integer panY = 0;

	private Integer previousX = 0;
	private Integer previousY = 0;

	private Image source;

	public ScalingImageHolder()
	{
		this.addMouseWheelListener(new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent event)
			{
				scaleX = scaleX - event.getWheelRotation() * 0.1;
				scaleY = scaleY - event.getWheelRotation() * 0.1;
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
				panX = previousX + event.getX();
				panY = previousY + event.getY();

				repaint();
			}
		});

		this.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				previousX = panX;
				previousY = panY;
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
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
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		if (source != null)
		{
			Graphics2D g2d = (Graphics2D) g;

			// Backup original transform
			AffineTransform originalTransform = g2d.getTransform();

			g2d.translate(panX, panY);
			g2d.scale(scaleX, scaleY);

			// paint the image here with no scaling
			g2d.drawImage(source, 0, 0, null);

			// Restore original transform
			g2d.setTransform(originalTransform);
		}
	}

	public void setScaleX(Double scaleX)
	{
		this.scaleX = scaleX;
		ScalingImageHolder.this.repaint();
	}

	public void setScaleY(Double scaleY)
	{
		this.scaleY = scaleY;
		ScalingImageHolder.this.repaint();
	}

	public void setScaleToFit(Integer width, Integer height)
	{
		if (source != null)
		{
			this.setScaleX((double) width / (double) this.source.getWidth(null));
			this.setScaleY((double) height / (double) this.source.getHeight(null));
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

	public void setSource(Image source)
	{
		if (this.source != null)
			this.source.flush();
		this.source = source;
		ScalingImageHolder.this.repaint();
	}
}
