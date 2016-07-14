package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import view.map.SwingComponentOverlay;

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
	private JButton btnAddSpeciesToList;
	private JButton btnRemoveSpeciesFromList;
	private JPanel pnlSpeciesPresent;
	private JLabel lblSpeciesEntries;
	private JScrollPane pneSpeciesList;
	private JList lstSpecies;

	private JPanel pnlMap;
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

	private JTabbedPane tabOutputTabs;
	private JScrollPane pneAllOutput;
	private JTextArea tarAllOutput;

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

		pnlMap = new JPanel();
		pnlMap.setBounds(679, 11, 638, 666);
		pnlMap.setBorder(new LineBorder(Color.BLACK));
		pnlMap.setLayout(null);
		getContentPane().add(pnlMap);

		lblMapProvider = new JLabel("Map Provider:");
		lblMapProvider.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblMapProvider.setBounds(10, 10, 95, 14);
		pnlMap.add(lblMapProvider);

		cbxMapProviders = new ComboBoxFullMenu<String>();
		cbxMapProviders.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cbxMapProviders.setBounds(115, 6, 167, 23);
		pnlMap.add(cbxMapProviders);

		mapViewer = new SanimalMap(cbxMapProviders);
		mapViewer.setLayout(null);
		mapViewer.setBounds(0, 60, 637, 564);
		mapViewer.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		mapViewer.addMouseWheelListener(new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent event)
			{
				lblZoomLevel.setText(zoomLevelBase + mapViewer.getZoom());
				lblCurrentLat.setText(currentLatBase + String.format("%7.6f", mapViewer.getCenterPosition().getLatitude()));
				lblCurrentLng.setText(currentLngBase + String.format("%7.6f", mapViewer.getCenterPosition().getLongitude()));
				double maxZoom = (double) mapViewer.getTileFactory().getInfo().getMaximumZoomLevel();
				double currZoom = (double) mapViewer.getZoom();
				mapViewer.setMarkerScale((maxZoom - currZoom) / maxZoom);
			}
		});
		mapViewer.addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
			}

			@Override
			public void mouseDragged(MouseEvent event)
			{
				SanimalView.this.lblCurrentLat.setText(currentLatBase + String.format("%7.6f", mapViewer.getCenterPosition().getLatitude()));
				SanimalView.this.lblCurrentLng.setText(currentLngBase + String.format("%7.6f", mapViewer.getCenterPosition().getLongitude()));
			}
		});
		pnlMap.add(mapViewer);

		lblZoomLevel = new JLabel(zoomLevelBase + mapViewer.getZoom());
		lblZoomLevel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblZoomLevel.setBounds(10, 35, 105, 14);
		pnlMap.add(lblZoomLevel);

		lblCurrentLat = new JLabel(currentLatBase + String.format("%7.6f", mapViewer.getCenterPosition().getLatitude()));
		lblCurrentLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCurrentLat.setBounds(292, 10, 159, 14);
		pnlMap.add(lblCurrentLat);

		lblCurrentLng = new JLabel(currentLngBase + String.format("%7.6f", mapViewer.getCenterPosition().getLongitude()));
		lblCurrentLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCurrentLng.setBounds(292, 35, 159, 14);
		pnlMap.add(lblCurrentLng);

		btnTop = new JButton(new ImageIcon("images/Top2.png"));
		btnTop.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnTop.setBounds(10, 635, 20, 20);
		pnlMap.add(btnTop);

		btnBackwards = new JButton(new ImageIcon("images/Backward2.png"));
		btnBackwards.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnBackwards.setBounds(40, 635, 20, 20);
		pnlMap.add(btnBackwards);

		btnPrevious = new JButton(new ImageIcon("images/Previous2.png"));
		btnPrevious.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnPrevious.setBounds(70, 635, 20, 20);
		pnlMap.add(btnPrevious);

		btnStop = new JButton(new ImageIcon("images/Stop2.png"));
		btnStop.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnStop.setBounds(100, 635, 20, 20);
		pnlMap.add(btnStop);

		btnNext = new JButton(new ImageIcon("images/Next2.png"));
		btnNext.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnNext.setBounds(130, 635, 20, 20);
		pnlMap.add(btnNext);

		btnForward = new JButton(new ImageIcon("images/Forward2.png"));
		btnForward.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnForward.setBounds(160, 635, 20, 20);
		pnlMap.add(btnForward);

		btnBottom = new JButton(new ImageIcon("images/Bottom2.png"));
		btnBottom.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnBottom.setBounds(190, 635, 20, 20);
		pnlMap.add(btnBottom);

		sldSpeed = new JSlider(SwingConstants.HORIZONTAL);
		sldSpeed.setMinorTickSpacing(1);
		sldSpeed.setValue(0);
		sldSpeed.setPaintTicks(true);
		sldSpeed.setSnapToTicks(true);
		sldSpeed.setBounds(220, 632, 68, 23);
		sldSpeed.setMinimum(0);
		sldSpeed.setMaximum(5);
		sldSpeed.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent event)
			{
				lblSpeed.setText(sldSpeed.getValue() + "x");
			}
		});
		pnlMap.add(sldSpeed);

		lblSpeed = new JLabel("1x");
		lblSpeed.setBounds(292, 632, 26, 23);
		pnlMap.add(lblSpeed);
		lblSpeed.setFont(new Font("Tahoma", Font.PLAIN, 16));

		prgDataShow = new JProgressBar(SwingConstants.HORIZONTAL);
		prgDataShow.setBounds(328, 632, 300, 23);
		prgDataShow.setMinimum(0);
		prgDataShow.setMaximum(100);
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
		pnlMap.add(prgDataShow);

		tabOutputTabs = new JTabbedPane();
		tabOutputTabs.setBounds(10, 535, 659, 142);
		tabOutputTabs.setSelectedIndex(-1);
		this.getContentPane().add(tabOutputTabs);

		tarAllOutput = new JTextArea();
		tarAllOutput.setBounds(10, 11, 100, 100);
		tarAllOutput.setLayout(null);

		pneAllOutput = new JScrollPane();
		pneAllOutput.setViewportView(tarAllOutput);
		tabOutputTabs.insertTab("All Output", new ImageIcon(""), pneAllOutput, "All Output from the analysis", 0);

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

	public void addALToAddSpeciesToList(ActionListener listener)
	{
		this.btnAddSpeciesToList.addActionListener(listener);
	}

	public void addALToRemoveSpeciesFromList(ActionListener listener)
	{
		this.btnRemoveSpeciesFromList.addActionListener(listener);
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
			mapViewer.setCenterPosition(imageLoc.toGeoPosition());
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
		this.mapViewer.clearMarkers();
		for (Location location : locations)
		{
			this.cbxLocation.addItem(location);
			this.mapViewer.addMarker(new SwingComponentOverlay(location.toGeoPosition(), new SanimalMapMarker()));
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

	public Species askUserForNewSpecies()
	{
		String name = "";
		while (name.isEmpty())
		{
			name = JOptionPane.showInputDialog("Enter the name of the new species");
			if (name == null)
				return null;
		}
		return new Species(name);
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
		while (latitude > 85 || latitude < -85)
		{
			try
			{
				String latitudeString = JOptionPane.showInputDialog("Enter the latitude (+/- 85) of location '" + name + "'");
				if (latitudeString == null)
					return null;
				latitude = Double.parseDouble(latitudeString);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		Double longitude = Double.MAX_VALUE;
		while (longitude > 180 || longitude < -180)
		{
			try
			{
				String longitudeString = JOptionPane.showInputDialog("Enter the longitude (+/- 180) of location '" + name + "'");
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
				String elevationString = JOptionPane.showInputDialog("Enter the elevation (in feet) of location '" + name + "'");
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
