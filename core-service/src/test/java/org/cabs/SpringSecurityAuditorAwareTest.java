package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import org.cabs.config.Constants;
import org.cabs.security.SpringSecurityAuditorAware;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class SpringSecurityAuditorAwareTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditorShouldReturnSystemByDefault() {
        SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware();
        assertEquals(Constants.SYSTEM, auditorAware.getCurrentAuditor().orElseThrow());
    }

    @Test
    void getCurrentAuditorShouldReturnAuthenticatedLogin() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, Collections.emptyList())
        );
        SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware();
        assertEquals("alice", auditorAware.getCurrentAuditor().orElseThrow());
    }
}
