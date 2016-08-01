package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import library.ComboBoxFullMenu;
import model.ImageDirectory;
import model.ImageEntry;
import model.Location;
import model.Species;
import model.SpeciesEntry;
import view.map.SanimalMapMarkerOverlay;

public class SanimalView extends JFrame
{
	private JPanel pnlImageBrowser;
	private JTree treImages;
	private JTextField txtDate;
	private JLabel lblDate;
	private JLabel lblThumbnail;
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
	private ComboBoxFullMenu<Species> cbxSpecies;
	private JButton btnAddNewSpecies;
	private JButton btnRemoveSpecies;
	private JButton btnPerformAnalysis;
	private JLabel lblAnalysisEventInterval;
	private JTextField txtAnalysisEventInterval;

	private JButton btnAddSpeciesToList;
	private JButton btnRemoveSpeciesFromList;
	private JPanel pnlSpeciesPresent;
	private JLabel lblSpeciesEntries;
	private JScrollPane pneSpeciesList;
	private JList lstSpecies;

	private MapPanel map;

	private JTabbedPane tabOutputTabs;
	private JScrollPane pneAllOutput;
	private JTextArea tarAllOutput;
	private JPanel pnlExcelOutput;
	private JButton btnToExcel;

	public SanimalView()
	{
		this.getContentPane().setLayout(null);
		this.setResizable(false);
		this.setTitle("Sanimal");
		this.setSize(1334, 713);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		lblThumbnail = new JLabel();
		lblThumbnail.setBounds(278, 11, 391, 300);
		lblThumbnail.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(lblThumbnail);

		pnlPropertyList = new JPanel();
		pnlPropertyList.setLayout(null);
		pnlPropertyList.setBounds(10, 322, 425, 202);
		pnlPropertyList.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(pnlPropertyList);

		lblDate = new JLabel("Date Taken:");
		lblDate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblDate.setBounds(10, 14, 87, 14);
		pnlPropertyList.add(lblDate);

		txtDate = new JTextField();
		txtDate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtDate.setBounds(107, 11, 310, 20);
		txtDate.setEditable(false);
		pnlPropertyList.add(txtDate);

		lblLocation = new JLabel("Location: ");
		lblLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocation.setBounds(10, 39, 87, 14);
		pnlPropertyList.add(lblLocation);

		cbxLocation = new ComboBoxFullMenu<Location>();
		cbxLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cbxLocation.setBounds(107, 36, 133, 23);
		cbxLocation.setSelectedIndex(-1);
		pnlPropertyList.add(cbxLocation);

		btnAddNewLocation = new JButton("Add");
		btnAddNewLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAddNewLocation.setBounds(250, 35, 70, 23);
		pnlPropertyList.add(btnAddNewLocation);

		btnRemoveLocation = new JButton("Remove");
		btnRemoveLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnRemoveLocation.setBounds(330, 35, 87, 23);
		pnlPropertyList.add(btnRemoveLocation);

		lblLocationLat = new JLabel("Latitude: ");
		lblLocationLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocationLat.setBounds(10, 64, 87, 14);
		pnlPropertyList.add(lblLocationLat);

		txtLat = new JTextField();
		txtLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtLat.setBounds(107, 61, 310, 20);
		txtLat.setEditable(false);
		pnlPropertyList.add(txtLat);

		lblLocationLng = new JLabel("Longitude: ");
		lblLocationLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocationLng.setBounds(10, 89, 87, 14);
		pnlPropertyList.add(lblLocationLng);

		txtLng = new JTextField();
		txtLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtLng.setBounds(107, 86, 310, 20);
		txtLng.setEditable(false);
		pnlPropertyList.add(txtLng);

		lblLocationElevation = new JLabel("Elevation: ");
		lblLocationElevation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblLocationElevation.setBounds(10, 114, 87, 14);
		pnlPropertyList.add(lblLocationElevation);

		txtElevation = new JTextField();
		txtElevation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtElevation.setBounds(107, 111, 310, 20);
		txtElevation.setEditable(false);
		pnlPropertyList.add(txtElevation);

		lblSpecies = new JLabel("Species List: ");
		lblSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblSpecies.setBounds(10, 139, 87, 14);
		pnlPropertyList.add(lblSpecies);

		cbxSpecies = new ComboBoxFullMenu<Species>();
		cbxSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cbxSpecies.setBounds(107, 135, 133, 23);
		cbxSpecies.setSelectedIndex(-1);
		pnlPropertyList.add(cbxSpecies);

		btnAddNewSpecies = new JButton("Add");
		btnAddNewSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAddNewSpecies.setBounds(250, 135, 70, 23);
		btnAddNewSpecies.setToolTipText("Add a new species to the species dictionary");
		pnlPropertyList.add(btnAddNewSpecies);

		btnRemoveSpecies = new JButton("Remove");
		btnRemoveSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnRemoveSpecies.setBounds(330, 135, 87, 23);
		btnRemoveSpecies.setToolTipText("Remove the selected species from the species dictionary");
		pnlPropertyList.add(btnRemoveSpecies);

		btnPerformAnalysis = new JButton("Perform Analysis");
		btnPerformAnalysis.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnPerformAnalysis.setBounds(10, 164, 138, 23);
		pnlPropertyList.add(btnPerformAnalysis);

		lblAnalysisEventInterval = new JLabel("Event Interval (minutes): ");
		lblAnalysisEventInterval.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblAnalysisEventInterval.setBounds(153, 168, 167, 14);
		pnlPropertyList.add(lblAnalysisEventInterval);

		txtAnalysisEventInterval = new JTextField();
		txtAnalysisEventInterval.setText("60");
		txtAnalysisEventInterval.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtAnalysisEventInterval.setBounds(330, 165, 87, 20);
		pnlPropertyList.add(txtAnalysisEventInterval);

		pnlSpeciesPresent = new JPanel();
		pnlSpeciesPresent.setLayout(null);
		pnlSpeciesPresent.setBounds(445, 322, 224, 202);
		pnlSpeciesPresent.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(pnlSpeciesPresent);

		lblSpeciesEntries = new JLabel("Species in image:");
		lblSpeciesEntries.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblSpeciesEntries.setBounds(10, 10, 207, 14);
		pnlSpeciesPresent.add(lblSpeciesEntries);

		pneSpeciesList = new JScrollPane();
		lstSpecies = new JList();
		lstSpecies.setModel(new DefaultListModel());
		pneSpeciesList.setBounds(10, 35, 207, 88);
		pneSpeciesList.setViewportView(lstSpecies);
		pnlSpeciesPresent.add(pneSpeciesList);

		btnAddSpeciesToList = new JButton("Add Species to Image");
		btnAddSpeciesToList.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnAddSpeciesToList.setBounds(10, 134, 207, 23);
		btnAddSpeciesToList.setToolTipText("Add the selected species to the selected image");
		pnlSpeciesPresent.add(btnAddSpeciesToList);

		btnRemoveSpeciesFromList = new JButton("Remove Species from Image");
		btnRemoveSpeciesFromList.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnRemoveSpeciesFromList.setBounds(10, 168, 207, 23);
		btnRemoveSpeciesFromList.setToolTipText("Remove the selected species from the selected image");
		pnlSpeciesPresent.add(btnRemoveSpeciesFromList);

		pnlImageBrowser = new JPanel();
		pnlImageBrowser.setBounds(10, 11, 258, 300);
		getContentPane().add(pnlImageBrowser);
		pnlImageBrowser.setBorder(new LineBorder(Color.BLACK));
		pnlImageBrowser.setLayout(null);

		chxIncludeSubdirectories = new JCheckBox("Include Subdirectories");
		chxIncludeSubdirectories.setFont(new Font("Tahoma", Font.PLAIN, 10));
		chxIncludeSubdirectories.setSelected(true);
		chxIncludeSubdirectories.setBounds(111, 270, 141, 23);
		chxIncludeSubdirectories.setToolTipText("Search sub-directories as well as the selected directory for images");
		pnlImageBrowser.add(chxIncludeSubdirectories);

		btnBrowseForImages = new JButton("Select Images");
		btnBrowseForImages.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnBrowseForImages.setBounds(6, 270, 99, 23);
		pnlImageBrowser.add(btnBrowseForImages);

		pneImageList = new JScrollPane();
		treImages = new JTree((TreeModel) null);
		pneImageList.setBounds(10, 11, 237, 252);
		pneImageList.setViewportView(treImages);
		pnlImageBrowser.add(pneImageList);

		map = new MapPanel();
		getContentPane().add(map);

		tabOutputTabs = new JTabbedPane();
		tabOutputTabs.setBounds(10, 535, 659, 142);
		tabOutputTabs.setSelectedIndex(-1);
		this.getContentPane().add(tabOutputTabs);

		tarAllOutput = new JTextArea();
		tarAllOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));
		tarAllOutput.setBounds(10, 11, 100, 100);
		tarAllOutput.setLayout(null);

		pnlExcelOutput = new JPanel();
		pnlExcelOutput.setFont(new Font("Tahoma", Font.PLAIN, 10));
		pnlExcelOutput.setLayout(null);

		btnToExcel = new JButton("Create excel file");
		btnToExcel.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnToExcel.setBounds(10, 11, 145, 23);
		btnToExcel.setLayout(null);
		pnlExcelOutput.add(btnToExcel);

		pneAllOutput = new JScrollPane();
		pneAllOutput.setViewportView(tarAllOutput);
		tabOutputTabs.insertTab("Excel Output", new ImageIcon(""), pnlExcelOutput, "Excel output testing", 0);
		tabOutputTabs.insertTab("Text Output", new ImageIcon(""), pneAllOutput, "All Output from the analysis", 0);
		tabOutputTabs.setSelectedIndex(0);

		this.setLocationRelativeTo(null);
	}

	public void addImageTreeValueChanged(TreeSelectionListener listener)
	{
		this.treImages.addTreeSelectionListener(listener);
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

	public void addALToAddNewSpecies(ActionListener listener)
	{
		this.btnAddNewSpecies.addActionListener(listener);
	}

	public void addALToRemoveLocation(ActionListener listener)
	{
		this.btnRemoveLocation.addActionListener(listener);
	}

	public void addALToRemoveSpecies(ActionListener listener)
	{
		this.btnRemoveSpecies.addActionListener(listener);
	}

	public void addALToPerformAnalysis(ActionListener listener)
	{
		this.btnPerformAnalysis.addActionListener(listener);
	}

	public void addALToAddSpeciesToList(ActionListener listener)
	{
		this.btnAddSpeciesToList.addActionListener(listener);
	}

	public void addALToRemoveSpeciesFromList(ActionListener listener)
	{
		this.btnRemoveSpeciesFromList.addActionListener(listener);
	}

	public void addALToCreateExcel(ActionListener listener)
	{
		this.btnToExcel.addActionListener(listener);
	}

	public void addALToPrgDataShow(MouseListener listener)
	{
		this.map.getPrgDataShow().addMouseListener(listener);
	}

	public void addALToPrgDataShow(MouseMotionListener listener)
	{
		this.map.getPrgDataShow().addMouseMotionListener(listener);
	}

	public Location getSelectedLocation()
	{
		if (this.cbxLocation.getSelectedIndex() == -1)
			return null;
		else
			return (Location) this.cbxLocation.getSelectedItem();
	}

	public Species getSelectedSpecies()
	{
		if (this.cbxSpecies.getSelectedIndex() == -1)
			return null;
		else
			return (Species) this.cbxSpecies.getSelectedItem();
	}

	public Integer getAnalysisEventInterval()
	{
		String value = this.txtAnalysisEventInterval.getText();
		Integer intValue = -1;
		try
		{
			intValue = Integer.parseInt(value);
		}
		catch (NumberFormatException exception)
		{
		}
		if (intValue < 1 || intValue > 10080)
		{
			JOptionPane.showMessageDialog(this, "Error invalid value for event interval '" + value + "'\nThe value must be in the range '1 <= value <= 10080'");
			return -1;
		}
		return intValue;
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
		{
			this.cbxLocation.setSelectedItem(imageLoc);
			this.map.getMapViewer().setCenterPosition(imageLoc.toGeoPosition());
		}
		else
			this.cbxLocation.setSelectedIndex(-1);
		this.refreshLocationFields();
	}

	public void setSpecies(Species species)
	{
		if (species != null)
			this.cbxSpecies.setSelectedItem(species);
		else
			this.cbxSpecies.setSelectedIndex(-1);
	}

	public void setLocationList(List<Location> locations)
	{
		if (!locations.contains((Location) this.cbxLocation.getSelectedItem()))
			this.cbxLocation.setSelectedIndex(-1);
		this.cbxLocation.removeAllItems();
		this.map.getMapViewer().clearMarkers();
		for (Location location : locations)
		{
			this.cbxLocation.addItem(location);
			this.map.getMapViewer().addMarker(new SanimalMapMarkerOverlay(location, new SanimalMapMarker()));
		}
	}

	public void setSpeciesList(List<Species> species)
	{
		Species selectedSpecies = (Species) this.cbxSpecies.getSelectedItem();
		this.cbxSpecies.removeAllItems();
		for (Species species2 : species)
			this.cbxSpecies.addItem(species2);
		if (species.contains(selectedSpecies))
			this.cbxSpecies.setSelectedItem(selectedSpecies);
	}

	public void setSpeciesEntryList(List<SpeciesEntry> speciesEntries)
	{
		if (this.lstSpecies.getModel() instanceof DefaultListModel<?>)
		{
			DefaultListModel<SpeciesEntry> items = (DefaultListModel<SpeciesEntry>) this.lstSpecies.getModel();
			items.clear();
			if (speciesEntries != null)
				for (SpeciesEntry entry : speciesEntries)
					items.addElement(entry);
		}
	}

	public void setImagesDrawnOnMap(List<ImageEntry> images)
	{
		this.map.getMapViewer().setImagesDrawnOnMap(images);
	}

	public void setImageList(ImageDirectory imageDirectory)
	{
		DefaultMutableTreeNode head = new DefaultMutableTreeNode(imageDirectory);
		this.createTreeFromImageDirectory(head, imageDirectory);
		this.treImages.setModel(new DefaultTreeModel(head));
	}

	private void createTreeFromImageDirectory(DefaultMutableTreeNode headNode, ImageDirectory headDirectory)
	{
		for (ImageEntry image : headDirectory.getImages())
			headNode.add(new DefaultMutableTreeNode(image));
		for (ImageDirectory subDirectory : headDirectory.getSubDirectories())
		{
			DefaultMutableTreeNode subDirectoryNode = new DefaultMutableTreeNode(subDirectory);
			this.createTreeFromImageDirectory(subDirectoryNode, subDirectory);
			headNode.add(subDirectoryNode);
		}
	}

	public void setOutputText(String text)
	{
		this.tarAllOutput.setText(text);
	}

	public List<ImageEntry> getSelectedImageEntries()
	{
		List<ImageEntry> entries = new ArrayList<ImageEntry>();
		for (TreePath path : this.treImages.getSelectionModel().getSelectionPaths())
		{
			Object selectedElement = path.getLastPathComponent();
			if (selectedElement instanceof DefaultMutableTreeNode)
			{
				Object selectedObject = ((DefaultMutableTreeNode) selectedElement).getUserObject();
				if (selectedObject instanceof ImageEntry)
				{
					ImageEntry current = (ImageEntry) selectedObject;
					if (!entries.contains(current))
						entries.add(current);
				}
				if (selectedObject instanceof ImageDirectory)
				{
					ImageDirectory current = (ImageDirectory) selectedObject;
					this.addAllImagesUnderDirectory(current, entries);
				}
			}
		}
		return entries;
	}

	private void addAllImagesUnderDirectory(ImageDirectory directory, List<ImageEntry> toAddTo)
	{
		for (ImageEntry entry : directory.getImages())
			if (!toAddTo.contains(entry))
				toAddTo.add(entry);
		for (ImageDirectory subDirectory : directory.getSubDirectories())
			this.addAllImagesUnderDirectory(subDirectory, toAddTo);
	}

	public List<ImageEntry> getAllTreeImageEntries()
	{
		List<ImageEntry> entries = new ArrayList<ImageEntry>();
		if (this.treImages.getModel() != null)
			if (this.treImages.getModel().getRoot() instanceof DefaultMutableTreeNode)
			{
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.treImages.getModel().getRoot();
				for (DefaultMutableTreeNode node : Collections.<DefaultMutableTreeNode> list(root.preorderEnumeration()))
					if (node.getUserObject() instanceof ImageEntry)
						entries.add((ImageEntry) node.getUserObject());
			}
		return entries;
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
}
