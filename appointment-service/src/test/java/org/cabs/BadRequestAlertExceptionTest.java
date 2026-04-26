package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.cabs.exception.BadRequestAlertException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class BadRequestAlertExceptionTest {

    @Test
    void constructorShouldPopulateDefaultProblemDetail() {
        BadRequestAlertException exception =
            new BadRequestAlertException("Bad request message", "appointment", "idinvalid");

        assertEquals("appointment", exception.getEntityName());
        assertEquals("idinvalid", exception.getErrorKey());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Bad request message", exception.getBody().getTitle());
        assertEquals("error.idinvalid", exception.getBody().getProperties().get("message"));
        assertEquals("appointment", exception.getBody().getProperties().get("params"));
    }

    @Test
    void constructorShouldRespectCustomType() {
        URI type = URI.create("https://example.com/custom");
        BadRequestAlertException exception =
            new BadRequestAlertException(type, "Custom message", "appointment", "idexists");

        assertEquals(type, exception.getBody().getType());
        assertEquals("Custom message", exception.getBody().getTitle());
    }
}
