package com.smartcampus.exception;

// Used when a request contains a linked resource ID that does not exist in the system
public class LinkedResourceNotFoundException extends RuntimeException {

    // Passes the custom error message to the parent RuntimeException class
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}