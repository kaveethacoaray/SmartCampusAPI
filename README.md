# SmartCampusAPI
# Smart Campus Sensor & Room Management API

Module: 5COSC022W — Client-Server Architectures  
Student Name: B.K.D.Cooray 
Student ID: w2120640/20231311

## Overview

This is a JAX-RS RESTful API developed on the University of Westminster Smart Campus scenario. It handles rooms, the sensors attached to the rooms and the historical readings of each sensor.

Jersey 2.41 and an embedded Grizzly HTTP server are used to build the API. No database is utilized. A shared singleton `DataStore` with `ConcurrentHashMap` and synchronized lists is used to store all the data in memory.

Base URL: `http://localhost:8080/api/v1`

**Tech stack**
- Java 11
- JAX-RS (Jersey 2.41)
- Grizzly HTTP server
- Jackson for JSON
- Maven
- In-memory storage only

## Build and Run

**Requirements**
- Java JDK 11 or higher
- Apache Maven 3.6 or higher

**Check installed versions**
```bash
java -version
mvn -version
```

**Step 1 — Clone the repository**
```bash
git clone https://github.com/kaveethacoaray/SmartCampusAPI.git
cd SmartCampusAPI
```

**Step 2 — Build the project**
```bash
mvn clean package
```

Wait for `BUILD SUCCESS`.

**Step 3 — Run the server**
```bash
java -jar target/smart-campus-api-1.0.0.jar
```

The server starts on port `8080`.

**Step 4 — Verify**
Open this URL in a browser or Postman:
```bash
http://localhost:8080/api/v1
```

You should get the discovery JSON response.

**Preloaded sample data**
- Rooms: `LIB-301`, `LAB-101`, `HALL-A`
- Sensors: `TEMP-001`, `CO2-001`, `OCC-001`, `TEMP-002`

This allows the API to be tested immediately after startup.

## API Structure

 Path - what its is  
 `GET /api/v1` - Discovery endpoint with API metadata and links 
 `/api/v1/rooms` - Room collection — list, create, get by ID, delete 
 `/api/v1/sensors` - Sensor collection — list, create, get by ID, delete 
 `/api/v1/sensors/{sensorId}/readings` - Sensor reading sub-resource — get history and add new readings 

## Sample curl Commands

All commands assume the server is running on `http://localhost:8080`.

**1. Discovery endpoint — GET /api/v1**
```bash
curl -s -X GET http://localhost:8080/api/v1 \
     -H "Accept: application/json" | python3 -m json.tool
```

**2. Get all rooms — GET /api/v1/rooms**
```bash
curl -s -X GET http://localhost:8080/api/v1/rooms \
     -H "Accept: application/json" | python3 -m json.tool
```

**3. Create a new room — POST /api/v1/rooms**
```bash
curl -i -X POST http://localhost:8080/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"DEMO-900","name":"Demo Room","capacity":25}'
```

**4. Get a specific room — GET /api/v1/rooms/DEMO-900**
```bash
curl -s -X GET http://localhost:8080/api/v1/rooms/DEMO-900 \
     -H "Accept: application/json" | python3 -m json.tool
```

**5. Create a valid sensor — POST /api/v1/sensors**
```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"HUM-900","type":"Humidity","status":"ACTIVE","currentValue":55.5,"roomId":"DEMO-900"}'
```

**6. Filter sensors by type — GET /api/v1/sensors?type=CO2**
```bash
curl -s -X GET "http://localhost:8080/api/v1/sensors?type=CO2" \
     -H "Accept: application/json" | python3 -m json.tool
```

**7. Get sensor reading history — GET /api/v1/sensors/TEMP-001/readings**
```bash
curl -s -X GET http://localhost:8080/api/v1/sensors/TEMP-001/readings \
     -H "Accept: application/json" | python3 -m json.tool
```

**8. Add a new reading — POST /api/v1/sensors/TEMP-001/readings**
```bash
curl -s -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":24.8}' | python3 -m json.tool
```

## Report: Answers to Coursework Questions

### Part 1 — Setup & Discovery

#### 1.1 JAX-RS resource lifecycle and in-memory data management

JAX-RS by default will create a new resource object with every incoming HTTP request. It is a request-scoped lifecycle. Due to that, resource classes are to remain stateless, as any instance field within the resource object would only be present in that particular request and would be destroyed afterwards.

That is important to in-memory storage. Having rooms and sensors as normal instance fields in RoomResource or SensorResource would not allow the data to be stored between requests. A POST request may append data to a single object instance, but the subsequent GET request would be using a different resource object and would not reflect that state.

The API solves this by using a singleton DataStore. All the resources classes invoke DataStore.getInstance, and therefore all requests share common in-memory data.

To achieve concurrency, the DataStore is based on ConcurrentHashMap of rooms and sensors, and synchronized lists of sensor readings. This minimizes the chances of race conditions and data corruption in case of multiple requests being processed simultaneously.

#### 1.2 HATEOAS and the value of hypermedia

HATEOAS implies that an API response contains links to other related resources to enable the client to learn how to navigate the API at runtime.

In this project, the discovery endpoint at GET /api/v1 will provide metadata and links to the primary collections of /api/v1/rooms and /api/v1/sensors.

