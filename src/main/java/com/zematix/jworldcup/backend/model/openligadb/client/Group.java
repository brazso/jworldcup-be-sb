package com.zematix.jworldcup.backend.model.openligadb.client;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * <p>Java class for Group complex type.
 */
public class Group {

	@JsonProperty("GroupName")
    protected String groupName;
	
	@JsonProperty("GroupOrderID")
    protected int groupOrderId;
	
	@JsonProperty("GroupID")
    protected int groupId;

    /**
     * Gets the value of the groupName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the value of the groupName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupName(String value) {
        this.groupName = value;
    }

    /**
     * Gets the value of the groupOrderId property.
     * 
     */
    public int getGroupOrderId() {
        return groupOrderId;
    }

    /**
     * Sets the value of the groupOrderId property.
     * 
     */
    public void setGroupOrderId(int value) {
        this.groupOrderId = value;
    }

    /**
     * Gets the value of the groupId property.
     * 
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * Sets the value of the groupId property.
     * 
     */
    public void setGroupId(int value) {
        this.groupId = value;
    }

}
