package com.smartcampus.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

public class DataStore {

    // Single shared instance used across the whole application
    private static final DataStore INSTANCE = new DataStore();

    // Returns the single DataStore object so all resources use the same in-memory data
    public static DataStore getInstance() {
        return INSTANCE;
    }

    // Main in-memory collection for storing rooms by their ID
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Main in-memory collection for storing sensors by their ID
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Stores the reading history for each sensor using the sensor ID as the key
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    // Private constructor is used to enforce the singleton pattern and seed the API with sample data
    private DataStore() {
        // Creating some sample rooms so the API can be tested straight away
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        Room r3 = new Room("HALL-A", "Main Lecture Hall A", 200);

        // Storing the sample rooms in the room map
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // Creating some sample sensors linked to existing rooms
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 0.0, "LAB-101");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "OFFLINE", 18.0, "HALL-A");

        // Storing the sample sensors in the sensor map
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);
        sensors.put(s4.getId(), s4);

        // Preparing an empty reading list for each seeded sensor
        sensorReadings.put(s1.getId(), Collections.synchronizedList(new ArrayList<>()));
        sensorReadings.put(s2.getId(), Collections.synchronizedList(new ArrayList<>()));
        sensorReadings.put(s3.getId(), Collections.synchronizedList(new ArrayList<>()));
        sensorReadings.put(s4.getId(), Collections.synchronizedList(new ArrayList<>()));

        // Linking each sensor ID back to the room it belongs to
        r1.addSensorId("TEMP-001");
        r1.addSensorId("CO2-001");
        r2.addSensorId("OCC-001");
        r3.addSensorId("TEMP-002");

        // Adding a few starting readings to make the historical reading endpoint testable
        sensorReadings.get("TEMP-001").add(new SensorReading(21.0));
        sensorReadings.get("TEMP-001").add(new SensorReading(22.5));
        sensorReadings.get("CO2-001").add(new SensorReading(410.0));
    }

    // Returns all rooms as a read-only map so outside classes cannot modify it directly
    public Map<String, Room> getRooms() {
        return Collections.unmodifiableMap(rooms);
    }

    // Finds and returns one room by its ID
    public Room getRoom(String id) {
        return rooms.get(id);
    }

    // Adds or replaces a room in the in-memory store if the object is valid
    public void putRoom(Room room) {
        if (room != null) {
            rooms.put(room.getId(), room);
        }
    }

    // Removes a room by ID and returns true if a room was actually deleted
    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    // Checks whether a room with the given ID exists
    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    // Returns all sensors as a read-only map
    public Map<String, Sensor> getSensors() {
        return Collections.unmodifiableMap(sensors);
    }

    // Finds and returns one sensor by its ID
    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    // Adds or replaces a sensor and also makes sure it has a reading list ready
    public void putSensor(Sensor sensor) {
        if (sensor != null) {
            sensors.put(sensor.getId(), sensor);
            sensorReadings.putIfAbsent(sensor.getId(), Collections.synchronizedList(new ArrayList<>()));
        }
    }

    // Checks whether a sensor with the given ID exists
    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    // Deletes a sensor and also removes its reading history from the data store
    public boolean deleteSensor(String id) {
        sensorReadings.remove(id);
        return sensors.remove(id) != null;
    }

    // Returns a safe copy of the readings for one sensor so the original list is not exposed
    public List<SensorReading> getReadings(String sensorId) {
        List<SensorReading> readings = sensorReadings.get(sensorId);

        // Returning an empty list if the sensor has no stored readings
        if (readings == null) {
            return new ArrayList<>();
        }

        // Synchronizing before copying to avoid issues during concurrent access
        synchronized (readings) {
            return new ArrayList<>(readings);
        }
    }

    // Adds a new reading to the correct sensor history if both values are valid
    public void addReading(String sensorId, SensorReading reading) {
        // Ignoring the request if the sensor ID or reading object is missing
        if (sensorId == null || reading == null) {
            return;
        }

        // Creating the reading list first if that sensor does not already have one
        List<SensorReading> readings =
                sensorReadings.computeIfAbsent(sensorId, key -> Collections.synchronizedList(new ArrayList<>()));

        // Synchronizing the list before adding the new reading
        synchronized (readings) {
            readings.add(reading);
        }
    }
}