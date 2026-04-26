package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cabs.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

class GlobalExceptionHandlerTest {

    @Test
    void handleServiceDownShouldReturnServiceUnavailable() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<String> response = handler.handleServiceDown(new ResourceAccessException("down"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Service is unavailable", response.getBody());
    }
}
