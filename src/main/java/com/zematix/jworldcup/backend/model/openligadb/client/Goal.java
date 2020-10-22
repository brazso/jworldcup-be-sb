package com.zematix.jworldcup.backend.model.openligadb.client;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * <p>Java class for Goal complex type.
 */
public class Goal {

	@JsonProperty("GoalID")
    protected int goalID;
    
    @JsonProperty(value = "ScoreTeam1", required = true)
    protected Integer scoreTeam1;
    
    @JsonProperty(value = "ScoreTeam2", required = true)
    protected Integer scoreTeam2;

    @JsonProperty(value = "MatchMinute", required = true)
    protected Integer matchMinute;

    @JsonProperty("GoalGetterID")
    protected int goalGetterId;

    @JsonProperty("GoalGetterName")
    protected String goalGetterName;
    
    @JsonProperty(value= "IsPenalty", required = true)
    protected Boolean isPenalty;
    
    @JsonProperty(value= "IsOwnGoal", required = true)
    protected Boolean isOwnGoal;
    
    @JsonProperty(value= "IsOvertime", required = true)
    protected Boolean isOvertime;

    @JsonProperty("Comment")
    protected String comment;

    /**
     * Gets the value of the goalID property.
     * 
     */
    public int getGoalID() {
        return goalID;
    }

    /**
     * Sets the value of the goalID property.
     * 
     */
    public void setGoalID(int value) {
        this.goalID = value;
    }

    /**
     * Gets the value of the scoreTeam1 property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getScoreTeam1() {
        return scoreTeam1;
    }

    /**
     * Sets the value of the scoreTeam1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setScoreTeam1(Integer value) {
        this.scoreTeam1 = value;
    }

    /**
     * Gets the value of the scoreTeam2 property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getGoalScoreTeam2() {
        return scoreTeam2;
    }

    /**
     * Sets the value of the scoreTeam2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setGoalScoreTeam2(Integer value) {
        this.scoreTeam2 = value;
    }

    /**
     * Gets the value of the matchMinute property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMatchMinute() {
        return matchMinute;
    }

    /**
     * Sets the value of the matchMinute property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setmatchMinute(Integer value) {
        this.matchMinute = value;
    }

    /**
     * Gets the value of the goalGetterId property.
     * 
     */
    public int getGoalGetterId() {
        return goalGetterId;
    }

    /**
     * Sets the value of the goalGetterId property.
     * 
     */
    public void setGoalGetterId(int value) {
        this.goalGetterId = value;
    }

    /**
     * Gets the value of the goalGetterName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGoalGetterName() {
        return goalGetterName;
    }

    /**
     * Sets the value of the goalGetterName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGoalGetterName(String value) {
        this.goalGetterName = value;
    }

    /**
     * Gets the value of the isPenalty property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPenalty() {
        return isPenalty;
    }

    /**
     * Sets the value of the isPenalty property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPenalty(Boolean value) {
        this.isPenalty = value;
    }

    /**
     * Gets the value of the isOwnGoal property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOwnGoal() {
        return isOwnGoal;
    }

    /**
     * Sets the value of the isOwnGoal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOwnGoal(Boolean value) {
        this.isOwnGoal = value;
    }

    /**
     * Gets the value of the isOvertime property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOvertime() {
        return isOvertime;
    }

    /**
     * Sets the value of the isOvertime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOvertime(Boolean value) {
        this.isOvertime = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

}
