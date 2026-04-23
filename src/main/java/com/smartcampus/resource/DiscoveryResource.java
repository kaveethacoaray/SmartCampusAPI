package com.smartcampus.resource;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Root discovery endpoint that gives basic API details and navigable resource links
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new HashMap<>();

        // Adding general API information shown at the root endpoint
        response.put("api", "Smart Campus Sensor & Room Management API");
        response.put("version", "1.0.0");
        response.put("status", "operational");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");

        // Adding administrative contact details for the API
        Map<String, String> contact = new HashMap<>();
        contact.put("team", "Campus Infrastructure Team");
        contact.put("email", "kaveetha.20231311@iit.ac.lk");
        contact.put("institution", "University of Westminster");
        response.put("contact", contact);

        // Adding simple HATEOAS-style links so clients can discover the main resources
        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("_links", links);

        // Adding short descriptions of the main resource collections
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "Campus room registry — create, read, and delete physical spaces");
        resources.put("sensors", "IoT sensor registry — manage sensors and their historical readings");
        response.put("resources", resources);

        // Returning the discovery response as HTTP 200 OK
        return Response.ok(response).build();
    }
}