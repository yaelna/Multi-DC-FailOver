package com.gigaspaces.app.event_processing.common;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;


/**
 * A simple object used to work with the Space. Important properties include the id of the object, a
 * type (used to perform routing when working with partitioned space), the raw data and processed
 * data, and a boolean flag indicating if this Data object was processed or not.
 */
@SpaceClass
public class Data {

    private String id;

    private Long type;

    private String rawData;

    private Boolean processed;

    /**
     * Constructs a new Data object.
     */
    public Data() {

    }

    /**
     * Constructs a new Data object with the given type and raw data.
     */
    public Data(long type, String rawData) {
        this.type = type;
        this.rawData = rawData;
        this.processed = false;
    }

    /**
     * The id of this object.
     */
    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    /**
     * The id of this object. Its value will be auto generated when it is written to the space.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * The type of the data object. Used as the routing field when working with a partitioned
     * space.
     */
    @SpaceRouting
    public Long getType() {
        return type;
    }

    /**
     * The type of the data object. Used as the routing field when working with a partitioned
     * space.
     */
    public void setType(Long type) {
        this.type = type;
    }

    /**
     * The raw data this object holds.
     */
    public String getRawData() {
        return rawData;
    }

    /**
     * The raw data this object holds.
     */
    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    /**
     * A boolean flag indicating if the data object was processed or not.
     */
    public Boolean isProcessed() {
        return processed;
    }

    /**
     * A boolean flag indicating if the data object was processed or not.
     */
    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    @Override
    public String toString() {
        return "Data [id=" + id + ", type=" + type + ", rawData=" + rawData + ", processed=" + processed + "]";
    }
}
