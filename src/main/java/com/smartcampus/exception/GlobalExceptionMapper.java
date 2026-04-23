package com.smartcampus.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Catches any unexpected error that was not handled by the more specific exception mappers
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Logging the full exception so the server keeps the technical details internally
        LOGGER.log(
                Level.SEVERE,
                "Unhandled exception intercepted by GlobalExceptionMapper: " + exception.getMessage(),
                exception
        );

        // Building a safe JSON error response instead of exposing raw Java error details
        Map<String, Object> error = new HashMap<>();
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred on the server. Please try again later.");
        error.put("timestamp", System.currentTimeMillis());

        // Returning a standard 500 response in JSON format
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}