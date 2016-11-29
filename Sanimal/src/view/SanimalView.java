package view;

import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.ArrayUtils;

import controller.Constants;
import model.analysis.ImageQuery;
import model.analysis.SanimalAnalysisUtils;
import model.image.ImageDirectory;
import model.image.ImageEntry;
import model.image.ImageImporterData;
import model.image.ImageUpdate;
import model.location.Location;
import model.location.LocationData;
import model.location.LocationUpdate;
import model.location.UTMCoord;
import model.species.Species;
import model.species.SpeciesData;
import model.species.SpeciesEntry;
import model.species.SpeciesUpdate;
import model.timeline.TimelineData;
import model.timeline.TimelineUpdate;
import view.map.SanimalMapMarkerOverlay;

public class SanimalView extends SanimalViewBase implements Observer
{
	protected ImageView imageView = new ImageView();

	public SanimalView()
	{
		super();
		this.setResizable(true);
		this.setTitle("Scientific Animal Image AnaLysis (SANIMAL)");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.setSize(1400, 773);
		this.setMinimumSize(new Dimension(1400, 773));
		this.pack();
		this.setLocationRelativeTo(null);

		btnShowPreview.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				if (!imageView.isVisible())
					imageView.setVisible(true);
			}
		});

		btnResetPreview.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				imageView.resetImage();
				sldBrightness.setValue(1);
				sldContrast.setValue(25);
			}
		});

		sldBrightness.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent event)
			{
				imageView.setImageBrightness((double) sldBrightness.getValue() * 10);
			}
		});

		sldContrast.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent event)
			{
				imageView.setImageContrast((double) sldContrast.getValue() / 25D);
			}
		});

		ActionListener utmLatLngFeetMetersClicked = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				SanimalView.this.refreshLocationFields();
			}
		};
		btnUnitLatLng.addActionListener(utmLatLngFeetMetersClicked);
		btnUnitUTM.addActionListener(utmLatLngFeetMetersClicked);
		btnUnitFeet.addActionListener(utmLatLngFeetMetersClicked);
		btnUnitMeter.addActionListener(utmLatLngFeetMetersClicked);
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
				this.imageView.setDisplayImage(image);
			});
		}
		else
		{
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

	public List<ImageEntry> getFilteredImageEntries()
	{
		List<ImageEntry> selected = this.getSelectedImageEntries();
		ImageQuery filter = new ImageQuery();
		if (!this.txtYearOnly.getText().isEmpty())
		{
			List<Integer> years = SanimalAnalysisUtils.csvStringToInts(this.txtYearOnly.getText());
			filter.yearOnly(ArrayUtils.toPrimitive(years.<Integer> toArray(new Integer[years.size()])));
		}
		if (!this.txtMonthOnly.getText().isEmpty())
		{
			List<Integer> months = SanimalAnalysisUtils.csvStringToInts(this.txtMonthOnly.getText());
			ListIterator<Integer> listIterator = months.listIterator();
			while (listIterator.hasNext())
			{
				Integer month = listIterator.next() - 1;
				if (month < 0 || month > 11)
					listIterator.remove();
				else
					listIterator.set(month);
			}
			if (!months.isEmpty())
				filter.monthOnly(ArrayUtils.toPrimitive(months.<Integer> toArray(new Integer[months.size()])));
		}
		if (!this.txtTimeFrameStart.getText().isEmpty() && !this.txtTimeFramEnd.getText().isEmpty())
		{
			try
			{
				Integer startTime = Integer.parseInt(this.txtTimeFrameStart.getText());
				Integer endTime = Integer.parseInt(this.txtTimeFramEnd.getText());
				if (startTime > endTime)
				{
					Integer temp = startTime;
					startTime = endTime;
					endTime = temp;
				}
				filter.timeFrame(startTime, endTime);
			}
			catch (NumberFormatException exception)
			{
			}
		}
		if (this.cbxSpeciesOnly.isSelected() && this.getSelectedSpecies() != null)
		{
			filter.speciesOnly(this.getSelectedSpecies());
		}
		if (this.cbxLocationOnly.isSelected() && this.getSelectedLocation() != null)
		{
			filter.locationOnly(this.getSelectedLocation());
		}
		return filter.query(selected);
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
			Double heightInFeet = currentlySelected.getElevation();
			Double latitude = currentlySelected.getLat();
			Double longitude = currentlySelected.getLng();
			if (btnUnitFeet.isSelected())
			{
				txtElevation.setText(String.format("%.1f", heightInFeet));
			}
			else if (btnUnitMeter.isSelected())
			{
				txtElevation.setText(String.format("%.1f", heightInFeet * Constants.FEET2METERS));
			}
			else
			{
				txtElevation.setText("");
			}

			if (btnUnitLatLng.isSelected())
			{
				tarLocationCoord.setText(String.format("Latitude: %.6f\nLongitude: %.6f", latitude, longitude));
			}
			else if (btnUnitUTM.isSelected())
			{
				UTMCoord utm = SanimalAnalysisUtils.Deg2UTM(latitude, longitude);
				tarLocationCoord.setText(String.format("%s%s %.0f %.0f", utm.getZone().toString(), utm.getLetter().toString(), utm.getEasting(), utm.getNorthing()));
			}
			else
			{
				tarLocationCoord.setText("");
			}
		}
		else
		{
			tarLocationCoord.setText("");
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
