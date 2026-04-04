package org.cabs.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@SuppressWarnings("java:S110")
public class BadRequestAlertException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    private final String entityName;
    private final String errorKey;

    public BadRequestAlertException(String defaultMessage, String entityName, String errorKey) {
        this(URI.create("https://example.com/problem/bad-request"), defaultMessage, entityName,
            errorKey);
    }

    public BadRequestAlertException(URI type, String defaultMessage, String entityName,
        String errorKey) {
        super(HttpStatus.BAD_REQUEST,
            createProblemDetail(type, defaultMessage, entityName, errorKey), null);
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    private static ProblemDetail createProblemDetail(URI type, String defaultMessage,
        String entityName, String errorKey) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(type);
        problemDetail.setTitle(defaultMessage);

        // Custom properties (same as JHipster behavior)
        problemDetail.setProperty("message", "error." + errorKey);
        problemDetail.setProperty("params", entityName);

        return problemDetail;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public ProblemDetail getProblemDetail() {
        return (ProblemDetail) this.getBody();
    }
}