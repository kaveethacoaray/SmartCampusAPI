package com.smartcampus.application;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    // Starts the Grizzly server and registers JSON support for the API
    public static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new SmartCampusApplication());
        rc.register(JacksonFeature.class);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    // Starts the server and keeps it running until the user stops it from the console by pressing enter 
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();

        // Logging the main API address and discovery endpoint for quick testing
        LOGGER.info("Smart Campus API started at: " + BASE_URI);
        LOGGER.info("Discovery endpoint: " + BASE_URI);
        LOGGER.info("Rooms endpoint: " + BASE_URI + "rooms");
        LOGGER.info("Sensors endpoint: " + BASE_URI + "sensors");
        LOGGER.info("Example readings endpoint: " + BASE_URI + "sensors/TEMP-001/readings");
        LOGGER.info("Press ENTER to stop the server...");

        System.in.read();
        server.shutdownNow();
    }
}