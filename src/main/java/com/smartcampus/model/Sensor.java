package com.smartcampus.model;

// Represents a sensor that belongs to a room and stores its current state and latest value
public class Sensor {

    // Allowed sensor status values used across the API
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";
    public static final String STATUS_OFFLINE = "OFFLINE";

    private String id;
    private String type;
    private String status;
    private double currentValue;
    private String roomId;

    // Default constructor needed so Sensor objects can be created from incoming JSON
    public Sensor() {
    }

    // Main constructor used when creating a sensor with all of its main details
    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    // Returns the sensor ID
    public String getId() {
        return id;
    }

    // Sets the sensor ID
    public void setId(String id) {
        this.id = id;
    }

    // Returns the sensor type such as Temperature or CO2
    public String getType() {
        return type;
    }

    // Sets the sensor type
    public void setType(String type) {
        this.type = type;
    }

    // Returns the current sensor status
    public String getStatus() {
        return status;
    }

    // Sets the current sensor status
    public void setStatus(String status) {
        this.status = status;
    }

    // Returns the latest recorded value for the sensor
    public double getCurrentValue() {
        return currentValue;
    }

    // Updates the latest recorded value for the sensor
    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    // Returns the ID of the room this sensor is linked to
    public String getRoomId() {
        return roomId;
    }

    // Sets the room ID this sensor belongs to
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}