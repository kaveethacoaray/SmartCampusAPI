package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/test-error")
public class TestErrorResource {

    // Creating a test endpoint used only to deliberately trigger a server-side error
    @GET
    public String triggerError() {
        // Throwing a runtime exception so the API returns a 500 Internal Server Error for testing
        throw new RuntimeException("Demo test error");
    }
}