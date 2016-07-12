/*
 * Author: David Slovikosky
 * Mod: Afraid of the Dark
 * Ideas and Textures: Michael Albertson
 */
package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import model.ImageEntry;
import model.Location;
import model.SanimalData;
import model.Species;
import model.SpeciesEntry;
import view.ImageImportView;
import view.SanimalView;

public class SanimalController
{
	private final SanimalView sanimalView;
	private final SanimalData sanimalData;

	public SanimalController(SanimalView sanimalView, SanimalData sanimalData)
	{
		this.sanimalView = sanimalView;
		this.sanimalData = sanimalData;

		this.sanimalView.addALToLoadImages(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ImageImportView view = new ImageImportView();
				// When the user clicks a new image in the image list on the right
				view.addImageTreeValueChanged(new TreeSelectionListener()
				{
					@Override
					public void valueChanged(TreeSelectionEvent event)
					{
						SanimalController.this.selectedItemUpdated(view);
					}
				});
				// When the user wants to load in images
				view.addImageBrowseListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						JFileChooser chooser = new JFileChooser();
						chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
						int returnVal = chooser.showOpenDialog(null);
						if (returnVal == JFileChooser.APPROVE_OPTION)
						{
							sanimalData.getImageData().readAndAddImages(chooser.getSelectedFile(), view.searchSubdirectories());
							view.setImageList(sanimalData.getImageData().getHeadDirectory());
						}
					}
				});
				// When the user selects a new location from the drop down
				view.addLocationSelectedListener(new ItemListener()
				{
					@Override
					public void itemStateChanged(ItemEvent event)
					{
						if (event.getStateChange() == ItemEvent.SELECTED)
						{
							Location selected = ((Location) event.getItem());
							if (selected != null)
								for (ImageEntry selectedImage : view.getSelectedImageEntries())
									selectedImage.setLocationTaken(selected);
							view.setLocation(selected);
						}
					}
				});
				// When the user wants to add a new location
				view.addALToAddNewLocation(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{
						Location newLocation = view.askUserForNewLocation();
						if (newLocation != null)
						{
							sanimalData.getLocationData().addLocation(newLocation);
							view.setLocationList(sanimalData.getLocationData().getRegisteredLocations());
						}
					}
				});
				// When the user wants to add a new species
				view.addALToAddNewSpecies(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{
						Species species = view.askUserForNewSpecies();
						if (species != null)
						{
							sanimalData.getSpeciesData().addSpecies(species);
							view.setSpeciesList(sanimalData.getSpeciesData().getRegisteredSpecies());
						}
					}
				});
				// When the user wants to remove a location
				view.addALToRemoveLocation(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{
						if (view.getSelectedLocation() != null)
						{
							Location selected = view.getSelectedLocation();
							int numberOfSelectedOccourances = 0;
							// Count how many images have this location selected
							for (ImageEntry image : view.getAllTreeImageEntries())
								if (image.getLocationTaken() == selected)
									numberOfSelectedOccourances++;
							// If we have more than 1, ask the user if he'd like to continue
							if (numberOfSelectedOccourances >= 1)
								if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(view, "This location has already been set on multiple images, continue removing the location from all images?"))
									return;
							// Delete the location
							for (ImageEntry image : view.getAllTreeImageEntries())
								if (image.getLocationTaken() == selected)
									image.setLocationTaken(null);
							sanimalData.getLocationData().removeLocation(selected.toString());
							view.setLocationList(sanimalData.getLocationData().getRegisteredLocations());
							SanimalController.this.selectedItemUpdated(view);
						}
					}
				});
				// When the user wants to remove a species
				view.addALToRemoveSpecies(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{
						if (view.getSelectedSpecies() != null)
						{
							Species selected = view.getSelectedSpecies();
							int numberOfSelectedOccourances = 0;
							// Count how many images have this species selected
							for (ImageEntry image : view.getAllTreeImageEntries())
								for (SpeciesEntry entry : image.getSpeciesPresent())
									if (entry.getSpecies() == selected)
										numberOfSelectedOccourances++;
							// If we have more than 1, ask the user if he'd like to continue
							if (numberOfSelectedOccourances >= 1)
								if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(view, "This species has already been set on multiple images, continue removing the species from all images?"))
									return;
							for (ImageEntry image : view.getAllTreeImageEntries())
							{
								Iterator<SpeciesEntry> iterator = image.getSpeciesPresent().iterator();
								while (iterator.hasNext())
									if (iterator.next().getSpecies() == selected)
										iterator.remove();
							}
							sanimalData.getSpeciesData().removeSpecies(selected);
							view.setSpeciesList(sanimalData.getSpeciesData().getRegisteredSpecies());
							SanimalController.this.selectedItemUpdated(view);
						}
					}
				});
				// When the user adds a new species to the image
				view.addALToAddSpeciesToList(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{
						Integer numberOfAnimals = Integer.MAX_VALUE;
						while (numberOfAnimals == Integer.MAX_VALUE)
						{
							try
							{
								String numberOfAnimalsString = JOptionPane.showInputDialog("How many animals of this species are in the image?");
								if (numberOfAnimalsString == null)
									return;
								numberOfAnimals = Integer.parseInt(numberOfAnimalsString);
							}
							catch (NumberFormatException exception)
							{
							}
						}
						Species selectedSpecies = view.getSelectedSpecies();
						List<ImageEntry> selectedImages = view.getSelectedImageEntries();
						if (selectedSpecies != null)
						{
							for (ImageEntry imageEntry : selectedImages)
								imageEntry.addSpecies(selectedSpecies, numberOfAnimals);
							if (selectedImages.size() == 1)
								view.setSpeciesEntryList(selectedImages.get(0).getSpeciesPresent());
						}
						SanimalController.this.selectedItemUpdated(view);
					}
				});
				// When the user removes an animal from an image
				view.addALToRemoveSpeciesFromList(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{
						Species selectedSpecies = view.getSelectedSpecies();
						List<ImageEntry> selectedImages = view.getSelectedImageEntries();
						if (selectedSpecies != null)
						{
							for (ImageEntry imageEntry : selectedImages)
								imageEntry.removeSpecies(selectedSpecies);
						}
						SanimalController.this.selectedItemUpdated(view);
					}
				});
				// When the user is done with importing images
				view.addALToFinishImporting(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{

					}
				});
				view.setVisible(true);
			}
		});
	}

	public void selectedItemUpdated(ImageImportView view)
	{
		ImageEntry first = null;
		Location firstLocation = null;
		String firstDate = null;
		List<SpeciesEntry> firstSpeciesEntries = null;
		List<ImageEntry> selectedImages = view.getSelectedImageEntries();
		for (ImageEntry current : selectedImages)
		{
			if (first == null)
			{
				first = current;
				firstLocation = first.getLocationTaken();
				firstDate = first.getDateTakenFormatted();
				firstSpeciesEntries = first.getSpeciesPresent();
				Collections.sort(firstSpeciesEntries);
				continue;
			}
			if (!current.getDateTakenFormatted().equals(firstDate))
				firstDate = null;
			if (current.getLocationTaken() != firstLocation)
				firstLocation = null;
			if (firstSpeciesEntries != null)
			{
				Collections.sort(current.getSpeciesPresent());
				if (!current.getSpeciesPresent().equals(firstSpeciesEntries))
					firstSpeciesEntries = null;
			}
		}
		view.setLocation(firstLocation);
		view.setDate(firstDate);
		view.setSpeciesEntryList(firstSpeciesEntries);
		view.refreshLocationFields();
		if (selectedImages.size() == 1)
			view.setThumbnailImage(first);
		else
			view.setThumbnailImage(null);
	}
}
