package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.cabs.filter.HttpLoggingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

class HttpLoggingFilterTest {

    @Test
    void doFilterInternalShouldWrapRequestAndResponseAndCopyBody() throws ServletException, IOException {
        HttpLoggingFilter filter = new HttpLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api-gateway/cabs/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean sawWrappedRequest = new AtomicBoolean(false);
        AtomicBoolean sawWrappedResponse = new AtomicBoolean(false);

        FilterChain chain = (req, res) -> {
            sawWrappedRequest.set(req instanceof ContentCachingRequestWrapper);
            sawWrappedResponse.set(res instanceof ContentCachingResponseWrapper);
            ((ContentCachingResponseWrapper) res).getWriter().write("ok");
            ((ContentCachingResponseWrapper) res).setStatus(202);
        };

        filter.doFilter(request, response, chain);

        assertTrue(sawWrappedRequest.get());
        assertTrue(sawWrappedResponse.get());
        assertEquals(202, response.getStatus());
        assertEquals("ok", response.getContentAsString());
    }

    @Test
    void doFilterInternalShouldPropagateFilterChainException() {
        HttpLoggingFilter filter = new HttpLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api-gateway/cabs/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            throw new ServletException("boom");
        };

        ServletException exception = org.junit.jupiter.api.Assertions.assertThrows(
            ServletException.class,
            () -> filter.doFilter(request, response, chain)
        );

        assertEquals("boom", exception.getMessage());
    }
}
