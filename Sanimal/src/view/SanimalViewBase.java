/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import javax.swing.tree.TreeModel;

import library.ComboBoxFullMenu;
import model.image.ImageLoadingUtils;
import model.location.Location;
import model.species.Species;

public abstract class SanimalViewBase extends JFrame
{
	protected JToolBar barTop;
	protected JButton btnSaveToFile;

	protected JPanel pnlImageBrowser;
	protected JTree treImages;
	protected JTextField txtDate;
	protected JLabel lblDate;
	protected JCheckBox chxIncludeSubdirectories;
	protected JButton btnBrowseForImages;
	protected JScrollPane pneImageList;

	protected JPanel pnlThumbnailSettings;
	protected JButton btnShowPreview;
	protected JButton btnResetPreview;
	protected JLabel lblBrightness;
	protected JSlider sldBrightness;
	protected JLabel lblContrast;
	protected JSlider sldContrast;

	protected JPanel pnlPropertyList;
	protected JLabel lblLocation;
	protected ComboBoxFullMenu<Location> cbxLocation;
	protected JButton btnAddNewLocation;
	protected JButton btnRemoveLocation;
	protected JLabel lblLocationLat;
	protected JTextField txtLat;
	protected JLabel lblLocationLng;
	protected JTextField txtLng;
	protected JLabel lblLocationElevation;
	protected JTextField txtElevation;
	protected JLabel lblSpecies;
	protected ComboBoxFullMenu<Species> cbxSpecies;
	protected JButton btnAddNewSpecies;
	protected JButton btnRemoveSpecies;
	protected JButton btnPerformAnalysis;
	protected JLabel lblAnalysisEventInterval;
	protected JTextField txtAnalysisEventInterval;

	protected JButton btnAddSpeciesToList;
	protected JButton btnRemoveSpeciesFromList;
	protected JPanel pnlSpeciesPresent;
	protected JLabel lblSpeciesEntries;
	protected JScrollPane pneSpeciesList;
	protected JList lstSpecies;

	protected MapPanel map;

	protected JTabbedPane tabOutputTabs;
	protected JScrollPane pneAllOutput;
	protected JTextArea tarAllOutput;
	protected JScrollPane pneExcelOutput;
	protected JPanel pnlExcelOutput;
	protected JButton btnToExcel;
	protected JButton btnAllPictures;
	protected JButton btnLoadDefaultAnimals;

	protected JLabel lblYearOnly;
	protected JTextField txtYearOnly;
	protected JLabel lblMonthOnly;
	protected JTextField txtMonthOnly;
	protected JLabel lblTimeFrameStart;
	protected JTextField txtTimeFrameStart;
	protected JLabel lblTimeFrameEnd;
	protected JTextField txtTimeFramEnd;
	protected JCheckBox cbxSpeciesOnly;
	protected JCheckBox cbxLocationOnly;
	/*
	protected JRadioButton radNumPictures;
	protected JRadioButton radAbundance;
	protected JRadioButton radPeriod;
	protected JRadioButton radActivity;
	protected ButtonGroup grpDataType;
	*/
	protected JButton btnLoadFromFile;

