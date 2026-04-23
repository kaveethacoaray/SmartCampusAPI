package com.smartcampus.model;

import java.util.UUID;

// Represents one historical reading recorded for a sensor
public class SensorReading {

    private String id;
    private long timestamp;
    private double value;

    // Default constructor needed so SensorReading objects can be created from incoming JSON
    public SensorReading() {
    }

    // Creates a new reading with an auto-generated ID and the current system time
    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    // Returns the reading ID
    public String getId() {
        return id;
    }

    // Sets the reading ID
    public void setId(String id) {
        this.id = id;
    }

    // Returns the time the reading was recorded
    public long getTimestamp() {
        return timestamp;
    }

    // Sets the reading timestamp
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Returns the sensor reading value
    public double getValue() {
        return value;
    }

    // Sets the sensor reading value
    public void setValue(double value) {
        this.value = value;
    }
}