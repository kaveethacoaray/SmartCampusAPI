package com.smartcampus.exception;

// Used when a room cannot be deleted because it still has sensors assigned to it
public class RoomNotEmptyException extends RuntimeException {

    // Passes the custom error message to the parent RuntimeException class
    public RoomNotEmptyException(String message) {
        super(message);
    }
}