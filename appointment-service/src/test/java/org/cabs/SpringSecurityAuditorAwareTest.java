package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import org.cabs.configuration.SpringSecurityAuditorAware;
import org.cabs.constants.Constants;
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
    void getCurrentAuditorShouldReturnSystemWhenNoUserAuthenticated() {
        SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware();

        assertEquals(Constants.SYSTEM, auditorAware.getCurrentAuditor().orElseThrow());
    }

    @Test
    void getCurrentAuditorShouldReturnAuthenticatedUsername() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, Collections.emptyList())
        );

        SpringSecurityAuditorAware auditorAware = new SpringSecurityAuditorAware();

        assertEquals("alice", auditorAware.getCurrentAuditor().orElseThrow());
    }
}
