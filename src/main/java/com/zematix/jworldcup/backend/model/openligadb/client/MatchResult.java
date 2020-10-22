package com.zematix.jworldcup.backend.model.openligadb.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Java class for matchResult complex type.
 */
public class MatchResult {

	@JsonProperty("ResultID")
	protected int resultId;
	
	@JsonProperty("ResultName")
    protected String resultName;
    
	@JsonProperty("PointsTeam1")
    protected int pointsTeam1;
	
	@JsonProperty("PointsTeam2")
    protected int pointsTeam2;
	
	@JsonProperty("ResultOrderID")
    protected int resultOrderId;

	@JsonProperty("ResultTypeID")
    protected int resultTypeId;
    
	@JsonProperty("ResultDescription")
    protected String resultDescription;


    /**
     * Gets the value of the resultId property.
     * 
     */
    public int getResultId() {
        return resultId;
    }

    /**
     * Sets the value of the resultId property.
     * 
     */
    public void setResultId(int value) {
        this.resultId = value;
    }

    /**
     * Gets the value of the resultName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResultName() {
        return resultName;
    }

    /**
     * Sets the value of the resultName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResultName(String value) {
        this.resultName = value;
    }

    /**
     * Gets the value of the pointsTeam1 property.
     * 
     */
    public int getPointsTeam1() {
        return pointsTeam1;
    }

    /**
     * Sets the value of the pointsTeam1 property.
     * 
     */
    public void setPointsTeam1(int value) {
        this.pointsTeam1 = value;
    }

    /**
     * Gets the value of the pointsTeam2 property.
     * 
     */
    public int getPointsTeam2() {
        return pointsTeam2;
    }

    /**
     * Sets the value of the pointsTeam2 property.
     * 
     */
    public void setPointsTeam2(int value) {
        this.pointsTeam2 = value;
    }

    /**
     * Gets the value of the resultOrderId property.
     * 
     */
    public int getResultOrderId() {
        return resultOrderId;
    }

    /**
     * Sets the value of the resultOrderId property.
     * 
     */
    public void setResultOrderId(int value) {
        this.resultOrderId = value;
    }

    /**
     * Gets the value of the resultTypeId property.
     * 
     */
    public int getResultTypeId() {
        return resultTypeId;
    }

    /**
     * Sets the value of the resultTypeId property.
     * 
     */
    public void setResultTypeId(int value) {
        this.resultTypeId = value;
    }

    /**
     * Gets the value of the resultDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResultDescription() {
        return resultDescription;
    }

    /**
     * Sets the value of the resultDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResultDescription(String value) {
        this.resultDescription = value;
    }

}
