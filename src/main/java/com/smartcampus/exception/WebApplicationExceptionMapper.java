package com.smartcampus.exception;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Handles common JAX-RS web exceptions and returns them in a consistent JSON format
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        // Getting the HTTP status code from the original exception response
        int status = exception.getResponse().getStatus();

        String error;
        String message;

        // Setting clearer API-friendly messages for common HTTP errors
        switch (status) {
            case 404:
                error = "Not Found";
                message = "The requested resource was not found.";
                break;

            case 405:
                error = "Method Not Allowed";
                message = "The HTTP method is not allowed for this resource.";
                break;

            case 415:
                error = "Unsupported Media Type";
                message = "This endpoint only accepts application/json.";
                break;

            // Using a more general fallback for other web application errors
            default:
                Response.Status statusInfo = Response.Status.fromStatusCode(status);
                error = (statusInfo != null) ? statusInfo.getReasonPhrase() : "HTTP Error";
                message = (exception.getMessage() != null && !exception.getMessage().isBlank())
                        ? exception.getMessage()
                        : "A request error occurred.";
                break;
        }

        // Building a standard JSON error body for the client
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        // Returning the error response with the original HTTP status code
        return Response.status(status)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}