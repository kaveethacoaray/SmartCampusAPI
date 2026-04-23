package com.smartcampus.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Represents one physical room and keeps track of the sensors assigned to it
public class Room {

    private String id;
    private String name;
    private int capacity;
    private List<String> sensorIds = Collections.synchronizedList(new ArrayList<>());

    // Default constructor needed so Room objects can be created from incoming JSON
    public Room() {
    }

    // Main constructor used when creating a room with its basic details
    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.sensorIds = Collections.synchronizedList(new ArrayList<>());
    }

    // Returns the room ID
    public String getId() {
        return id;
    }

    // Sets the room ID
    public void setId(String id) {
        this.id = id;
    }

    // Returns the room name
    public String getName() {
        return name;
    }

    // Sets the room name
    public void setName(String name) {
        this.name = name;
    }

    // Returns the room capacity
    public int getCapacity() {
        return capacity;
    }

    // Sets the room capacity
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Returns a safe copy of the sensor ID list instead of exposing the original list directly
    public List<String> getSensorIds() {
        synchronized (sensorIds) {
            return new ArrayList<>(sensorIds);
        }
    }

    // Replaces the sensor ID list after removing null, blank, and duplicate values
    public void setSensorIds(List<String> sensorIds) {
        List<String> cleanedIds = new ArrayList<>();

        if (sensorIds != null) {
            for (String sensorId : sensorIds) {
                if (sensorId != null) {
                    String cleanSensorId = sensorId.trim();

                    if (!cleanSensorId.isEmpty() && !cleanedIds.contains(cleanSensorId)) {
                        cleanedIds.add(cleanSensorId);
                    }
                }
            }
        }

        this.sensorIds = Collections.synchronizedList(cleanedIds);
    }

    // Adds a sensor ID only if it is valid and not already linked to the room
    public void addSensorId(String sensorId) {
        if (sensorId == null) {
            return;
        }

        String cleanSensorId = sensorId.trim();
        if (cleanSensorId.isEmpty()) {
            return;
        }

        synchronized (sensorIds) {
            if (!sensorIds.contains(cleanSensorId)) {
                sensorIds.add(cleanSensorId);
            }
        }
    }

    // Removes a sensor ID from the room if a valid value is given
    public void removeSensorId(String sensorId) {
        if (sensorId == null) {
            return;
        }

        String cleanSensorId = sensorId.trim();
        if (cleanSensorId.isEmpty()) {
            return;
        }

        synchronized (sensorIds) {
            sensorIds.remove(cleanSensorId);
        }
    }
}