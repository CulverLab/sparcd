/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionListener;

import library.ComboBoxFullMenu;
import model.ImageEntry;
import model.Location;

public class ImageImportView extends JFrame
{
	private JList lstImages;
	private JTextField txtDate;
	private JLabel lblDate;
	private JLabel lblThumbnail;
	private JPanel pnlImageBrowser;
	private JCheckBox chxIncludeSubdirectories;
	private JButton btnBrowseForImages;
	private JScrollPane pneImageList;
	private JPanel pnlPropertyList;
	private JLabel lblLocation;
	private ComboBoxFullMenu<Location> cbxLocation;
	private JButton btnAddNewLocation;
	private JButton btnRemoveLocation;
	private JLabel lblLocationLat;
	private JTextField txtLat;
	private JLabel lblLocationLng;
	private JTextField txtLng;
	private JLabel lblLocationElevation;
	private JTextField txtElevation;
	private JLabel lblSpecies;
	private ComboBoxFullMenu<String> cbxSpecies;
	private JButton btnAddNewSpecies;
	private JButton btnRemoveSpecies;

	public ImageImportView()
	{
		this.getContentPane().setLayout(null);
		this.setResizable(false);
		this.setTitle("Image Importer");
		this.setSize(748, 610);

		lblThumbnail = new JLabel();
		lblThumbnail.setBounds(10, 11, 434, 362);
		lblThumbnail.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(lblThumbnail);

		pnlImageBrowser = new JPanel();
		pnlImageBrowser.setBounds(454, 11, 278, 362);
		pnlImageBrowser.setBorder(new LineBorder(Color.BLACK));
		pnlImageBrowser.setLayout(null);
		this.getContentPane().add(pnlImageBrowser);

		chxIncludeSubdirectories = new JCheckBox("Include Subdirectories");
		chxIncludeSubdirectories.setBounds(137, 332, 131, 23);
		pnlImageBrowser.add(chxIncludeSubdirectories);

		btnBrowseForImages = new JButton("Select Images");
		btnBrowseForImages.setBounds(6, 332, 125, 23);
		pnlImageBrowser.add(btnBrowseForImages);

		pneImageList = new JScrollPane();
		lstImages = new JList();
		lstImages.setModel(new DefaultListModel());
		pneImageList.setBounds(10, 11, 258, 314);
		pneImageList.setViewportView(lstImages);
		pnlImageBrowser.add(pneImageList);

		pnlPropertyList = new JPanel();
		pnlPropertyList.setLayout(null);
		pnlPropertyList.setBounds(10, 384, 722, 187);
		pnlPropertyList.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(pnlPropertyList);

		lblDate = new JLabel("Date Taken:");
		lblDate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDate.setBounds(10, 14, 87, 14);
		pnlPropertyList.add(lblDate);

		txtDate = new JTextField();
		txtDate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtDate.setBounds(107, 11, 317, 20);
		txtDate.setEditable(false);
		pnlPropertyList.add(txtDate);

		lblLocation = new JLabel("Location: ");
		lblLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocation.setBounds(10, 39, 87, 14);
		pnlPropertyList.add(lblLocation);

		cbxLocation = new ComboBoxFullMenu<Location>();
		cbxLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cbxLocation.setBounds(107, 36, 125, 23);
		cbxLocation.setSelectedIndex(-1);
		pnlPropertyList.add(cbxLocation);

		btnAddNewLocation = new JButton("Add");
		btnAddNewLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAddNewLocation.setBounds(242, 35, 72, 23);
		pnlPropertyList.add(btnAddNewLocation);

		btnRemoveLocation = new JButton("Remove");
		btnRemoveLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnRemoveLocation.setBounds(324, 35, 100, 23);
		pnlPropertyList.add(btnRemoveLocation);

		lblLocationLat = new JLabel("Latitude: ");
		lblLocationLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocationLat.setBounds(10, 64, 87, 14);
		pnlPropertyList.add(lblLocationLat);

		txtLat = new JTextField();
		txtLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtLat.setBounds(107, 61, 317, 20);
		txtLat.setEditable(false);
		pnlPropertyList.add(txtLat);

		lblLocationLng = new JLabel("Longitude: ");
		lblLocationLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocationLng.setBounds(10, 89, 87, 14);
		pnlPropertyList.add(lblLocationLng);

		txtLng = new JTextField();
		txtLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtLng.setBounds(107, 86, 317, 20);
		txtLng.setEditable(false);
		pnlPropertyList.add(txtLng);

		lblLocationElevation = new JLabel("Elevation: ");
		lblLocationElevation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocationElevation.setBounds(10, 114, 87, 14);
		pnlPropertyList.add(lblLocationElevation);

		txtElevation = new JTextField();
		txtElevation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtElevation.setBounds(107, 111, 317, 20);
		txtElevation.setEditable(false);
		pnlPropertyList.add(txtElevation);

		lblSpecies = new JLabel("Species: ");
		lblSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblSpecies.setBounds(10, 139, 87, 14);
		pnlPropertyList.add(lblSpecies);

		cbxSpecies = new ComboBoxFullMenu<String>();
		cbxSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cbxSpecies.setBounds(107, 135, 125, 23);
		cbxSpecies.setSelectedIndex(-1);
		pnlPropertyList.add(cbxSpecies);

		btnAddNewSpecies = new JButton("Add");
		btnAddNewSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAddNewSpecies.setBounds(242, 135, 72, 23);
		pnlPropertyList.add(btnAddNewSpecies);

		btnRemoveSpecies = new JButton("Remove");
		btnRemoveSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnRemoveSpecies.setBounds(324, 135, 100, 23);
		pnlPropertyList.add(btnRemoveSpecies);

		this.setLocationRelativeTo(null);
	}

