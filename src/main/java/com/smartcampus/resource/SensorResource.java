package com.smartcampus.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

// Handles the main sensor collection and sensor-related operations
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // Using the shared in-memory data store across all sensor operations
    private final DataStore store = DataStore.getInstance();

    // Allowed status values used when validating new sensors
    private static final Set<String> ALLOWED_STATUSES = new HashSet<>(
            Arrays.asList("ACTIVE", "MAINTENANCE", "OFFLINE")
    );

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        // Getting all sensors currently stored in memory
        Collection<Sensor> all = store.getSensors().values();

        List<Sensor> result;

        // Applying optional filtering when a sensor type is provided in the query string
        if (type != null && !type.isBlank()) {
            String cleanedType = type.trim();

            result = all.stream()
                    .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(cleanedType))
                    .collect(Collectors.toList());
        } else {
            result = new ArrayList<>(all);
        }

        return Response.ok(result).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        // Validating that the request body exists
        if (sensor == null) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Sensor body is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validating the required sensor ID
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Sensor 'id' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validating the required sensor type
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Sensor 'type' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validating the required sensor status
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Sensor 'status' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validating the required linked room ID
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Sensor 'roomId' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Trimming and normalizing text values before storing them
        String cleanId = sensor.getId().trim();
        String cleanType = sensor.getType().trim();
        String cleanStatus = sensor.getStatus().trim().toUpperCase();
        String cleanRoomId = sensor.getRoomId().trim();

        // Checking again after trimming in case the value only had spaces
        if (cleanId.isEmpty()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Sensor 'id' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Checking again after trimming in case the value only had spaces
        if (cleanType.isEmpty()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Sensor 'type' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Checking again after trimming in case the value only had spaces
        if (cleanRoomId.isEmpty()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Sensor 'roomId' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Making sure the status is one of the values allowed by the API
        if (!ALLOWED_STATUSES.contains(cleanStatus)) {
            return Response.status(400)
                    .entity(buildError(
                            400,
                            "Bad Request",
                            "Sensor 'status' must be one of: ACTIVE, MAINTENANCE, OFFLINE."
                    ))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Rejecting invalid numeric values for the current sensor reading
        if (Double.isNaN(sensor.getCurrentValue()) || Double.isInfinite(sensor.getCurrentValue())) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Sensor 'currentValue' must be a valid number."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Preventing duplicate sensors from being created with the same ID
        if (store.sensorExists(cleanId)) {
            return Response.status(409)
                    .entity(buildError(409, "Conflict", "Sensor '" + cleanId + "' already exists."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Making sure the linked room really exists before creating the sensor
        Room room = store.getRoom(cleanRoomId);
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Cannot register sensor: the referenced roomId '" + cleanRoomId +
                    "' does not exist in the system. Create the room first."
            );
        }

        // Saving back the cleaned values so the stored data stays consistent
        sensor.setId(cleanId);
        sensor.setType(cleanType);
        sensor.setStatus(cleanStatus);
        sensor.setRoomId(cleanRoomId);

        // Storing the sensor and linking its ID to the correct room
        store.putSensor(sensor);
        room.addSensorId(cleanId);

        URI location = URI.create("/api/v1/sensors/" + cleanId);
        return Response.created(location).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        // Looking up one sensor by its ID
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(buildError(404, "Not Found", "Sensor '" + sensorId + "' does not exist."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        return Response.ok(sensor).build();
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        // Checking whether the sensor exists before trying to remove it
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(buildError(404, "Not Found", "Sensor '" + sensorId + "' does not exist."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Removing the sensor ID from the room it belongs to
        Room room = store.getRoom(sensor.getRoomId());
        if (room != null) {
            room.removeSensorId(sensorId);
        }

        // Deleting the sensor and its saved reading history from the data store
        store.deleteSensor(sensorId);

        // Returning a simple success response after deletion
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Sensor '" + sensorId + "' has been removed.");
        result.put("deletedSensorId", sensorId);
        return Response.ok(result).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        // Checking the sensor exists before forwarding the request to the nested readings resource
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor '" + sensorId + "' not found.");
        }

        return new SensorReadingResource(sensorId);
    }

    // Builds a consistent JSON error body used by this resource
    private Map<String, Object> buildError(int status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }
}