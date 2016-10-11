package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import library.ComboBoxFullMenu;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.image.ImageImporterData;
import model.image.ImageLoadingUtils;
import model.image.ImageUpdate;
import model.location.Location;
import model.location.LocationData;
import model.location.LocationUpdate;
import model.species.Species;
import model.species.SpeciesData;
import model.species.SpeciesEntry;
import model.species.SpeciesUpdate;
import model.timeline.TimelineData;
import model.timeline.TimelineUpdate;
import view.map.SanimalMapMarkerOverlay;

public class SanimalView extends JFrame implements Observer
{
	private static ImageIcon DEFAULT_ICON = null;

	private JToolBar barTop;
	private JButton btnSaveToFile;

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
	private JScrollPane pneExcelOutput;
	private JPanel pnlExcelOutput;
	private JButton btnToExcel;
	private JButton btnAllPictures;
	private JButton btnLoadDefaultAnimals;
	private JRadioButton radNumPictures;
	private JRadioButton radAbundance;
	private JRadioButton radPeriod;
	private JRadioButton radActivity;
	private ButtonGroup grpDataType;
	private JButton btnLoadFromFile;

	private ImageView imageView = new ImageView();

	public SanimalView()
	{
		this.getContentPane().setLayout(null);
		this.setResizable(true);
		this.setTitle("Scientific Animal Image AnaLysis (SANIMAL)");
		this.setSize(1334, 713);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		barTop = new JToolBar(JToolBar.HORIZONTAL);
		barTop.setBounds(10, 11, 1308, 48);
		this.getContentPane().add(barTop);

		btnSaveToFile = new JButton();
		btnSaveToFile.setMaximumSize(new Dimension(40, 40));
		btnSaveToFile.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnSaveToFile.setToolTipText("Save project");
		btnSaveToFile.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		btnSaveToFile.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/saveIcon.png")), 38, 38, Image.SCALE_SMOOTH, false));
		barTop.add(btnSaveToFile);

		btnLoadFromFile = new JButton();
		btnLoadFromFile.setMaximumSize(new Dimension(40, 40));
		btnLoadFromFile.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnLoadFromFile.setToolTipText("Load project");
		btnLoadFromFile.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		barTop.add(btnLoadFromFile);
		btnLoadFromFile.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/loadIcon.png")), 38, 38, Image.SCALE_SMOOTH, false));

		barTop.addSeparator();

		btnPerformAnalysis = new JButton();
		btnPerformAnalysis.setMaximumSize(new Dimension(40, 40));
		btnPerformAnalysis.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnPerformAnalysis.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		btnPerformAnalysis.setToolTipText("Re-create 'Output.txt'");
		btnPerformAnalysis.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/performAnalysis.png")), 38, 38, Image.SCALE_SMOOTH, false));
		barTop.add(btnPerformAnalysis);

		btnAllPictures = new JButton();
		btnAllPictures.setMaximumSize(new Dimension(40, 40));
		btnAllPictures.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnAllPictures.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		btnAllPictures.setToolTipText("Create all pictures output");
		btnAllPictures.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/allPicturesOutput.png")), 38, 38, Image.SCALE_SMOOTH, false));
		barTop.add(btnAllPictures);

		btnToExcel = new JButton();
		btnToExcel.setMaximumSize(new Dimension(40, 40));
		btnToExcel.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnToExcel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		btnToExcel.setToolTipText("Create excel file");
		btnToExcel.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/excelOutput.png")), 38, 38, Image.SCALE_SMOOTH, false));
		barTop.add(btnToExcel);

		barTop.addSeparator();

		btnLoadDefaultAnimals = new JButton();
		btnLoadDefaultAnimals.setMaximumSize(new Dimension(40, 40));
		btnLoadDefaultAnimals.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnLoadDefaultAnimals.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		btnLoadDefaultAnimals.setToolTipText("Load default desert animals");
		btnLoadDefaultAnimals.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/paw.png")), 38, 38, Image.SCALE_SMOOTH, false));
		barTop.add(btnLoadDefaultAnimals);

		lblThumbnail = new JLabel();
		lblThumbnail.setBounds(278, 70, 391, 241);
		lblThumbnail.setBorder(new LineBorder(Color.BLACK));
		lblThumbnail.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseReleased(MouseEvent event)
			{
				if (!imageView.isVisible())
					imageView.setVisible(true);
			}

			@Override
			public void mousePressed(MouseEvent event)
			{
			}

			@Override
			public void mouseExited(MouseEvent event)
			{
			}

			@Override
			public void mouseEntered(MouseEvent event)
			{
			}

			@Override
			public void mouseClicked(MouseEvent event)
			{
			}
		});
		this.getContentPane().add(lblThumbnail);

		if (DEFAULT_ICON == null)
			DEFAULT_ICON = ImageLoadingUtils.resizeImageIcon(new ImageIcon(ImageLoadingUtils.retrieveImageResource("loadingImageIcon.png").getFile()), this.lblThumbnail.getWidth(), this.lblThumbnail.getHeight(), Image.SCALE_SMOOTH, false);

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

		lblAnalysisEventInterval = new JLabel("Event Interval (minutes): ");
		lblAnalysisEventInterval.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblAnalysisEventInterval.setBounds(10, 167, 167, 14);
		pnlPropertyList.add(lblAnalysisEventInterval);

		txtAnalysisEventInterval = new JTextField();
		txtAnalysisEventInterval.setText("60");
		txtAnalysisEventInterval.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtAnalysisEventInterval.setBounds(187, 164, 230, 20);
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
		pnlImageBrowser.setBounds(10, 70, 258, 241);
		pnlImageBrowser.setBorder(new LineBorder(Color.BLACK));
		pnlImageBrowser.setLayout(null);
		getContentPane().add(pnlImageBrowser);

		chxIncludeSubdirectories = new JCheckBox("Include Subdirectories");
		chxIncludeSubdirectories.setFont(new Font("Tahoma", Font.PLAIN, 10));
		chxIncludeSubdirectories.setSelected(true);
		chxIncludeSubdirectories.setBounds(115, 206, 141, 23);
		chxIncludeSubdirectories.setToolTipText("Search sub-directories as well as the selected directory for images");
		pnlImageBrowser.add(chxIncludeSubdirectories);

		btnBrowseForImages = new JButton("Select Images");
		btnBrowseForImages.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnBrowseForImages.setBounds(10, 206, 99, 23);
		pnlImageBrowser.add(btnBrowseForImages);

		pneImageList = new JScrollPane();
		treImages = new JTree((TreeModel) null);
		pneImageList.setBounds(10, 11, 237, 188);
		pneImageList.setViewportView(treImages);
		pnlImageBrowser.add(pneImageList);

		map = new MapPanel();
		map.setSize(638, 604);
		map.setLocation(680, 70);
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

		pneExcelOutput = new JScrollPane();
		pneExcelOutput.setFont(new Font("Tahoma", Font.PLAIN, 10));
		pneExcelOutput.setViewportView(pnlExcelOutput);

		radNumPictures = new JRadioButton("Picture Count");
		radNumPictures.setSelected(true);
		radNumPictures.setFont(new Font("Tahoma", Font.PLAIN, 14));
		radNumPictures.setBounds(6, 7, 170, 23);
		pnlExcelOutput.add(radNumPictures);

		radActivity = new JRadioButton("Picture Activity");
		radActivity.setFont(new Font("Tahoma", Font.PLAIN, 14));
		radActivity.setBounds(6, 33, 170, 23);
		pnlExcelOutput.add(radActivity);

		radAbundance = new JRadioButton("Picture Abundance");
		radAbundance.setFont(new Font("Tahoma", Font.PLAIN, 14));
		radAbundance.setBounds(6, 59, 170, 23);
		pnlExcelOutput.add(radAbundance);

		radPeriod = new JRadioButton("Picture Period");
		radPeriod.setFont(new Font("Tahoma", Font.PLAIN, 14));
		radPeriod.setBounds(6, 85, 170, 23);
		pnlExcelOutput.add(radPeriod);

		grpDataType = new ButtonGroup();
		grpDataType.add(radNumPictures);
		grpDataType.add(radActivity);
		grpDataType.add(radAbundance);
		grpDataType.add(radPeriod);

		pneAllOutput = new JScrollPane();
		pneAllOutput.setViewportView(tarAllOutput);
		//tabOutputTabs.insertTab("Output Settings", new ImageIcon(""), pneExcelOutput, "Excel output testing", 0);
		tabOutputTabs.insertTab("Text Output", new ImageIcon(""), pneAllOutput, "All Output from the analysis", 0);
		tabOutputTabs.setSelectedIndex(0);

		this.getContentPane().addComponentListener(new ComponentListener()
		{
			@Override
			public void componentShown(ComponentEvent event)
			{
			}

			@Override
			public void componentResized(ComponentEvent event)
			{
				barTop.setBounds(barTop.getX(), barTop.getY(), event.getComponent().getWidth() - 20, barTop.getHeight());
				map.setBounds(map.getX(), map.getY(), event.getComponent().getWidth() - 690, event.getComponent().getHeight() - 75);
				tabOutputTabs.setBounds(tabOutputTabs.getX(), tabOutputTabs.getY(), tabOutputTabs.getWidth(), event.getComponent().getHeight() - 545);
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

	public void addALToAllPictures(ActionListener listener)
	{
		this.btnAllPictures.addActionListener(listener);
	}

	public void addALToPrgDataShow(MouseListener listener)
	{
		this.map.getPrgDataShow().addMouseListener(listener);
	}

	public void addALToPrgDataShow(MouseMotionListener listener)
	{
		this.map.getPrgDataShow().addMouseMotionListener(listener);
	}

	public void addALToSave(ActionListener listener)
	{
		this.btnSaveToFile.addActionListener(listener);
	}

	public void addALToLoad(ActionListener listener)
	{
		this.btnLoadFromFile.addActionListener(listener);
	}

	public void addALToLoadDefaultAnimals(ActionListener listener)
	{
		this.btnLoadDefaultAnimals.addActionListener(listener);
	}

	public MapPanel getMapPanel()
	{
		return this.map;
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

	public Species getSelectedSpeciesFromList()
	{
		if (this.lstSpecies.getSelectedIndex() == -1)
			return null;
		else
			return ((SpeciesEntry) (this.lstSpecies.getSelectedValue())).getSpecies();
	}

	/**
	 * @return 0 for number of pictures, 1 for abundance, 2 for activity, and 3 for period
	 */
	public Integer getSelectedDataTypeRadioButton()
	{
		return radNumPictures.isSelected() ? 0 : radAbundance.isSelected() ? 1 : radActivity.isSelected() ? 2 : 3;
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
		{
			SanimalIconLoader.getInstance().scheduleTask(() ->
			{
				this.lblThumbnail.setIcon(image.createIcon(this.lblThumbnail.getWidth(), this.lblThumbnail.getHeight()));
				this.imageView.setDisplayImage(image);
				if (this.imageView.isVisible())
					this.imageView.refreshIcon();
			});
		}
		else
		{
			this.lblThumbnail.setIcon(null);
			this.imageView.setDisplayImage(null);
		}
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

	public void setSpecies(Species species)
	{
		if (species != null)
			this.cbxSpecies.setSelectedItem(species);
		else
			this.cbxSpecies.setSelectedIndex(-1);
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

	public void setOutputText(String text)
	{
		this.tarAllOutput.setText(text);
	}

	public void addDropTarget(DropTargetListener listener)
	{
		new DropTarget(this, listener);
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

	@Override
	public void update(Observable observable, Object argument)
	{
		if (observable instanceof LocationData && argument instanceof LocationUpdate)
		{
			LocationData locationData = (LocationData) observable;
			LocationUpdate locationUpdate = (LocationUpdate) argument;
			if (locationUpdate == LocationUpdate.LocationListChange)
				this.setLocationList(locationData.getRegisteredLocations());
		}
		else if (observable instanceof SpeciesData && argument instanceof SpeciesUpdate)
		{
			SpeciesData speciesData = (SpeciesData) observable;
			SpeciesUpdate speciesUpdate = (SpeciesUpdate) argument;
			if (speciesUpdate == SpeciesUpdate.SpeciesListChanged)
				this.setSpeciesList(speciesData.getRegisteredSpecies());
		}
		else if (observable instanceof ImageImporterData && argument instanceof ImageUpdate)
		{
			ImageImporterData imageImporterData = (ImageImporterData) observable;
			ImageUpdate imageUpdate = (ImageUpdate) argument;
			if (imageUpdate == ImageUpdate.NewDirectorySelected)
				this.setImageList(imageImporterData.getHeadDirectory());
			else if (imageUpdate == ImageUpdate.InvalidImageContainersDetected)
				SanimalInput.askUserToValidateProject(imageImporterData.getInvalidContainers());
		}
		else if (observable instanceof TimelineData && argument instanceof TimelineUpdate)
		{
			TimelineData timelineData = (TimelineData) observable;
			TimelineUpdate timelineUpdate = (TimelineUpdate) argument;
			if (timelineUpdate == TimelineUpdate.NewImageListToDisplay)
			{
				this.setImagesDrawnOnMap(timelineData.getImagesToDisplay());
				this.map.getPrgDataShow().setValue((int) Math.round(100D * timelineData.getPercentageAcrossDisplayedImages()));
				this.map.setCurrentDateLabel(timelineData.getCenterDayAsDate());
			}
		}
	}

	///
	/// Begin observer utility functions
	///

	private void setLocationList(List<Location> locations)
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

	private void setSpeciesList(List<Species> species)
	{
		Species selectedSpecies = (Species) this.cbxSpecies.getSelectedItem();
		this.cbxSpecies.removeAllItems();
		for (Species species2 : species)
			this.cbxSpecies.addItem(species2);
		if (species.contains(selectedSpecies))
			this.cbxSpecies.setSelectedItem(selectedSpecies);
	}

	private void setImageList(ImageDirectory imageDirectory)
	{
		if (imageDirectory == null)
			this.treImages.setModel((TreeModel) null);
		else
		{
			DefaultMutableTreeNode head = new DefaultMutableTreeNode(imageDirectory);
			this.createTreeFromImageDirectory(head, imageDirectory);
			this.treImages.setModel(new DefaultTreeModel(head));
		}
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

	private void setImagesDrawnOnMap(List<ImageEntry> images)
	{
		this.map.getMapViewer().setImagesDrawnOnMap(images);
	}
}