	public void addImageListValueChanged(ListSelectionListener listener)
	{
		this.lstImages.addListSelectionListener(listener);
	}

	public void addImageBrowseListener(ActionListener listener)
	{
		this.btnBrowseForImages.addActionListener(listener);
	}

	public void addLocationSelectedListener(ItemListener listener)
	{
		this.cbxLocation.addItemListener(listener);
	}

	public void addALToAddNewLocation(ActionListener listener)
	{
		this.btnAddNewLocation.addActionListener(listener);
	}

	public void addALToRemoveLocation(ActionListener listener)
	{
		this.btnRemoveLocation.addActionListener(listener);
	}

	public int getMinSelectedImageIndex()
	{
		return this.lstImages.getMinSelectionIndex();
	}

	public int getMaxSelectedImageIndex()
	{
		return this.lstImages.getMaxSelectionIndex();
	}

	public String getSelectedLocation()
	{
		if (this.cbxLocation.getSelectedIndex() == -1)
			return "";
		else
			return this.cbxLocation.getSelectedItem().toString();
	}

	public boolean searchSubdirectories()
	{
		return this.chxIncludeSubdirectories.isSelected();
	}

	public void setThumbnailImage(ImageEntry image)
	{
		if (image != null)
			this.lblThumbnail.setIcon(image.createIcon(this.lblThumbnail.getWidth(), this.lblThumbnail.getHeight()));
		else
			this.lblThumbnail.setIcon(null);
	}

	public void setDate(String date)
	{
		this.txtDate.setText(date);
	}

	public void setLocation(Location imageLoc)
	{
		if (imageLoc != null)
			this.cbxLocation.setSelectedItem(imageLoc);
		else
			this.cbxLocation.setSelectedIndex(-1);
		this.refreshLocationFields();
	}

	public void setLocationList(List<Location> locations)
	{
		if (!locations.contains((Location) this.cbxLocation.getSelectedItem()))
			this.cbxLocation.setSelectedIndex(-1);
		this.cbxLocation.removeAllItems();
		for (Location location : locations)
			this.cbxLocation.addItem(location);
	}

	public void setImageList(List<ImageEntry> imageList)
	{
		if (this.lstImages.getModel() instanceof DefaultListModel)
		{
			DefaultListModel model = (DefaultListModel) this.lstImages.getModel();
			model.removeAllElements();
			for (ImageEntry file : imageList)
				model.addElement(file.getImagePath().getName());
		}
	}

	public void refreshLocationFields()
	{
		Location currentlySelected = ((Location) cbxLocation.getSelectedItem());
		if (currentlySelected != null)
		{
			txtLat.setText(Double.toString(currentlySelected.getLat()));
			txtLng.setText(Double.toString(currentlySelected.getLng()));
			txtElevation.setText(Double.toString(currentlySelected.getElevation()));
		}
		else
		{
			txtLat.setText("");
			txtLng.setText("");
			txtElevation.setText("");
		}
	}

	public Location askUserForNewLocation()
	{
		String name = "";
		while (name.isEmpty())
		{
			name = JOptionPane.showInputDialog("Enter the name of the new location");
			if (name == null)
				return null;
		}
		Double latitude = Double.MAX_VALUE;
		while (latitude == Double.MAX_VALUE)
		{
			try
			{
				String latitudeString = JOptionPane.showInputDialog("Enter the latitude of location '" + name + "'");
				if (latitudeString == null)
					return null;
				latitude = Double.parseDouble(latitudeString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		Double longitude = Double.MAX_VALUE;
		while (longitude == Double.MAX_VALUE)
		{
			try
			{
				String longitudeString = JOptionPane.showInputDialog("Enter the longitude of location '" + name + "'");
				if (longitudeString == null)
					return null;
				longitude = Double.parseDouble(longitudeString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		Double elevation = Double.MAX_VALUE;
		while (elevation == Double.MAX_VALUE)
		{
			try
			{
				String elevationString = JOptionPane.showInputDialog("Enter the elevation of location '" + name + "'");
				if (elevationString == null)
					return null;
				elevation = Double.parseDouble(elevationString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		return new Location(name, latitude, longitude, elevation);
	}
}
