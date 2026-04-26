package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.cabs.service.EmailAlreadyUsedException;
import org.cabs.service.InvalidPasswordException;
import org.cabs.service.UsernameAlreadyUsedException;
import org.cabs.web.rest.errors.BadRequestAlertException;
import org.cabs.web.rest.errors.ErrorConstants;
import org.cabs.web.rest.errors.ExceptionTranslator;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import tech.jhipster.config.JHipsterConstants;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause;

class ExceptionTranslatorTest {

    @Test
    void handleAnyExceptionShouldTranslateBadRequestAlertException() {
        MockEnvironment env = new MockEnvironment();
        TestableExceptionTranslator translator = new TestableExceptionTranslator(env);
        org.springframework.test.util.ReflectionTestUtils.setField(translator, "applicationName", "cabsApp");
        NativeWebRequest request = buildRequest("/api/test");

        ResponseEntity<Object> response = translator.handleAnyException(
            new BadRequestAlertException("bad request", "appointment", "idinvalid"),
            request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ProblemDetailWithCause body = (ProblemDetailWithCause) response.getBody();
        assertEquals("error.idinvalid", body.getProperties().get("message"));
        assertEquals(URI.create("/api/test"), body.getProperties().get("path"));
        assertNotNull(response.getHeaders().getFirst("X-cabsApp-error"));
    }

    @Test
    void handleAnyExceptionShouldMapServiceExceptionsToRestErrors() {
        TestableExceptionTranslator translator = new TestableExceptionTranslator(new MockEnvironment());

        ResponseEntity<Object> loginResponse = translator.handleAnyException(new UsernameAlreadyUsedException(), buildRequest("/api/test"));
        ResponseEntity<Object> emailResponse = translator.handleAnyException(new EmailAlreadyUsedException(), buildRequest("/api/test"));
        ResponseEntity<Object> passwordResponse = translator.handleAnyException(new InvalidPasswordException(), buildRequest("/api/test"));

        assertEquals(HttpStatus.BAD_REQUEST, loginResponse.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, emailResponse.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, passwordResponse.getStatusCode());
    }

    @Test
    void customizeProblemShouldMapStatusesAndMessages() {
        MockEnvironment env = new MockEnvironment();
        TestableExceptionTranslator translator = new TestableExceptionTranslator(env);

        ProblemDetailWithCause accessDenied = translator.callWrapAndCustomizeProblem(new AccessDeniedException("denied"), buildRequest("/api/access"));
        ProblemDetailWithCause conflict = translator.callWrapAndCustomizeProblem(new ConcurrencyFailureException("conflict"), buildRequest("/api/conflict"));
        ProblemDetailWithCause unauthorized = translator.callWrapAndCustomizeProblem(new BadCredentialsException("bad creds"), buildRequest("/api/auth"));

        assertEquals(HttpStatus.FORBIDDEN.value(), accessDenied.getStatus());
        assertEquals(HttpStatus.CONFLICT.value(), conflict.getStatus());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), unauthorized.getStatus());
        assertEquals(ErrorConstants.ERR_CONCURRENCY_FAILURE, conflict.getProperties().get("message"));
    }

    @Test
    void customizeProblemShouldUseProductionSafeDetails() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION);
        TestableExceptionTranslator translator = new TestableExceptionTranslator(env);

        ProblemDetailWithCause httpMessage = translator.callWrapAndCustomizeProblem(
            new HttpMessageConversionException("bad conversion"),
            buildRequest("/api/http")
        );
        ProblemDetailWithCause packageMessage = translator.callWrapAndCustomizeProblem(
            new RuntimeException("java.lang.IllegalStateException: boom"),
            buildRequest("/api/runtime")
        );

        assertEquals("Unable to convert http message", httpMessage.getDetail());
        assertEquals("Unexpected runtime exception", packageMessage.getDetail());
    }

    @Test
    void customizeProblemShouldUseCauseMessageOutsideProduction() {
        MockEnvironment env = new MockEnvironment();
        TestableExceptionTranslator translator = new TestableExceptionTranslator(env);

        RuntimeException exception = new RuntimeException("outer", new IllegalStateException("inner"));
        ProblemDetailWithCause problem = translator.callWrapAndCustomizeProblem(exception, buildRequest("/api/runtime"));

        assertEquals("inner", problem.getDetail());
    }

    @Test
    void buildCauseShouldReturnEmptyWhenCasualChainDisabled() {
        TestableExceptionTranslator translator = new TestableExceptionTranslator(new MockEnvironment());
        assertTrue(translator.buildCause(new RuntimeException("boom"), buildRequest("/api/test")).isEmpty());
    }

    @Test
    void pathShouldFallbackToAboutBlankWhenRequestMissing() {
        TestableExceptionTranslator translator = new TestableExceptionTranslator(new MockEnvironment());
        ProblemDetailWithCause problem = translator.callWrapAndCustomizeProblem(new RuntimeException("boom"), null);
        assertEquals(URI.create("about:blank"), problem.getProperties().get("path"));
    }

    @Test
    void handleExceptionInternalShouldWrapNullBody() {
        TestableExceptionTranslator translator = new TestableExceptionTranslator(new MockEnvironment());
        ResponseEntity<Object> response = translator.callHandleExceptionInternal(
            new RuntimeException("boom"),
            null,
            null,
            HttpStatus.INTERNAL_SERVER_ERROR,
            (org.springframework.web.context.request.WebRequest) buildRequest("/api/internal")
        );
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void containsPackageStyleMessageShouldBeHandledInProduction() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_PRODUCTION);
        TestableExceptionTranslator translator = new TestableExceptionTranslator(env);

        ProblemDetailWithCause problem = translator.callWrapAndCustomizeProblem(
            new ConversionFailedException(TypeDescriptor.valueOf(String.class), TypeDescriptor.valueOf(Integer.class), "x", new RuntimeException("com.example.Boom")),
            buildRequest("/api/convert")
        );

        assertFalse(problem.getDetail().isBlank());
    }

    private NativeWebRequest buildRequest(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return new ServletWebRequest(request, new MockHttpServletResponse());
    }

    private static class TestableExceptionTranslator extends ExceptionTranslator {

        TestableExceptionTranslator(MockEnvironment env) {
            super(env);
        }

        ProblemDetailWithCause callWrapAndCustomizeProblem(Throwable ex, NativeWebRequest request) {
            return super.wrapAndCustomizeProblem(ex, request);
        }

        ResponseEntity<Object> callHandleExceptionInternal(
            Exception ex,
            Object body,
            org.springframework.http.HttpHeaders headers,
            org.springframework.http.HttpStatusCode statusCode,
            org.springframework.web.context.request.WebRequest request
        ) {
            return super.handleExceptionInternal(ex, body, headers, statusCode, request);
        }
    }
}
