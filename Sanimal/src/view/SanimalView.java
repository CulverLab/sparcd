package view;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SanimalView extends JFrame
{
	private JPanel mapPanel;
	private JPanel topBar;
	private JButton btnLoadImages;

	public SanimalView()
	{
		this.getContentPane().setLayout(null);
		this.setSize(677, 568);

		mapPanel = new JPanel();
		mapPanel.setBounds(10, 102, 516, 420);
		this.getContentPane().add(mapPanel);

		topBar = new JPanel();
		topBar.setLayout(null);
		topBar.setBounds(10, 11, 641, 77);
		this.getContentPane().add(topBar);

		btnLoadImages = new JButton("Load Images");
		btnLoadImages.setBounds(10, 11, 122, 23);
		topBar.add(btnLoadImages);

		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	public void addALToLoadImages(ActionListener listener)
	{
		this.btnLoadImages.addActionListener(listener);
	}
}
