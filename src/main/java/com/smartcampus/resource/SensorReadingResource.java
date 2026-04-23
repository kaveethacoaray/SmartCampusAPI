package com.smartcampus.resource;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

// Handles the nested readings endpoint for a specific sensor
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    // Stores the parent sensor ID passed from the sub-resource locator
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        // Getting the full reading history for the selected sensor
        List<SensorReading> readings = store.getReadings(sensorId);

        // Returning the sensor ID together with the reading count and list
        Map<String, Object> response = new HashMap<>();
        response.put("sensorId", sensorId);
        response.put("count", readings.size());
        response.put("readings", readings);

        return Response.ok(response).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        // Getting the parent sensor before adding a new reading
        Sensor sensor = store.getSensor(sensorId);

        // Guarding against invalid sensor access even though the parent resource should already validate it
        if (sensor == null) {
            Map<String, Object> error = buildError(404, "Not Found", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(404)
                    .entity(error)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Only ACTIVE sensors are allowed to accept new readings
        String currentStatus = sensor.getStatus();
        String safeStatus = (currentStatus == null || currentStatus.isBlank()) ? "UNKNOWN" : currentStatus;

        if (!"ACTIVE".equalsIgnoreCase(safeStatus)) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently '" + safeStatus +
                    "' and cannot record new readings. Only ACTIVE sensors can accept readings."
            );
        }

        // Validating that the request body was actually sent
        if (reading == null) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Reading body is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Rejecting invalid numeric reading values
        if (Double.isNaN(reading.getValue()) || Double.isInfinite(reading.getValue())) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Reading value must be a valid number."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Storing any timestamp that came from the client before possibly recreating the reading object
        long providedTimestamp = reading.getTimestamp();

        // Creating missing reading metadata while preserving a valid timestamp where possible
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading = new SensorReading(reading.getValue());

            if (providedTimestamp != 0) {
                reading.setTimestamp(providedTimestamp);
            } else if (reading.getTimestamp() == 0) {
                reading.setTimestamp(System.currentTimeMillis());
            }
        } else if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Saving the reading into the sensor history
        store.addReading(sensorId, reading);

        // Updating the parent sensor so its current value always matches the latest reading
        sensor.setCurrentValue(reading.getValue());

        // Returning a success response with the stored reading and updated sensor value
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Reading recorded successfully for sensor '" + sensorId + "'.");
        response.put("reading", reading);
        response.put("updatedSensorCurrentValue", sensor.getCurrentValue());

        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings");
        return Response.created(location).entity(response).build();
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