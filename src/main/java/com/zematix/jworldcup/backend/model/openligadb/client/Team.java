
package com.zematix.jworldcup.backend.model.openligadb.client;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * <p>Java class for Team complex type.
 */
public class Team {

	@JsonProperty("TeamId")
    protected int teamId;
	
	@JsonProperty("TeamName")
    protected String teamName;
	
	@JsonProperty("ShortName")
	protected String shortName;
	
	@JsonProperty("TeamIconUrl")
    protected String teamIconURL;

	@JsonProperty("TeamGroupName")
    protected String teamGroupName;

    /**
     * Gets the value of the teamId property.
     * 
     */
    public int getTeamId() {
        return teamId;
    }

    /**
     * Sets the value of the teamId property.
     * 
     */
    public void setTeamId(int value) {
        this.teamId = value;
    }

    /**
     * Gets the value of the teamName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Sets the value of the teamName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTeamName(String value) {
        this.teamName = value;
    }

    /**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
     * Gets the value of the teamIconURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTeamIconURL() {
        return teamIconURL;
    }

    /**
     * Sets the value of the teamIconURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTeamIconURL(String value) {
        this.teamIconURL = value;
    }

	/**
	 * @return the teamGroupName
	 */
	public String getTeamGroupName() {
		return teamGroupName;
	}

	/**
	 * @param teamGroupName the teamGroupName to set
	 */
	public void setTeamGroupName(String teamGroupName) {
		this.teamGroupName = teamGroupName;
	}

}