	public SanimalViewBase()
	{
		SpringLayout contentPaneLayout = new SpringLayout();
		this.getContentPane().setLayout(contentPaneLayout);

		barTop = new JToolBar(JToolBar.HORIZONTAL);
		contentPaneLayout.putConstraint(SpringLayout.NORTH, barTop, 0, SpringLayout.NORTH, getContentPane());
		contentPaneLayout.putConstraint(SpringLayout.WEST, barTop, 0, SpringLayout.WEST, getContentPane());
		contentPaneLayout.putConstraint(SpringLayout.SOUTH, barTop, 50, SpringLayout.NORTH, getContentPane());
		contentPaneLayout.putConstraint(SpringLayout.EAST, barTop, 0, SpringLayout.EAST, getContentPane());
		this.getContentPane().add(barTop);

		btnSaveToFile = new JButton();
		btnSaveToFile.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnSaveToFile.setToolTipText("Save project");
		btnSaveToFile.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		btnSaveToFile.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/saveIcon.png")), 38, 38, Image.SCALE_SMOOTH, false));
		barTop.add(btnSaveToFile);

		btnLoadFromFile = new JButton();
		btnLoadFromFile.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnLoadFromFile.setToolTipText("Load project");
		btnLoadFromFile.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		barTop.add(btnLoadFromFile);
		btnLoadFromFile.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/loadIcon.png")), 38, 38, Image.SCALE_SMOOTH, false));

		barTop.addSeparator();

		btnPerformAnalysis = new JButton();
		btnPerformAnalysis.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnPerformAnalysis.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		btnPerformAnalysis.setToolTipText("Re-create 'Output.txt'");
		btnPerformAnalysis.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/performAnalysis.png")), 38, 38, Image.SCALE_SMOOTH, false));
		barTop.add(btnPerformAnalysis);

		btnAllPictures = new JButton();
		btnAllPictures.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnAllPictures.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		btnAllPictures.setToolTipText("Create all pictures output");
		btnAllPictures.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/allPicturesOutput.png")), 38, 38, Image.SCALE_SMOOTH, false));
		barTop.add(btnAllPictures);

		btnToExcel = new JButton();
		btnToExcel.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnToExcel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		btnToExcel.setToolTipText("Create excel table");
		btnToExcel.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/excelOutput.png")), 38, 38, Image.SCALE_SMOOTH, false));
		barTop.add(btnToExcel);

		barTop.addSeparator();

		btnLoadDefaultAnimals = new JButton();
		btnLoadDefaultAnimals.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnLoadDefaultAnimals.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		btnLoadDefaultAnimals.setToolTipText("Load default desert animals");
		btnLoadDefaultAnimals.setIcon(ImageLoadingUtils.resizeImageIcon(new ImageIcon(SanimalView.class.getResource("/images/paw.png")), 38, 38, Image.SCALE_SMOOTH, false));
		barTop.add(btnLoadDefaultAnimals);

		pnlImageBrowser = new JPanel();
		contentPaneLayout.putConstraint(SpringLayout.NORTH, pnlImageBrowser, 0, SpringLayout.SOUTH, barTop);
		contentPaneLayout.putConstraint(SpringLayout.WEST, pnlImageBrowser, 10, SpringLayout.WEST, getContentPane());
		contentPaneLayout.putConstraint(SpringLayout.SOUTH, pnlImageBrowser, 280, SpringLayout.SOUTH, barTop);
		contentPaneLayout.putConstraint(SpringLayout.EAST, pnlImageBrowser, 247, SpringLayout.WEST, getContentPane());
		pnlImageBrowser.setBorder(new LineBorder(Color.BLACK));
		SpringLayout imageBrowserLayout = new SpringLayout();
		pnlImageBrowser.setLayout(imageBrowserLayout);
		getContentPane().add(pnlImageBrowser);

		chxIncludeSubdirectories = new JCheckBox("Include Subdirectories");
		imageBrowserLayout.putConstraint(SpringLayout.SOUTH, chxIncludeSubdirectories, -10, SpringLayout.SOUTH, pnlImageBrowser);
		imageBrowserLayout.putConstraint(SpringLayout.EAST, chxIncludeSubdirectories, -10, SpringLayout.EAST, pnlImageBrowser);
		chxIncludeSubdirectories.setFont(new Font("Tahoma", Font.PLAIN, 10));
		chxIncludeSubdirectories.setSelected(true);
		chxIncludeSubdirectories.setToolTipText("Search sub-directories as well as the selected directory for images");
		pnlImageBrowser.add(chxIncludeSubdirectories);

		btnBrowseForImages = new JButton("Select Images");
		imageBrowserLayout.putConstraint(SpringLayout.NORTH, btnBrowseForImages, 0, SpringLayout.NORTH, chxIncludeSubdirectories);
		imageBrowserLayout.putConstraint(SpringLayout.WEST, btnBrowseForImages, 5, SpringLayout.WEST, pnlImageBrowser);
		btnBrowseForImages.setFont(new Font("Tahoma", Font.PLAIN, 10));
		pnlImageBrowser.add(btnBrowseForImages);

		pneImageList = new JScrollPane();
		imageBrowserLayout.putConstraint(SpringLayout.NORTH, pneImageList, 0, SpringLayout.NORTH, pnlImageBrowser);
		imageBrowserLayout.putConstraint(SpringLayout.WEST, pneImageList, 0, SpringLayout.WEST, pnlImageBrowser);
		imageBrowserLayout.putConstraint(SpringLayout.SOUTH, pneImageList, -6, SpringLayout.NORTH, chxIncludeSubdirectories);
		imageBrowserLayout.putConstraint(SpringLayout.EAST, pneImageList, 0, SpringLayout.EAST, pnlImageBrowser);
		treImages = new JTree((TreeModel) null);
		pneImageList.setViewportView(treImages);
		pnlImageBrowser.add(pneImageList);

		pnlThumbnailSettings = new JPanel();
		SpringLayout thumbnailSettingsLayout = new SpringLayout();
		pnlThumbnailSettings.setLayout(thumbnailSettingsLayout);
		contentPaneLayout.putConstraint(SpringLayout.NORTH, pnlThumbnailSettings, 0, SpringLayout.SOUTH, barTop);
		contentPaneLayout.putConstraint(SpringLayout.WEST, pnlThumbnailSettings, 5, SpringLayout.EAST, pnlImageBrowser);
		contentPaneLayout.putConstraint(SpringLayout.SOUTH, pnlThumbnailSettings, 0, SpringLayout.SOUTH, pnlImageBrowser);
		pnlThumbnailSettings.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(pnlThumbnailSettings);

		btnShowPreview = new JButton("Show Image Preview");
		thumbnailSettingsLayout.putConstraint(SpringLayout.NORTH, btnShowPreview, 10, SpringLayout.NORTH, pnlThumbnailSettings);
		thumbnailSettingsLayout.putConstraint(SpringLayout.WEST, btnShowPreview, 10, SpringLayout.WEST, pnlThumbnailSettings);
		btnShowPreview.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlThumbnailSettings.add(btnShowPreview);

		btnResetPreview = new JButton("Reset Image Preview");
		thumbnailSettingsLayout.putConstraint(SpringLayout.NORTH, btnResetPreview, 0, SpringLayout.NORTH, btnShowPreview);
		thumbnailSettingsLayout.putConstraint(SpringLayout.WEST, btnResetPreview, 6, SpringLayout.EAST, btnShowPreview);
		btnResetPreview.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlThumbnailSettings.add(btnResetPreview);

		lblBrightness = new JLabel("Brightness: ");
		thumbnailSettingsLayout.putConstraint(SpringLayout.NORTH, lblBrightness, 12, SpringLayout.SOUTH, btnShowPreview);
		thumbnailSettingsLayout.putConstraint(SpringLayout.WEST, lblBrightness, 0, SpringLayout.WEST, btnShowPreview);
		lblBrightness.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlThumbnailSettings.add(lblBrightness);

		sldBrightness = new JSlider(JSlider.HORIZONTAL, -25, 25, 1);
		thumbnailSettingsLayout.putConstraint(SpringLayout.WEST, sldBrightness, 6, SpringLayout.EAST, lblBrightness);
		thumbnailSettingsLayout.putConstraint(SpringLayout.SOUTH, sldBrightness, 0, SpringLayout.SOUTH, lblBrightness);
		pnlThumbnailSettings.add(sldBrightness);

		lblContrast = new JLabel("Contrast: ");
		thumbnailSettingsLayout.putConstraint(SpringLayout.NORTH, lblContrast, 12, SpringLayout.SOUTH, lblBrightness);
		thumbnailSettingsLayout.putConstraint(SpringLayout.WEST, lblContrast, 0, SpringLayout.WEST, btnShowPreview);
		lblContrast.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlThumbnailSettings.add(lblContrast);

		sldContrast = new JSlider(JSlider.HORIZONTAL, 0, 50, 25);
		thumbnailSettingsLayout.putConstraint(SpringLayout.WEST, sldContrast, 0, SpringLayout.WEST, sldBrightness);
		thumbnailSettingsLayout.putConstraint(SpringLayout.SOUTH, sldContrast, 0, SpringLayout.SOUTH, lblContrast);
		pnlThumbnailSettings.add(sldContrast);

		pnlPropertyList = new JPanel();
		contentPaneLayout.putConstraint(SpringLayout.NORTH, pnlPropertyList, 6, SpringLayout.SOUTH, pnlImageBrowser);
		contentPaneLayout.putConstraint(SpringLayout.WEST, pnlPropertyList, 9, SpringLayout.WEST, getContentPane());
		SpringLayout propertyListLayout = new SpringLayout();
		pnlPropertyList.setLayout(propertyListLayout);
		pnlPropertyList.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(pnlPropertyList);

		lblDate = new JLabel("Date Taken:");
		lblDate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlPropertyList.add(lblDate);

		txtDate = new JTextField();
		propertyListLayout.putConstraint(SpringLayout.NORTH, txtDate, -3, SpringLayout.NORTH, lblDate);
		propertyListLayout.putConstraint(SpringLayout.WEST, txtDate, 6, SpringLayout.EAST, lblDate);
		txtDate.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtDate.setEditable(false);
		pnlPropertyList.add(txtDate);

		lblLocation = new JLabel("Location: ");
		propertyListLayout.putConstraint(SpringLayout.WEST, lblDate, 0, SpringLayout.WEST, lblLocation);
		propertyListLayout.putConstraint(SpringLayout.SOUTH, lblDate, -10, SpringLayout.NORTH, lblLocation);
		lblLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlPropertyList.add(lblLocation);

		cbxLocation = new ComboBoxFullMenu<Location>();
		propertyListLayout.putConstraint(SpringLayout.NORTH, cbxLocation, -3, SpringLayout.NORTH, lblLocation);
		cbxLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cbxLocation.setSelectedIndex(-1);
		pnlPropertyList.add(cbxLocation);

		btnAddNewLocation = new JButton("Add");
		propertyListLayout.putConstraint(SpringLayout.NORTH, btnAddNewLocation, -4, SpringLayout.NORTH, lblLocation);
		propertyListLayout.putConstraint(SpringLayout.WEST, btnAddNewLocation, 6, SpringLayout.EAST, cbxLocation);
		btnAddNewLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlPropertyList.add(btnAddNewLocation);

		btnRemoveLocation = new JButton("Remove");
		propertyListLayout.putConstraint(SpringLayout.EAST, txtDate, 0, SpringLayout.EAST, btnRemoveLocation);
		propertyListLayout.putConstraint(SpringLayout.NORTH, btnRemoveLocation, -4, SpringLayout.NORTH, lblLocation);
		propertyListLayout.putConstraint(SpringLayout.WEST, btnRemoveLocation, 6, SpringLayout.EAST, btnAddNewLocation);
		btnRemoveLocation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlPropertyList.add(btnRemoveLocation);

		lblLocationLat = new JLabel("Latitude: ");
		propertyListLayout.putConstraint(SpringLayout.WEST, lblLocation, 0, SpringLayout.WEST, lblLocationLat);
		propertyListLayout.putConstraint(SpringLayout.SOUTH, lblLocation, -10, SpringLayout.NORTH, lblLocationLat);
		lblLocationLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlPropertyList.add(lblLocationLat);

		txtLat = new JTextField();
		propertyListLayout.putConstraint(SpringLayout.WEST, cbxLocation, 0, SpringLayout.WEST, txtLat);
		propertyListLayout.putConstraint(SpringLayout.NORTH, txtLat, -3, SpringLayout.NORTH, lblLocationLat);
		txtLat.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtLat.setEditable(false);
		pnlPropertyList.add(txtLat);

		lblLocationLng = new JLabel("Longitude: ");
		propertyListLayout.putConstraint(SpringLayout.WEST, lblLocationLat, 0, SpringLayout.WEST, lblLocationLng);
		propertyListLayout.putConstraint(SpringLayout.SOUTH, lblLocationLat, -10, SpringLayout.NORTH, lblLocationLng);
		lblLocationLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlPropertyList.add(lblLocationLng);

		txtLng = new JTextField();
		propertyListLayout.putConstraint(SpringLayout.WEST, txtLat, 0, SpringLayout.WEST, txtLng);
		propertyListLayout.putConstraint(SpringLayout.EAST, txtLat, 0, SpringLayout.EAST, txtLng);
		propertyListLayout.putConstraint(SpringLayout.NORTH, txtLng, -3, SpringLayout.NORTH, lblLocationLng);
		txtLng.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtLng.setEditable(false);
		pnlPropertyList.add(txtLng);

		lblLocationElevation = new JLabel("Elevation: ");
		propertyListLayout.putConstraint(SpringLayout.WEST, lblLocationLng, 0, SpringLayout.WEST, lblLocationElevation);
		propertyListLayout.putConstraint(SpringLayout.SOUTH, lblLocationLng, -10, SpringLayout.NORTH, lblLocationElevation);
		lblLocationElevation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlPropertyList.add(lblLocationElevation);

		txtElevation = new JTextField();
		propertyListLayout.putConstraint(SpringLayout.WEST, txtLng, 0, SpringLayout.WEST, txtElevation);
		propertyListLayout.putConstraint(SpringLayout.EAST, txtLng, 0, SpringLayout.EAST, txtElevation);
		propertyListLayout.putConstraint(SpringLayout.NORTH, txtElevation, -3, SpringLayout.NORTH, lblLocationElevation);
		txtElevation.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtElevation.setEditable(false);
		pnlPropertyList.add(txtElevation);

		lblSpecies = new JLabel("Species List: ");
		propertyListLayout.putConstraint(SpringLayout.WEST, lblLocationElevation, 0, SpringLayout.WEST, lblSpecies);
		propertyListLayout.putConstraint(SpringLayout.SOUTH, lblLocationElevation, -10, SpringLayout.NORTH, lblSpecies);
		lblSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlPropertyList.add(lblSpecies);

		cbxSpecies = new ComboBoxFullMenu<Species>();
		propertyListLayout.putConstraint(SpringLayout.EAST, cbxLocation, 0, SpringLayout.EAST, cbxSpecies);
		propertyListLayout.putConstraint(SpringLayout.WEST, txtElevation, 0, SpringLayout.WEST, cbxSpecies);
		propertyListLayout.putConstraint(SpringLayout.NORTH, cbxSpecies, -3, SpringLayout.NORTH, lblSpecies);
		propertyListLayout.putConstraint(SpringLayout.WEST, cbxSpecies, 6, SpringLayout.EAST, lblSpecies);
		propertyListLayout.putConstraint(SpringLayout.EAST, cbxSpecies, 175, SpringLayout.EAST, lblSpecies);
		cbxSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		cbxSpecies.setSelectedIndex(-1);
		pnlPropertyList.add(cbxSpecies);

		btnAddNewSpecies = new JButton("Add");
		propertyListLayout.putConstraint(SpringLayout.NORTH, btnAddNewSpecies, -4, SpringLayout.NORTH, lblSpecies);
		propertyListLayout.putConstraint(SpringLayout.WEST, btnAddNewSpecies, 6, SpringLayout.EAST, cbxSpecies);
		btnAddNewSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnAddNewSpecies.setToolTipText("Add a new species to the species dictionary");
		pnlPropertyList.add(btnAddNewSpecies);

		btnRemoveSpecies = new JButton("Remove");
		propertyListLayout.putConstraint(SpringLayout.EAST, txtElevation, 0, SpringLayout.EAST, btnRemoveSpecies);
		propertyListLayout.putConstraint(SpringLayout.NORTH, btnRemoveSpecies, -4, SpringLayout.NORTH, lblSpecies);
		propertyListLayout.putConstraint(SpringLayout.WEST, btnRemoveSpecies, 6, SpringLayout.EAST, btnAddNewSpecies);
		btnRemoveSpecies.setFont(new Font("Tahoma", Font.PLAIN, 14));
		btnRemoveSpecies.setToolTipText("Remove the selected species from the species dictionary");
		pnlPropertyList.add(btnRemoveSpecies);

		lblAnalysisEventInterval = new JLabel("Event Interval (minutes): ");
		propertyListLayout.putConstraint(SpringLayout.WEST, lblSpecies, 0, SpringLayout.WEST, lblAnalysisEventInterval);
		propertyListLayout.putConstraint(SpringLayout.SOUTH, lblSpecies, -10, SpringLayout.NORTH, lblAnalysisEventInterval);
		propertyListLayout.putConstraint(SpringLayout.WEST, lblAnalysisEventInterval, 10, SpringLayout.WEST, pnlPropertyList);
		propertyListLayout.putConstraint(SpringLayout.SOUTH, lblAnalysisEventInterval, -10, SpringLayout.SOUTH, pnlPropertyList);
		lblAnalysisEventInterval.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlPropertyList.add(lblAnalysisEventInterval);

		txtAnalysisEventInterval = new JTextField();
		propertyListLayout.putConstraint(SpringLayout.NORTH, txtAnalysisEventInterval, -3, SpringLayout.NORTH, lblAnalysisEventInterval);
		propertyListLayout.putConstraint(SpringLayout.WEST, txtAnalysisEventInterval, 6, SpringLayout.EAST, lblAnalysisEventInterval);
		propertyListLayout.putConstraint(SpringLayout.SOUTH, txtAnalysisEventInterval, -6, SpringLayout.SOUTH, pnlPropertyList);
		propertyListLayout.putConstraint(SpringLayout.EAST, txtAnalysisEventInterval, 0, SpringLayout.EAST, btnRemoveSpecies);
		txtAnalysisEventInterval.setText("60");
		txtAnalysisEventInterval.setFont(new Font("Tahoma", Font.PLAIN, 14));

		pnlPropertyList.add(txtAnalysisEventInterval);
		pnlSpeciesPresent = new JPanel();
		contentPaneLayout.putConstraint(SpringLayout.NORTH, pnlSpeciesPresent, 336, SpringLayout.NORTH, getContentPane());
		contentPaneLayout.putConstraint(SpringLayout.EAST, pnlPropertyList, -6, SpringLayout.WEST, pnlSpeciesPresent);
		contentPaneLayout.putConstraint(SpringLayout.WEST, pnlSpeciesPresent, 440, SpringLayout.WEST, getContentPane());
		SpringLayout speciesLayout = new SpringLayout();
		pnlSpeciesPresent.setLayout(speciesLayout);
		pnlSpeciesPresent.setBorder(new LineBorder(Color.BLACK));
		this.getContentPane().add(pnlSpeciesPresent);

		pneSpeciesList = new JScrollPane();
		speciesLayout.putConstraint(SpringLayout.NORTH, pneSpeciesList, 5, SpringLayout.NORTH, pnlSpeciesPresent);
		speciesLayout.putConstraint(SpringLayout.WEST, pneSpeciesList, 5, SpringLayout.WEST, pnlSpeciesPresent);
		speciesLayout.putConstraint(SpringLayout.SOUTH, pneSpeciesList, -70, SpringLayout.SOUTH, pnlSpeciesPresent);
		speciesLayout.putConstraint(SpringLayout.EAST, pneSpeciesList, -5, SpringLayout.EAST, pnlSpeciesPresent);
		lstSpecies = new JList();
		lstSpecies.setModel(new DefaultListModel());
		pneSpeciesList.setViewportView(lstSpecies);
		pnlSpeciesPresent.add(pneSpeciesList);

		lblSpeciesEntries = new JLabel("Species in image:");
		pneSpeciesList.setColumnHeaderView(lblSpeciesEntries);
		speciesLayout.putConstraint(SpringLayout.NORTH, lblSpeciesEntries, 0, SpringLayout.NORTH, pnlSpeciesPresent);
		speciesLayout.putConstraint(SpringLayout.WEST, lblSpeciesEntries, 0, SpringLayout.WEST, pnlSpeciesPresent);
		lblSpeciesEntries.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblSpeciesEntries.setHorizontalAlignment(JLabel.CENTER);

		btnAddSpeciesToList = new JButton("Add Species to Image");
		speciesLayout.putConstraint(SpringLayout.NORTH, btnAddSpeciesToList, 5, SpringLayout.SOUTH, pneSpeciesList);
		speciesLayout.putConstraint(SpringLayout.WEST, btnAddSpeciesToList, 0, SpringLayout.WEST, pneSpeciesList);
		speciesLayout.putConstraint(SpringLayout.EAST, btnAddSpeciesToList, 0, SpringLayout.EAST, pneSpeciesList);
		btnAddSpeciesToList.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnAddSpeciesToList.setToolTipText("Add the selected species to the selected image");
		pnlSpeciesPresent.add(btnAddSpeciesToList);

		btnRemoveSpeciesFromList = new JButton("Remove Species from Image");
		speciesLayout.putConstraint(SpringLayout.NORTH, btnRemoveSpeciesFromList, 5, SpringLayout.SOUTH, btnAddSpeciesToList);
		speciesLayout.putConstraint(SpringLayout.WEST, btnRemoveSpeciesFromList, 0, SpringLayout.WEST, pneSpeciesList);
		speciesLayout.putConstraint(SpringLayout.EAST, btnRemoveSpeciesFromList, 0, SpringLayout.EAST, pneSpeciesList);
		btnRemoveSpeciesFromList.setFont(new Font("Tahoma", Font.PLAIN, 13));
		btnRemoveSpeciesFromList.setToolTipText("Remove the selected species from the selected image");
		pnlSpeciesPresent.add(btnRemoveSpeciesFromList);

		map = new MapPanel();
		contentPaneLayout.putConstraint(SpringLayout.NORTH, map, 0, SpringLayout.SOUTH, barTop);
		contentPaneLayout.putConstraint(SpringLayout.EAST, pnlThumbnailSettings, -5, SpringLayout.WEST, map);
		contentPaneLayout.putConstraint(SpringLayout.EAST, pnlSpeciesPresent, -5, SpringLayout.WEST, map);
		contentPaneLayout.putConstraint(SpringLayout.WEST, map, 700, SpringLayout.WEST, getContentPane());
		contentPaneLayout.putConstraint(SpringLayout.SOUTH, map, -5, SpringLayout.SOUTH, getContentPane());
		contentPaneLayout.putConstraint(SpringLayout.EAST, map, -5, SpringLayout.EAST, getContentPane());
		this.getContentPane().add(map);

		tabOutputTabs = new JTabbedPane();
		contentPaneLayout.putConstraint(SpringLayout.SOUTH, pnlPropertyList, -5, SpringLayout.NORTH, tabOutputTabs);
		contentPaneLayout.putConstraint(SpringLayout.SOUTH, pnlSpeciesPresent, -5, SpringLayout.NORTH, tabOutputTabs);
		contentPaneLayout.putConstraint(SpringLayout.NORTH, tabOutputTabs, 541, SpringLayout.NORTH, getContentPane());
		contentPaneLayout.putConstraint(SpringLayout.SOUTH, tabOutputTabs, -9, SpringLayout.SOUTH, getContentPane());
		contentPaneLayout.putConstraint(SpringLayout.WEST, tabOutputTabs, 9, SpringLayout.WEST, getContentPane());
		contentPaneLayout.putConstraint(SpringLayout.EAST, tabOutputTabs, -5, SpringLayout.WEST, map);
		tabOutputTabs.setBounds(10, 535, 659, 142);
		tabOutputTabs.setSelectedIndex(-1);
		this.getContentPane().add(tabOutputTabs);

		tarAllOutput = new JTextArea();
		tarAllOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));
		SpringLayout allOutputLayout = new SpringLayout();
		tarAllOutput.setLayout(allOutputLayout);

		pnlExcelOutput = new JPanel();
		pnlExcelOutput.setFont(new Font("Tahoma", Font.PLAIN, 10));
		SpringLayout excelLayout = new SpringLayout();
		pnlExcelOutput.setLayout(excelLayout);

		pneExcelOutput = new JScrollPane();
		pneExcelOutput.setFont(new Font("Tahoma", Font.PLAIN, 10));
		pneExcelOutput.setViewportView(pnlExcelOutput);

		lblYearOnly = new JLabel("Year(s) only [0-9999]: ");
		excelLayout.putConstraint(SpringLayout.NORTH, lblYearOnly, 10, SpringLayout.NORTH, pnlExcelOutput);
		excelLayout.putConstraint(SpringLayout.WEST, lblYearOnly, 10, SpringLayout.WEST, pnlExcelOutput);
		lblYearOnly.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(lblYearOnly);

		txtYearOnly = new JTextField();
		excelLayout.putConstraint(SpringLayout.NORTH, txtYearOnly, -3, SpringLayout.NORTH, lblYearOnly);
		excelLayout.putConstraint(SpringLayout.WEST, txtYearOnly, 6, SpringLayout.EAST, lblYearOnly);
		excelLayout.putConstraint(SpringLayout.EAST, txtYearOnly, 192, SpringLayout.EAST, lblYearOnly);
		txtYearOnly.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(txtYearOnly);

		lblMonthOnly = new JLabel("Month(s) only [1-12]: ");
		excelLayout.putConstraint(SpringLayout.NORTH, lblMonthOnly, 10, SpringLayout.SOUTH, lblYearOnly);
		excelLayout.putConstraint(SpringLayout.WEST, lblMonthOnly, 0, SpringLayout.WEST, lblYearOnly);
		lblMonthOnly.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(lblMonthOnly);

		txtMonthOnly = new JTextField();
		excelLayout.putConstraint(SpringLayout.NORTH, txtMonthOnly, 0, SpringLayout.NORTH, lblMonthOnly);
		excelLayout.putConstraint(SpringLayout.WEST, txtMonthOnly, 0, SpringLayout.EAST, lblMonthOnly);
		excelLayout.putConstraint(SpringLayout.EAST, txtMonthOnly, 0, SpringLayout.EAST, txtYearOnly);
		txtMonthOnly.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(txtMonthOnly);

		lblTimeFrameStart = new JLabel("Timeframe, Start Hour [1-24]:");
		excelLayout.putConstraint(SpringLayout.NORTH, lblTimeFrameStart, 10, SpringLayout.SOUTH, lblMonthOnly);
		excelLayout.putConstraint(SpringLayout.WEST, lblTimeFrameStart, 0, SpringLayout.WEST, lblYearOnly);
		lblTimeFrameStart.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(lblTimeFrameStart);

		txtTimeFrameStart = new JTextField();
		excelLayout.putConstraint(SpringLayout.NORTH, txtTimeFrameStart, 0, SpringLayout.NORTH, lblTimeFrameStart);
		excelLayout.putConstraint(SpringLayout.WEST, txtTimeFrameStart, 6, SpringLayout.EAST, lblTimeFrameStart);
		excelLayout.putConstraint(SpringLayout.EAST, txtTimeFrameStart, 0, SpringLayout.EAST, txtYearOnly);
		txtTimeFrameStart.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(txtTimeFrameStart);

		lblTimeFrameEnd = new JLabel("Timeframe, End Hour [1-24]:");
		excelLayout.putConstraint(SpringLayout.NORTH, lblTimeFrameEnd, 10, SpringLayout.SOUTH, lblTimeFrameStart);
		excelLayout.putConstraint(SpringLayout.WEST, lblTimeFrameEnd, 0, SpringLayout.WEST, lblYearOnly);
		lblTimeFrameEnd.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(lblTimeFrameEnd);

		txtTimeFramEnd = new JTextField();
		excelLayout.putConstraint(SpringLayout.NORTH, txtTimeFramEnd, 0, SpringLayout.NORTH, lblTimeFrameEnd);
		excelLayout.putConstraint(SpringLayout.WEST, txtTimeFramEnd, 6, SpringLayout.EAST, lblTimeFrameEnd);
		excelLayout.putConstraint(SpringLayout.EAST, txtTimeFramEnd, 0, SpringLayout.EAST, txtYearOnly);
		txtTimeFramEnd.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(txtTimeFramEnd);

		cbxSpeciesOnly = new JCheckBox("Selected species (in species list above) only?");
		excelLayout.putConstraint(SpringLayout.NORTH, cbxSpeciesOnly, 10, SpringLayout.SOUTH, lblTimeFrameEnd);
		excelLayout.putConstraint(SpringLayout.WEST, cbxSpeciesOnly, 0, SpringLayout.WEST, lblYearOnly);
		cbxSpeciesOnly.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(cbxSpeciesOnly);

		cbxLocationOnly = new JCheckBox("Selected location (in location list above) only?");
		excelLayout.putConstraint(SpringLayout.NORTH, cbxLocationOnly, 5, SpringLayout.SOUTH, cbxSpeciesOnly);
		excelLayout.putConstraint(SpringLayout.WEST, cbxLocationOnly, 0, SpringLayout.WEST, lblYearOnly);
		cbxLocationOnly.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(cbxLocationOnly);

		/*
		radNumPictures = new JRadioButton("Picture Count");
		radNumPictures.setSelected(true);
		radNumPictures.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(radNumPictures);
		
		radActivity = new JRadioButton("Picture Activity");
		radActivity.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(radActivity);
		
		radAbundance = new JRadioButton("Picture Abundance");
		radAbundance.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(radAbundance);
		
		radPeriod = new JRadioButton("Picture Period");
		radPeriod.setFont(new Font("Tahoma", Font.PLAIN, 14));
		pnlExcelOutput.add(radPeriod);
		
		grpDataType = new ButtonGroup();
		grpDataType.add(radNumPictures);
		grpDataType.add(radActivity);
		grpDataType.add(radAbundance);
		grpDataType.add(radPeriod);*/

		pneAllOutput = new JScrollPane();
		pneAllOutput.setViewportView(tarAllOutput);
		tabOutputTabs.insertTab("Excel Output Settings", new ImageIcon(""), pneExcelOutput, "Excel output testing", 0);
		tabOutputTabs.insertTab("Text Output", new ImageIcon(""), pneAllOutput, "All Output from the analysis", 0);
		tabOutputTabs.setSelectedIndex(0);

	}
}
