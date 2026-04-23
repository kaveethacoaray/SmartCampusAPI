package com.smartcampus.filter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

// This filter is used to log all incoming requests and outgoing responses for the API
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Getting the request method and full URI before the request reaches the resource class
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getRequestUri().toString();

        // Logging the incoming request details
        LOGGER.info(String.format("[REQUEST]  --> %s %s", method, uri));
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        // Getting the request details again together with the final response status
        String method     = requestContext.getMethod();
        String uri        = requestContext.getUriInfo().getRequestUri().toString();
        int    statusCode = responseContext.getStatus();

        // Logging the outgoing response details
        LOGGER.info(String.format("[RESPONSE] <-- %s %s | Status: %d", method, uri, statusCode));
    }
}