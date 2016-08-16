package controller;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.time.StopWatch;

import model.SanimalData;
import model.image.ImageEntry;
import model.location.Location;
import model.species.Species;
import model.species.SpeciesEntry;
import view.SanimalInput;
import view.SanimalView;

/**
 * The main controller of sanimal in the MVC design pattern. It links the view to the model
 * 
 * @author David Slovikosky
 */
public class SanimalController
{
	// Declare the view and the model
	private final SanimalView sanimalView;
	private final SanimalData sanimalData;
	// Declare constants
	private static final long MILLIS_BETWEEN_CHECKS = 500;

	// DEBUG
	// Loc 1: 32.273302, -110.836417
	// Loc 2: 32.273057, -110.836507
	// Loc 3: 32.273390, -110.836450

	/**
	 * Constructor of the controller
	 * 
	 * @param sanimalView
	 *            The view to link the model to
	 * @param sanimalData
	 *            The model to link the view to
	 */
	public SanimalController(SanimalView sanimalView, SanimalData sanimalData)
	{
		this.sanimalView = sanimalView;
		this.sanimalData = sanimalData;

		///
		/// Begin to setup observable/observer listeners
		///

		sanimalData.getLocationData().addObserver(sanimalView);
		sanimalData.getSpeciesData().addObserver(sanimalView);
		sanimalData.getImageData().addObserver(sanimalView);
		sanimalData.getTimelineData().addObserver(sanimalView);

		///
		/// End setup observable/observer listeners
		///

		///
		/// Begin to setup sanimalView with action listeners 
		///

		// When the user clicks a new image in the image list on the right
		sanimalView.addImageTreeValueChanged(event ->
		{
			// Update the selected item and update the timeline data's image list
			SanimalController.this.selectedItemUpdated();
			sanimalData.getTimelineData().updateSourceImageList(sanimalView.getSelectedImageEntries());
		});
		// When the user wants to load in images
		sanimalView.addImageBrowseListener(event ->
		{
			// Create a JFileChooser and load in images from the directory
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
				sanimalData.getImageData().readAndAddImages(chooser.getSelectedFile(), sanimalView.searchSubdirectories());
		});
		// When the user selects a new location from the drop down
		sanimalView.addLocationSelectedListener(event ->
		{
			if (event.getStateChange() == ItemEvent.SELECTED)
			{
				Location selected = ((Location) event.getItem());
				if (selected != null)
					for (ImageEntry selectedImage : sanimalView.getSelectedImageEntries())
						selectedImage.setLocationTaken(selected);
				sanimalView.setLocation(selected);
				sanimalView.getMapPanel().getMapViewer().setCenterPosition(selected.toGeoPosition());
			}
		});
		// When the user wants to add a new location
		sanimalView.addALToAddNewLocation(event ->
		{
			Location newLocation = SanimalInput.askUserForNewLocation();
			if (newLocation != null)
				sanimalData.getLocationData().addLocation(newLocation);
			sanimalView.setLocation(newLocation);
		});
		// When the user wants to add a new species
		sanimalView.addALToAddNewSpecies(event ->
		{
			Species species = SanimalInput.askUserForNewSpecies();
			if (species != null)
				sanimalData.getSpeciesData().addSpecies(species);
			sanimalView.setSpecies(species);
		});
		// When the user wants to remove a location
		sanimalView.addALToRemoveLocation(event ->
		{
			if (sanimalView.getSelectedLocation() != null)
			{
				Location selected = sanimalView.getSelectedLocation();
				int numberOfSelectedOccourances = 0;
				// Count how many images have this location selected
				for (ImageEntry image : sanimalView.getAllTreeImageEntries())
					if (image.getLocationTaken() == selected)
						numberOfSelectedOccourances++;
				// If we have more than 1, ask the user if he'd like to continue
				if (numberOfSelectedOccourances >= 1)
					if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(sanimalView, "This location has already been set on multiple images, continue removing the location from all images?"))
						return;
				// Delete the location
				for (ImageEntry image : sanimalView.getAllTreeImageEntries())
					if (image.getLocationTaken() == selected)
						image.setLocationTaken(null);
				sanimalData.getLocationData().removeLocation(selected.toString());
				SanimalController.this.selectedItemUpdated();
			}
		});
		// When the user wants to remove a species
		sanimalView.addALToRemoveSpecies(event ->
		{
			if (sanimalView.getSelectedSpecies() != null)
			{
				Species selected = sanimalView.getSelectedSpecies();
				int numberOfSelectedOccourances = 0;
				// Count how many images have this species selected
				for (ImageEntry image : sanimalView.getAllTreeImageEntries())
					for (SpeciesEntry entry : image.getSpeciesPresent())
						if (entry.getSpecies() == selected)
							numberOfSelectedOccourances++;
				// If we have more than 1, ask the user if he'd like to continue
				if (numberOfSelectedOccourances >= 1)
					if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(sanimalView, "This species has already been set on multiple images, continue removing the species from all images?"))
						return;
				for (ImageEntry image : sanimalView.getAllTreeImageEntries())
				{
					Iterator<SpeciesEntry> iterator = image.getSpeciesPresent().iterator();
					while (iterator.hasNext())
						if (iterator.next().getSpecies() == selected)
							iterator.remove();
				}
				sanimalData.getSpeciesData().removeSpecies(selected);
				SanimalController.this.selectedItemUpdated();
			}
		});
		// When the user adds a new species to the image
		sanimalView.addALToAddSpeciesToList(event ->
		{
			Species selectedSpecies = sanimalView.getSelectedSpecies();
			if (selectedSpecies != null)
			{
				List<ImageEntry> selectedImages = sanimalView.getSelectedImageEntries();
				// Ensure we have a species selected and an image selected
				if (!selectedImages.isEmpty())
				{
					// Get the number of animals, and add it to the image
					Integer numberOfAnimals = SanimalInput.askUserForNumberAnimals();
					if (numberOfAnimals != Integer.MAX_VALUE)
					{
						for (ImageEntry imageEntry : selectedImages)
							imageEntry.addSpecies(selectedSpecies, numberOfAnimals);
						//						if (selectedImages.size() == 1)
						//							sanimalView.setSpeciesEntryList(selectedImages.get(0).getSpeciesPresent());
						SanimalController.this.selectedItemUpdated();
					}
				}
				else
				{
					JOptionPane.showConfirmDialog(sanimalView, "You must select an image(s) to apply this species to!", "Error adding species to Image", JOptionPane.DEFAULT_OPTION);
				}
			}
			else
			{
				JOptionPane.showConfirmDialog(sanimalView, "You must select a species from the drop down first!", "Error adding species to Image", JOptionPane.DEFAULT_OPTION);
			}
		});
		// When the user removes an animal from an image
		sanimalView.addALToRemoveSpeciesFromList(event ->
		{
			Species selectedSpecies = sanimalView.getSelectedSpeciesFromList();
			List<ImageEntry> selectedImages = sanimalView.getSelectedImageEntries();
			if (selectedSpecies != null)
				for (ImageEntry imageEntry : selectedImages)
					imageEntry.removeSpecies(selectedSpecies);
			SanimalController.this.selectedItemUpdated();
		});
		// When the user clicks "Perform Analysis"
		sanimalView.addALToPerformAnalysis(event ->
		{
			Integer eventInterval = sanimalView.getAnalysisEventInterval();
			if (eventInterval != -1)
				sanimalView.setOutputText(sanimalData.getOutputFormatter().format(sanimalView.getSelectedImageEntries(), eventInterval));
		});
		// When the user clicks to create an excel file
		sanimalView.addALToCreateExcel(event ->
		{
			// Create a JFileChooser, and then create the excel file
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle("Select the location to save the excel file to");
			chooser.setFileFilter(new FileNameExtensionFilter("Excel files (.xlsx)", "xlsx"));
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			chooser.setSelectedFile(new File("Sanimal.xlsx"));
			int response = chooser.showSaveDialog(sanimalView);
			if (response == JFileChooser.APPROVE_OPTION)
			{
				File directory = chooser.getSelectedFile();
				if (sanimalData.getExcelFormatter().format(directory, sanimalView.getSelectedImageEntries(), sanimalView.getSelectedDataTypeRadioButton(), sanimalView.getAnalysisEventInterval()))
					JOptionPane.showMessageDialog(sanimalView, "Excel file saved sucessfully!");
				else
					JOptionPane.showMessageDialog(sanimalView, "There was an error when saving the excel file.");
			}
		});
		// When the user selects the timeline
		sanimalView.addALToPrgDataShow(new MouseListener()
		{
			@Override
			public void mouseReleased(MouseEvent event)
			{
				double percentage = (double) event.getX() / (double) event.getComponent().getWidth();
				if (percentage < 0)
					percentage = 0;
				if (percentage > 1)
					percentage = 1;
				sanimalData.getTimelineData().centerOnDayByPercent(percentage);
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
		// When the user drags the timeline
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		sanimalView.addALToPrgDataShow(new MouseMotionListener()
		{
			@Override
			public void mouseMoved(MouseEvent event)
			{
			}

			@Override
			public void mouseDragged(MouseEvent event)
			{
				// Only perform the update once every .5 seconds. This ensures that this update does not get
				// spammed and lag the program
				if (stopWatch.getTime() > MILLIS_BETWEEN_CHECKS)
				{
					double percentage = (double) event.getX() / (double) event.getComponent().getWidth();
					if (percentage < 0)
						percentage = 0;
					if (percentage > 1)
						percentage = 1;
					sanimalData.getTimelineData().centerOnDayByPercent(percentage);
					stopWatch.reset();
					stopWatch.start();
				}
			}
		});
		// When the user creates "All Pictures" output
		sanimalView.addALToAllPictures(event ->
		{
			Integer eventInterval = sanimalView.getAnalysisEventInterval();
			if (eventInterval != -1)
				sanimalView.setOutputText(sanimalData.getOutputFormatter().createAllPictures(sanimalView.getSelectedImageEntries(), eventInterval));
		});
		// When the user tries to save the current project to a file
		sanimalView.addALToSave(event ->
		{
			// Create a JFileChooser, and then create the sanimal file
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle("Select the location to save the project to");
			chooser.setFileFilter(new FileNameExtensionFilter("Sanimal project file (.sanimal)", "sanimal"));
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			chooser.setSelectedFile(new File("Untitled.sanimal"));
			int response = chooser.showSaveDialog(sanimalView);
			if (response == JFileChooser.APPROVE_OPTION)
			{
				File directory = chooser.getSelectedFile();
				try
				{
					byte[] sanimalDataBytes = SerializationUtils.serialize(sanimalData);
					if (!directory.exists() || directory.exists() && JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(sanimalView, "The file already exists, continue and overwrite the file?", "File already exists", JOptionPane.YES_NO_OPTION))
						FileUtils.writeByteArrayToFile(directory, sanimalDataBytes);
				}
				catch (SerializationException | IOException exception)
				{
					System.err.println("Error in sanimal data serialization.");
					exception.printStackTrace();
				}
			}
		});
		// When the user tries to load a project into the program
		sanimalView.addALToLoad(event ->
		{
			// Create a JFileChooser, and then load the sanimal file
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle("Select project file to load");
			chooser.setFileFilter(new FileNameExtensionFilter("Sanimal project file (.sanimal)", "sanimal"));
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			int response = chooser.showOpenDialog(sanimalView);
			if (response == JFileChooser.APPROVE_OPTION)
			{
				File selected = chooser.getSelectedFile();
				if (selected.exists())
				{
					try
					{
						byte[] sanimalDataBytes = FileUtils.readFileToByteArray(selected);
						SanimalData newData = SerializationUtils.<SanimalData> deserialize(sanimalDataBytes);
						this.loadProject(newData);
					}
					catch (IOException | SerializationException exception)
					{
						System.err.println("Error in sanimal data de-serialization.");
						exception.printStackTrace();
						JOptionPane.showMessageDialog(sanimalView, "Error reading file " + selected + "\nWas this project created in an older version of Sanimal?");
					}
				}
				else
					JOptionPane.showMessageDialog(sanimalView, "Selected file does not exist!");
			}
		});
		// When the user drags and drops a file onto the screen
		sanimalView.addDropTarget(new DropTargetListener()
		{
			@Override
			public void dropActionChanged(DropTargetDragEvent event)
			{
			}

			@Override
			public void drop(DropTargetDropEvent event)
			{
				// Accept copy drops
				event.acceptDrop(DnDConstants.ACTION_COPY);

				// Get the transfer which can provide the dropped item data
				Transferable transferable = event.getTransferable();

				// Get the data formats of the dropped item
				DataFlavor[] flavors = transferable.getTransferDataFlavors();

				// Loop through the flavors
				for (DataFlavor flavor : flavors)
				{
					// If the drop items are files
					if (flavor.isFlavorJavaFileListType())
					{
						// Get all of the dropped files
						try
						{
							List<File> files = (List<File>) transferable.getTransferData(flavor);

							if (files.size() == 1)
							{
								File selected = files.get(0);
								String extension = FilenameUtils.getExtension(selected.getName());
								if (extension != null && extension.equalsIgnoreCase("sanimal"))
								{
									try
									{
										byte[] sanimalDataBytes = FileUtils.readFileToByteArray(selected);
										SanimalData newData = SerializationUtils.<SanimalData> deserialize(sanimalDataBytes);
										SanimalController.this.loadProject(newData);
									}
									catch (IOException | SerializationException exception)
									{
										System.err.println("Error in sanimal data de-serialization from drag & drop.");
										exception.printStackTrace();
										JOptionPane.showMessageDialog(sanimalView, "Error reading file " + selected + "\nWas this project created in an older version of Sanimal?");
									}
								}
							}
							else
								JOptionPane.showMessageDialog(sanimalView, "Only drag & drop a single file to load onto the program.");
						}
						catch (UnsupportedFlavorException | IOException exception)
						{
							System.err.println("Error reading transferred data.");
							exception.printStackTrace();
						}
					}
				}

				// Inform that the drop is complete
				event.dropComplete(true);
			}

			@Override
			public void dragOver(DropTargetDragEvent event)
			{
			}

			@Override
			public void dragExit(DropTargetEvent event)
			{
			}

			@Override
			public void dragEnter(DropTargetDragEvent event)
			{
			}

		});
		// If the user clicks the single day advance button
		sanimalView.getMapPanel().addALToNext(event ->
		{
			sanimalData.getTimelineData().advanceBySingleDay();
		});
		// If the user clicks the single day reverse button
		sanimalView.getMapPanel().addALToPrevious(event ->
		{
			sanimalData.getTimelineData().rewindBySingleDay();
		});
		// If the user clicks the bottom button
		sanimalView.getMapPanel().addALToBottom(event ->
		{
			sanimalData.getTimelineData().goToLast();
		});
		// If the user clicks the top button
		sanimalView.getMapPanel().addALToTop(event ->
		{
			sanimalData.getTimelineData().goToFirst();
		});
		// If the user clicks the stop button
		sanimalView.getMapPanel().addALToStop(event ->
		{
			sanimalData.getTimelineData().stopPlay();
		});
		// If the user clicks the play button
		sanimalView.getMapPanel().addALToForward(event ->
		{
			sanimalData.getTimelineData().beginForwardPlay();
		});
		// If the user clicks the reverse button
		sanimalView.getMapPanel().addALToBackwards(event ->
		{
			sanimalData.getTimelineData().beginReversePlay();
		});
		// When the user selects a new playback speed
		sanimalView.getMapPanel().addCLToSpeedSlider(event ->
		{
			sanimalData.getTimelineData().setClockSpeedMultiplier(sanimalView.getMapPanel().getCurrentSliderSpeed());
		});

		// Set the view to visible now that it has been constructed
		sanimalView.setVisible(true);

		///
		/// End setup sanimalView with action listeners 
		///
	}

	/**
	 * When the selected item is updated, we need to go through each entry and find common features between them to be displayed.
	 */
	private void selectedItemUpdated()
	{
		// The first entry to compare to
		ImageEntry first = null;
		// The first image's location
		Location firstLocation = null;
		// The first image's date
		String firstDate = null;
		// The first image's species
		List<SpeciesEntry> firstSpeciesEntries = null;
		List<ImageEntry> selectedImages = sanimalView.getSelectedImageEntries();
		// Find similarities
		for (ImageEntry current : selectedImages)
		{
			// Setup the first entry
			if (first == null)
			{
				first = current;
				firstLocation = first.getLocationTaken();
				firstDate = first.getDateTakenFormatted();
				firstSpeciesEntries = first.getSpeciesPresent();
				Collections.sort(firstSpeciesEntries);
				continue;
			}
			// If the first date is not null yet, we check to see if the current date equals the first date. If so,
			// Continue, else we set the first date to null
			if (firstDate != null)
				if (!current.getDateTakenFormatted().equals(firstDate))
					firstDate = null;
			// Perform the same operation on location as above
			if (firstLocation != null)
				if (current.getLocationTaken() != firstLocation)
					firstLocation = null;
			// Perform the same operation on species as above
			if (firstSpeciesEntries != null)
			{
				Collections.sort(current.getSpeciesPresent());
				if (!current.getSpeciesPresent().equals(firstSpeciesEntries))
					firstSpeciesEntries = null;
			}
		}
		// Update the fields accordingly
		sanimalView.setLocation(firstLocation);
		sanimalView.setDate(firstDate);
		sanimalView.setSpeciesEntryList(firstSpeciesEntries);
		sanimalView.refreshLocationFields();
		if (selectedImages.size() == 1)
			sanimalView.setThumbnailImage(first);
		else
			sanimalView.setThumbnailImage(null);
	}

	/**
	 * After de-serialization, load the project with this method
	 * 
	 * @param newData
	 *            The new data to replace the old data
	 */
	private void loadProject(SanimalData newData)
	{
		// Load location data
		sanimalData.getLocationData().clearRegisteredLocations();
		if (newData.getLocationData() != null)
			for (Location location : newData.getLocationData().getRegisteredLocations())
				sanimalData.getLocationData().addLocation(location);

		// Load species data
		sanimalData.getSpeciesData().clearRegisteredSpecies();
		if (newData.getSpeciesData() != null)
			for (Species species : newData.getSpeciesData().getRegisteredSpecies())
				sanimalData.getSpeciesData().addSpecies(species);

		// Load image data
		if (newData.getImageData() != null)
			sanimalData.getImageData().loadImagesFromExistingDirectory(newData.getImageData().getHeadDirectory());
		else
			sanimalData.getImageData().loadImagesFromExistingDirectory(null);
	}
}
