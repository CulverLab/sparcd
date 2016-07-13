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
import view.SanimalView;

public class SanimalController
{
	private final SanimalView sanimalView;
	private final SanimalData sanimalData;

	// DEBUG
	// Loc 1: 32.273302, -110.836417
	// Loc 2: 32.273057, -110.836507
	// Loc 3: 32.273390, -110.836450

	// Lat +/- 85
	// Long +/- 180
	// Feet elevation

	public SanimalController(SanimalView sanimalView, SanimalData sanimalData)
	{
		this.sanimalView = sanimalView;
		this.sanimalData = sanimalData;

		///
		/// Begin to setup sanimalView with action listeners 
		///

		// When the user clicks a new image in the image list on the right
		sanimalView.addImageTreeValueChanged(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent event)
			{
				SanimalController.this.selectedItemUpdated();
			}
		});
		// When the user wants to load in images
		sanimalView.addImageBrowseListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					sanimalData.getImageData().readAndAddImages(chooser.getSelectedFile(), sanimalView.searchSubdirectories());
					sanimalView.setImageList(sanimalData.getImageData().getHeadDirectory());
				}
			}
		});
		// When the user selects a new location from the drop down
		sanimalView.addLocationSelectedListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent event)
			{
				if (event.getStateChange() == ItemEvent.SELECTED)
				{
					Location selected = ((Location) event.getItem());
					if (selected != null)
						for (ImageEntry selectedImage : sanimalView.getSelectedImageEntries())
							selectedImage.setLocationTaken(selected);
					sanimalView.setLocation(selected);
				}
			}
		});
		// When the user wants to add a new location
		sanimalView.addALToAddNewLocation(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				Location newLocation = sanimalView.askUserForNewLocation();
				if (newLocation != null)
				{
					sanimalData.getLocationData().addLocation(newLocation);
					sanimalView.setLocationList(sanimalData.getLocationData().getRegisteredLocations());
				}
			}
		});
		// When the user wants to add a new species
		sanimalView.addALToAddNewSpecies(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				Species species = sanimalView.askUserForNewSpecies();
				if (species != null)
				{
					sanimalData.getSpeciesData().addSpecies(species);
					sanimalView.setSpeciesList(sanimalData.getSpeciesData().getRegisteredSpecies());
				}
			}
		});
		// When the user wants to remove a location
		sanimalView.addALToRemoveLocation(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
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
					sanimalView.setLocationList(sanimalData.getLocationData().getRegisteredLocations());
					SanimalController.this.selectedItemUpdated();
				}
			}
		});
		// When the user wants to remove a species
		sanimalView.addALToRemoveSpecies(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
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
					sanimalView.setSpeciesList(sanimalData.getSpeciesData().getRegisteredSpecies());
					SanimalController.this.selectedItemUpdated();
				}
			}
		});
		// When the user adds a new species to the image
		sanimalView.addALToAddSpeciesToList(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				Species selectedSpecies = sanimalView.getSelectedSpecies();
				if (selectedSpecies != null)
				{
					if (!sanimalView.getSelectedImageEntries().isEmpty())
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
						List<ImageEntry> selectedImages = sanimalView.getSelectedImageEntries();
						for (ImageEntry imageEntry : selectedImages)
							imageEntry.addSpecies(selectedSpecies, numberOfAnimals);
						if (selectedImages.size() == 1)
							sanimalView.setSpeciesEntryList(selectedImages.get(0).getSpeciesPresent());
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
				SanimalController.this.selectedItemUpdated();
			}
		});
		// When the user removes an animal from an image
		sanimalView.addALToRemoveSpeciesFromList(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				Species selectedSpecies = sanimalView.getSelectedSpecies();
				List<ImageEntry> selectedImages = sanimalView.getSelectedImageEntries();
				if (selectedSpecies != null)
				{
					for (ImageEntry imageEntry : selectedImages)
						imageEntry.removeSpecies(selectedSpecies);
				}
				SanimalController.this.selectedItemUpdated();
			}
		});
		sanimalView.setVisible(true);

		///
		/// End setup sanimalView with action listeners 
		///
	}

	public void selectedItemUpdated()
	{
		ImageEntry first = null;
		Location firstLocation = null;
		String firstDate = null;
		List<SpeciesEntry> firstSpeciesEntries = null;
		List<ImageEntry> selectedImages = sanimalView.getSelectedImageEntries();
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
		sanimalView.setLocation(firstLocation);
		sanimalView.setDate(firstDate);
		sanimalView.setSpeciesEntryList(firstSpeciesEntries);
		sanimalView.refreshLocationFields();
		if (selectedImages.size() == 1)
			sanimalView.setThumbnailImage(first);
		else
			sanimalView.setThumbnailImage(null);
	}
}
