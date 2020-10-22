package com.zematix.jworldcup.backend.model.openligadb.client;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.msiggi.openligadb.client.ArrayOfGoal;
import com.msiggi.openligadb.client.ArrayOfMatchResult;


/**
 * <p>Java class for Matchdata complex type.
 */
public class Matchdata {

	@JsonProperty("MatchID")
    protected int matchId;
    
    @XmlSchemaType(name = "dateTime")
    @JsonProperty(value = "MatchDateTime", required = true)
    protected XMLGregorianCalendar matchDateTime;
    
    @XmlElement(name = "TimeZoneID")
    protected String timeZoneId;

    @JsonProperty("LeagueId")
    protected int leagueID;
    
    @JsonProperty("LeagueName")
    protected String leagueName;
    
    @XmlSchemaType(name = "dateTime")
    @JsonProperty(value = "MatchDateTimeUTC", required = true)
    protected XMLGregorianCalendar matchDateTimeUTC;
    
    @JsonProperty("Group")
    protected Group group;
    
    @JsonProperty("Team1")
    protected Team team1;
    
    @JsonProperty("Team2")
    protected Team team2;
    
    @XmlSchemaType(name = "dateTime")
    @JsonProperty(value = "LastUpdateDateTime", required = true)
    protected XMLGregorianCalendar lastUpdateDateTime;

    @JsonProperty("MatchIsFinished")
    protected boolean matchIsFinished;

    @JsonProperty("MatchResults")
    protected List<MatchResult> matchResults;
    
    @JsonProperty("Goals")
    protected List<Goal> goals;

    @JsonProperty(value = "NumberOfViewers")
    protected Integer numberOfViewers;

    @JsonProperty(value = "Location", required = true)
    protected Location location;

    /**
     * Gets the value of the matchID property.
     * 
     */
    public int getMatchId() {
        return matchId;
    }

    /**
     * Sets the value of the matchID property.
     * 
     */
    public void setMatchId(int value) {
        this.matchId = value;
    }

    /**
     * Gets the value of the matchDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMatchDateTime() {
        return matchDateTime;
    }

    /**
     * Sets the value of the matchDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMatchDateTime(XMLGregorianCalendar value) {
        this.matchDateTime = value;
    }

    /**
     * Gets the value of the timeZoneID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimeZoneId() {
        return timeZoneId;
    }

    /**
     * Sets the value of the timeZoneID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimeZoneId(String value) {
        this.timeZoneId = value;
    }

    /**
     * Gets the value of the leagueID property.
     * 
     */
    public int getLeagueID() {
        return leagueID;
    }

    /**
     * Sets the value of the leagueID property.
     * 
     */
    public void setLeagueID(int value) {
        this.leagueID = value;
    }

    /**
     * Gets the value of the leagueName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLeagueName() {
        return leagueName;
    }

    /**
     * Sets the value of the leagueName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLeagueName(String value) {
        this.leagueName = value;
    }

    /**
     * Gets the value of the matchDateTimeUTC property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMatchDateTimeUTC() {
        return matchDateTimeUTC;
    }

    /**
     * Sets the value of the matchDateTimeUTC property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setMatchDateTimeUTC(XMLGregorianCalendar value) {
        this.matchDateTimeUTC = value;
    }

    /**
     * Gets the value of the numberOfViewers property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNumberOfViewers() {
        return numberOfViewers;
    }

    /**
     * Sets the value of the numberOfViewers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNumberOfViewers(Integer value) {
        this.numberOfViewers = value;
    }

    /**
     * Gets the value of the matchIsFinished property.
     * 
     */
    public boolean isMatchIsFinished() {
        return matchIsFinished;
    }

    /**
     * Sets the value of the matchIsFinished property.
     * 
     */
    public void setMatchIsFinished(boolean value) {
        this.matchIsFinished = value;
    }

    /**
     * Gets the value of the matchResults property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMatchResult }
     *     
     */
    public List<MatchResult> getMatchResults() {
        return matchResults;
    }

    /**
     * Sets the value of the matchResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMatchResult }
     *     
     */
    public void setMatchResults(List<MatchResult> value) {
        this.matchResults = value;
    }

    /**
     * Gets the value of the goals property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfGoal }
     *     
     */
    public List<Goal> getGoals() {
        return goals;
    }

    /**
     * Sets the value of the goals property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfGoal }
     *     
     */
    public void setGoals(List<Goal> value) {
        this.goals = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link Location }
     *     
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link Location }
     *     
     */
    public void setLocation(Location value) {
        this.location = value;
    }

}
