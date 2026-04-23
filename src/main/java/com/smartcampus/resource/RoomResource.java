package com.smartcampus.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

// Handles the main room collection and individual room operations
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    // Using the shared in-memory data store across all room operations
    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        // Getting all stored rooms and returning them as a list
        Collection<Room> allRooms = store.getRooms().values();
        List<Room> roomList = new ArrayList<>(allRooms);
        return Response.ok(roomList).build();
    }

    @POST
    public Response createRoom(Room room) {
        // Validating that the request body was actually sent
        if (room == null) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Room body is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validating the required room ID
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Room 'id' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validating the required room name
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Room 'name' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Capacity must be a positive number
        if (room.getCapacity() <= 0) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Room 'capacity' must be greater than 0."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Trimming text values before storing them
        String cleanId = room.getId().trim();
        String cleanName = room.getName().trim();

        // Checking again after trimming in case the value only contained spaces
        if (cleanId.isEmpty()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Room 'id' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Checking again after trimming in case the value only contained spaces
        if (cleanName.isEmpty()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Room 'name' is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Preventing duplicate rooms from being created with the same ID
        if (store.roomExists(cleanId)) {
            return Response.status(409)
                    .entity(buildError(409, "Conflict", "A room with ID '" + cleanId + "' already exists."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Creating a clean room object and ignoring any sensor IDs sent by the client
        Room cleanRoom = new Room(cleanId, cleanName, room.getCapacity());
        cleanRoom.setSensorIds(new ArrayList<>());

        store.putRoom(cleanRoom);

        // Returning 201 Created with the new room and its resource location
        URI location = URI.create("/api/v1/rooms/" + cleanRoom.getId());
        return Response.created(location).entity(cleanRoom).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        // Cleaning the path parameter before using it
        String cleanRoomId = roomId == null ? "" : roomId.trim();

        // Rejecting empty room IDs in the path
        if (cleanRoomId.isEmpty()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Room ID path parameter is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Looking up the requested room by its ID
        Room room = store.getRoom(cleanRoomId);
        if (room == null) {
            return Response.status(404)
                    .entity(buildError(404, "Not Found", "Room with ID '" + cleanRoomId + "' does not exist."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        // Cleaning the path parameter before using it
        String cleanRoomId = roomId == null ? "" : roomId.trim();

        // Rejecting empty room IDs in the path
        if (cleanRoomId.isEmpty()) {
            return Response.status(400)
                    .entity(buildError(400, "Bad Request", "Room ID path parameter is required."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Checking whether the room exists before trying to delete it
        Room room = store.getRoom(cleanRoomId);
        if (room == null) {
            return Response.status(404)
                    .entity(buildError(404, "Not Found", "Room with ID '" + cleanRoomId + "' does not exist."))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Getting all sensors currently linked to the room
        List<String> assignedSensorIds = room.getSensorIds();
        List<String> activeSensorIds = new ArrayList<>();

        // Separating active sensors because they are specifically mentioned in the business rule
        for (String sensorId : assignedSensorIds) {
            Sensor sensor = store.getSensor(sensorId);
            if (sensor != null && "ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
                activeSensorIds.add(sensorId);
            }
        }

        // Blocking deletion if the room still has any sensors assigned
        if (!assignedSensorIds.isEmpty()) {
            if (!activeSensorIds.isEmpty()) {
                throw new RoomNotEmptyException(
                        "Room '" + cleanRoomId + "' cannot be deleted because it has " +
                        activeSensorIds.size() + " active sensor(s) still assigned: " +
                        activeSensorIds
                );
            }

            throw new RoomNotEmptyException(
                    "Room '" + cleanRoomId + "' cannot be deleted because it still has sensor(s) assigned, " +
                    "even though they are not currently ACTIVE: " + assignedSensorIds
            );
        }

        // Deleting the room only when no sensors are linked to it
        store.deleteRoom(cleanRoomId);

        // Returning a simple success response after deletion
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Room '" + cleanRoomId + "' has been successfully decommissioned.");
        result.put("deletedRoomId", cleanRoomId);
        return Response.ok(result).build();
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