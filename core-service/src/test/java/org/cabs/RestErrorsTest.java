package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import org.cabs.web.rest.errors.BadRequestAlertException;
import org.cabs.web.rest.errors.EmailAlreadyUsedException;
import org.cabs.web.rest.errors.ErrorConstants;
import org.cabs.web.rest.errors.FieldErrorVM;
import org.cabs.web.rest.errors.InvalidPasswordException;
import org.cabs.web.rest.errors.LoginAlreadyUsedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause;

class RestErrorsTest {

    @Test
    void badRequestAlertExceptionShouldExposeProblemDetails() {
        BadRequestAlertException exception = new BadRequestAlertException("bad request", "appointment", "idinvalid");

        assertEquals("appointment", exception.getEntityName());
        assertEquals("idinvalid", exception.getErrorKey());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("bad request", exception.getBody().getTitle());
        assertEquals("error.idinvalid", exception.getBody().getProperties().get("message"));
        assertEquals("appointment", exception.getBody().getProperties().get("params"));
        assertNotNull(exception.getProblemDetailWithCause());
    }

    @Test
    void badRequestAlertExceptionShouldRespectCustomType() {
        URI type = URI.create("https://example.com/problem");
        BadRequestAlertException exception = new BadRequestAlertException(type, "msg", "user", "userexists");

        assertEquals(type, exception.getBody().getType());
    }

    @Test
    void emailAlreadyUsedExceptionShouldUseExpectedMetadata() {
        EmailAlreadyUsedException exception = new EmailAlreadyUsedException();

        assertEquals(ErrorConstants.EMAIL_ALREADY_USED_TYPE, exception.getBody().getType());
        assertEquals("userManagement", exception.getEntityName());
        assertEquals("emailexists", exception.getErrorKey());
    }

    @Test
    void loginAlreadyUsedExceptionShouldUseExpectedMetadata() {
        LoginAlreadyUsedException exception = new LoginAlreadyUsedException();

        assertEquals(ErrorConstants.LOGIN_ALREADY_USED_TYPE, exception.getBody().getType());
        assertEquals("userManagement", exception.getEntityName());
        assertEquals("userexists", exception.getErrorKey());
    }

    @Test
    void invalidPasswordExceptionShouldUseExpectedMetadata() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(ErrorConstants.INVALID_PASSWORD_TYPE, exception.getBody().getType());
        assertEquals("Incorrect password", exception.getBody().getTitle());
    }

    @Test
    void fieldErrorVmShouldExposeRecordFields() {
        FieldErrorVM fieldErrorVM = new FieldErrorVM("User", "email", "must not be blank");

        assertEquals("User", fieldErrorVM.objectName());
        assertEquals("email", fieldErrorVM.field());
        assertEquals("must not be blank", fieldErrorVM.message());
    }

    @Test
    void errorConstantsShouldExposeUris() {
        assertTrue(ErrorConstants.DEFAULT_TYPE.toString().contains("problem-with-message"));
        assertTrue(ErrorConstants.CONSTRAINT_VIOLATION_TYPE.toString().contains("constraint-violation"));
        assertTrue(ErrorConstants.INVALID_PASSWORD_TYPE.toString().contains("invalid-password"));
        assertTrue(ErrorConstants.EMAIL_ALREADY_USED_TYPE.toString().contains("email-already-used"));
        assertTrue(ErrorConstants.LOGIN_ALREADY_USED_TYPE.toString().contains("login-already-used"));
    }
}
