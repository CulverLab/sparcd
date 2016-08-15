package view;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

import model.image.ImageEntry;

public class ImageView extends JFrame
{
	private JLabel lblImage;
	private ImageEntry image;
	private Timer resizeStopwatch = new Timer(250, new ResizeListener());

	public ImageView()
	{
		this.getContentPane().setLayout(new GridBagLayout());
		this.setTitle("Image Viewer");
		this.setSize(800, 500);
		resizeStopwatch.setRepeats(false);

		lblImage = new JLabel();
		lblImage.setBorder(new LineBorder(Color.BLACK));

		this.getContentPane().add(lblImage, new GridBagConstraints(0, 0, this.getWidth(), 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.CENTER, new Insets(0, 0, 0, 0), 0, 0));

		this.addComponentListener(new ComponentListener()
		{
			@Override
			public void componentShown(ComponentEvent event)
			{
				ImageView.this.refreshIcon();
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

		this.setLocationRelativeTo(null);
	}

	public void setDisplayImage(ImageEntry image)
	{
		this.image = image;
	}

	public void refreshIcon()
	{
		SanimalIconLoader.getInstance().scheduleTask(() ->
		{
			this.lblImage.setIcon(this.image.createIcon(this.getContentPane().getWidth(), this.getContentPane().getHeight()));
		});
	}

	private class ResizeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			ImageView.this.refreshIcon();
		}
	}
}
