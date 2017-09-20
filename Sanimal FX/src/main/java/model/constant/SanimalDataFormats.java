package model.constant;

import javafx.scene.input.DataFormat;

public class SanimalDataFormats
{
	// Data formats are used for drag and drop
	// Store the location name and id
	public static final DataFormat LOCATION_NAME_FORMAT = new DataFormat("com.dslovikosky.location.locationName");
	public static final DataFormat LOCATION_ID_FORMAT = new DataFormat("com.dslovikosky.location.locationID");
	// Store the species name and scientific name
	public static final DataFormat SPECIES_NAME_FORMAT = new DataFormat("com.dslovikosky.species.speciesName");
	public static final DataFormat SPECIES_SCIENTIFIC_NAME_FORMAT = new DataFormat("com.dslovikosky.species.speciesScientificName");
}
