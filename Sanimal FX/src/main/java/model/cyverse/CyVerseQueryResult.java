package model.cyverse;

import java.time.LocalDateTime;

public class CyVerseQueryResult
{
	private String irodsFileAbsolutePath;

	private LocalDateTime dateTimeTaken;
	private String locationName;
	private String locationID;
	private Double locationLatitude;
	private Double locationLongitude;
	private Double locationElevation;

	private String speciesName;
	private String speciesScientificName;
	private Integer speciesCount;

	public CyVerseQueryResult(String irodsFileAbsolutePath)
	{
		this.irodsFileAbsolutePath = irodsFileAbsolutePath;
	}

	public LocalDateTime getDateTimeTaken()
	{
		return dateTimeTaken;
	}

	public void setDateTimeTaken(LocalDateTime dateTimeTaken)
	{
		this.dateTimeTaken = dateTimeTaken;
	}

	public String getLocationName()
	{
		return locationName;
	}

	public void setLocationName(String locationName)
	{
		this.locationName = locationName;
	}

	public String getLocationID()
	{
		return locationID;
	}

	public void setLocationID(String locationID)
	{
		this.locationID = locationID;
	}

	public Double getLocationLatitude()
	{
		return locationLatitude;
	}

	public void setLocationLatitude(Double locationLatitude)
	{
		this.locationLatitude = locationLatitude;
	}

	public Double getLocationLongitude()
	{
		return locationLongitude;
	}

	public void setLocationLongitude(Double locationLongitude)
	{
		this.locationLongitude = locationLongitude;
	}

	public Double getLocationElevation()
	{
		return locationElevation;
	}

	public void setLocationElevation(Double locationElevation)
	{
		this.locationElevation = locationElevation;
	}

	public String getSpeciesName()
	{
		return speciesName;
	}

	public void setSpeciesName(String speciesName)
	{
		this.speciesName = speciesName;
	}

	public String getSpeciesScientificName()
	{
		return speciesScientificName;
	}

	public void setSpeciesScientificName(String speciesScientificName)
	{
		this.speciesScientificName = speciesScientificName;
	}

	public Integer getSpeciesCount()
	{
		return speciesCount;
	}

	public void setSpeciesCount(Integer speciesCount)
	{
		this.speciesCount = speciesCount;
	}

	public String getIrodsFileAbsolutePath()
	{
		return irodsFileAbsolutePath;
	}

	public void setIrodsFileAbsolutePath(String irodsFileAbsolutePath)
	{
		this.irodsFileAbsolutePath = irodsFileAbsolutePath;
	}
}
