package com.smartcampus.exception;

// Used when a reading cannot be added because the sensor is currently unavailable
public class SensorUnavailableException extends RuntimeException {

    // Passes the custom error message to the parent RuntimeException class
    public SensorUnavailableException(String message) {
        super(message);
    }
}