This is handy since the API is self-descriptive. A client is able to begin with a single known entry point and explore the resources available without relying solely on individual documentation. In case the API is discovered later, the discovery response can be updated as well, making clients easier to adapt.

Hypermedia simplifies the API exploration process and minimizes the risk of clients hard-coding all paths compared to the situation with only static documentation.

### Part 2 — Room Management

#### 2.1 Returning IDs vs full objects in a collection response

In the case of returning a collection like GET /api/v1/rooms, two primary choices exist: to only return room IDs, or to return complete room objects.

Sending back only IDs reduces the size of the response, and conserves bandwidth. But it also implies that the client will need to make additional requests to get the actual information about each room. When the number of rooms is large, it will multiply the number of HTTP requests and introduce additional delay.

Full room objects are larger to respond to, but the client receives all useful data in a single request. In this project, that is the more appropriate option since it simplifies the API and eliminates the need to make follow-up calls that are not necessary.

This API provides complete room objects.

#### 2.2 Is DELETE idempotent in this implementation?

Yes, the DELETE operation is idempotent here.

In case a client attempts to delete a room with sensors assigned, the API will respond with 409 Conflict. When the same request is re-sent without altering the room state, it will still respond with 409 Conflict. The server state remains the same and hence the repeated request is idempotent.

When a room is deleted successfully and there are no assigned sensors, the initial request will respond with 200 OK. When the same DELETE request is repeated later, the room does not exist anymore, and the second response is 404 Not Found. Even then, the end state of the server remains unchanged with repeated requests: the room is lost. That remains idempotent.

In this implementation, deletion is prevented and sensors are still allocated to the room. This more restrictive rule eliminates orphaned sensor references in the in-memory store.

### Part 3 — Sensor Operations & Filtering

#### 3.1 What happens if the client sends the wrong Content-Type?

The POST techniques of this API are based on @Consumes(MediaType).APPLICATION_JSON). This implies that the endpoint will only accept request bodies that are in the form of JSON.

When a client submits the body with a different Content-Type, like text/plain or application/xml, Jersey will reject the request before it is sent to the resource method. The client is sent HTTP 415 Unsupported Media Type.

The JAX-RS runtime itself deals with this behaviour. This does not have to be manually checked in every method of the application code.

#### 3.2 Why use @QueryParam instead of a path segment for filtering?

A query parameter is more appropriate to filter a collection than to place the filter within the path.

For example:
GET /api/v1/sensors?type=CO2
GET /api/v1/sensors/type/CO2

The former is superior since the sensors collection remains the primary resource, and type=CO2 is merely a filter on the collection. This is closer to REST design.

The query parameters are also more easily extended. In case additional filters were required in the future, they could be added automatically, e.g.:
GET /api/v1/sensors?type=CO2&status=ACTIVE

This is why this API relies on the filtering with the help of the @QueryParam(type).


### Part 4 — Sub-Resources

The Sub-Resource Locator pattern is used to locate a resource within a sub-resource.

A sub-resource locator is a technique with a @Path annotation but no HTTP method annotation like an annotation of type @GET or an annotation of type @POST. It has the task of returning another resource object which takes care of the remaining part of the request path.

The sub-resource locator of: is in SensorResource in this API.
```text
/sensors/{sensorId}/readings
```

The locator will give a sensor reading resource object and that class manages:
- GET to read history.
- POST to add a new reading.

This assists in maintaining the code in order. In the absence of a sub-resource locator, all sensor logic and all reading logic would need to remain within a single large resource class. Separating them, each class has a more definite responsibility and is simpler to maintain and test.


### Part 5 — Error Handling & Logging

#### 5.1 Why is HTTP 422 more accurate than 404 here?

In case a client makes a valid JSON request to POST /api/v1/sensors but the roomId within the body is not found, the endpoint remains valid. That would be a false alarm, since the URL is not the issue.

The issue is that the request body includes a reference to another resource which is not resolvable. The JSON is syntactically correct, but semantically incorrect.

This is why it is more appropriate to use HTTP 422 Unprocessable Entity. It informs the client that the request format has been received, but the request is not possible due to invalid data within the payload.

#### 5.2 Security risks of exposing Java stack traces

It is a security risk to expose raw Java stack traces in API responses.

A stack trace may indicate:
- names of frameworks and libraries.
- internal package and class names.
- names of methods and line numbers.
- file paths and deployment details.
- clues of poor validation or poor code paths

That information could be used by an attacker to understand the way the application is constructed and to seek more specific attacks.

To prevent this, the API relies on GlobalExceptionMapper that captures unexpected exceptions and sends a clean JSON 500 Internal Server Error response without revealing internal information to the client.

#### 5.3 Why use filters for logging?

Logging is a cross-cutting issue since it is not limited to a single endpoint but to all requests and responses.

In the case of logging being done manually within each resource method, the code would be duplicated numerous times. That complicates the code to maintain and forget when new endpoints are introduced.

In this project, a ContainerRequestFilter and ContainerResponseFilter are used instead. It implies that logging is done at a single location and all requests and responses are logged uniformly.

This maintains the resource classes business logic-oriented and the filter addresses the shared infrastructure issue.

# SmartCampusAPI

