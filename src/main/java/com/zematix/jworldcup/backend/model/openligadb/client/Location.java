package com.zematix.jworldcup.backend.model.openligadb.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Java class for Location complex type.
 */
public class Location {

	@JsonProperty("LocationCity")
    protected String locationCity;
	
	@JsonProperty("LocationID")
    protected int locationId;
	
	@JsonProperty("LocationStadium")
    protected String locationStadium;

    /**
     * Gets the value of the locationCity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocationCity() {
        return locationCity;
    }

    /**
     * Sets the value of the locationCity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocationCity(String value) {
        this.locationCity = value;
    }

    /**
     * Gets the value of the locationId property.
     * 
     */
    public int getLocationId() {
        return locationId;
    }

    /**
     * Sets the value of the locationId property.
     * 
     */
    public void setLocationId(int value) {
        this.locationId = value;
    }

    /**
     * Gets the value of the locationStadium property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocationStadium() {
        return locationStadium;
    }

    /**
     * Sets the value of the locationStadium property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocationStadium(String value) {
        this.locationStadium = value;
    }

}
