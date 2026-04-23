package com.smartcampus.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.smartcampus.exception.GlobalExceptionMapper;
import com.smartcampus.exception.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.exception.RoomNotEmptyExceptionMapper;
import com.smartcampus.exception.SensorUnavailableExceptionMapper;
import com.smartcampus.exception.WebApplicationExceptionMapper;
import com.smartcampus.filter.LoggingFilter;
import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import com.smartcampus.resource.TestErrorResource;

// Defines the versioned base path for the whole API
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        // Creating a set to register all classes used by the JAX-RS application
        Set<Class<?>> classes = new HashSet<>();

        // Registering the main resource classes that expose the API endpoints
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);
        classes.add(TestErrorResource.class);

        // Registering custom exception mappers so errors return proper JSON responses
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(WebApplicationExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);

        // Registering the logging filter to track incoming requests and outgoing responses
        classes.add(LoggingFilter.class);

        // Returning all registered classes to the JAX-RS runtime
        return classes;
    }
}