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
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
				view.addImageListValueChanged(new ListSelectionListener()
				{
					@Override
					public void valueChanged(ListSelectionEvent event)
					{
						if (view.getMinSelectedImageIndex() != -1)
						{
							List<ImageEntry> images = sanimalData.getImageData().getImages();
							ImageEntry first = images.get(view.getMinSelectedImageIndex());
							Location firstLocation = first.getLocationTaken();
							String firstDate = first.getDateTakenFormatted();
							for (int i = view.getMinSelectedImageIndex(); i <= view.getMaxSelectedImageIndex(); i++)
							{
								if (!images.get(i).getDateTakenFormatted().equals(firstDate))
									firstDate = null;
								if (images.get(i).getLocationTaken() != firstLocation)
									firstLocation = null;
							}
							view.setLocation(firstLocation);
							view.setDate(firstDate);
							if (view.getMinSelectedImageIndex() == view.getMaxSelectedImageIndex())
							{
								view.setThumbnailImage(first);
								view.setSpeciesEntryList(first.getSpeciesPresent());
							}
							else
							{
								view.setThumbnailImage(null);
								view.setSpeciesEntryList(null);
							}
						}
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
							view.setImageList(sanimalData.getImageData().getImages());
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
								if (view.getMinSelectedImageIndex() != -1)
									for (int i = view.getMinSelectedImageIndex(); i <= view.getMaxSelectedImageIndex(); i++)
										sanimalData.getImageData().getImages().get(i).setLocationTaken(selected);
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
							for (ImageEntry image : sanimalData.getImageData().getImages())
								if (image.getLocationTaken() == selected)
									numberOfSelectedOccourances++;
							if (numberOfSelectedOccourances >= 1)
							{
								int response = JOptionPane.showConfirmDialog(view, "This location has already been set on multiple images, continue removing the location from all images?");
								if (response != JOptionPane.YES_OPTION)
									return;
							}
							for (ImageEntry image : sanimalData.getImageData().getImages())
								if (image.getLocationTaken() == selected)
									image.setLocationTaken(null);
							sanimalData.getLocationData().removeLocation(selected.toString());
							view.setLocationList(sanimalData.getLocationData().getRegisteredLocations());
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
							for (ImageEntry image : sanimalData.getImageData().getImages())
								for (SpeciesEntry entry : image.getSpeciesPresent())
									if (entry.getSpecies() == selected)
										numberOfSelectedOccourances++;
							if (numberOfSelectedOccourances >= 1)
							{
								int response = JOptionPane.showConfirmDialog(view, "This species has already been set on multiple images, continue removing the species from all images?");
								if (response != JOptionPane.YES_OPTION)
									return;
							}
							for (ImageEntry image : sanimalData.getImageData().getImages())
							{
								Iterator<SpeciesEntry> iterator = image.getSpeciesPresent().iterator();
								while (iterator.hasNext())
								{
									SpeciesEntry current = iterator.next();
									if (current.getSpecies() == selected)
										iterator.remove();
								}
							}
							sanimalData.getSpeciesData().removeSpecies(selected);
							view.setSpeciesList(sanimalData.getSpeciesData().getRegisteredSpecies());
							if (view.getMinSelectedImageIndex() == view.getMaxSelectedImageIndex())
								view.setSpeciesEntryList(sanimalData.getImageData().getImages().get(view.getMinSelectedImageIndex()).getSpeciesPresent());
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
						int minSelectedIndex = view.getMinSelectedImageIndex();
						int maxSelectedIndex = view.getMaxSelectedImageIndex();
						if (selectedSpecies != null && minSelectedIndex != -1)
						{
							List<ImageEntry> imageEntries = sanimalData.getImageData().getImages();
							for (int i = minSelectedIndex; i <= maxSelectedIndex; i++)
							{
								ImageEntry current = imageEntries.get(i);
								current.addSpecies(selectedSpecies, numberOfAnimals);
							}
							if (minSelectedIndex == maxSelectedIndex)
								view.setSpeciesEntryList(imageEntries.get(minSelectedIndex).getSpeciesPresent());
						}
					}
				});
				view.setVisible(true);
			}
		});
	}
}
