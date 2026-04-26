package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.cabs.utils.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserLoginShouldReturnEmptyWhenNoAuthentication() {
        assertTrue(SecurityUtils.getCurrentUserLogin().isEmpty());
    }

    @Test
    void getCurrentUserLoginShouldReturnUsernameFromUserDetails() {
        User principal = new User("alice", "pw", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())
        );

        assertEquals("alice", SecurityUtils.getCurrentUserLogin().orElseThrow());
    }

    @Test
    void getCurrentUserLoginShouldReturnPrincipalString() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("bob", null, Collections.emptyList())
        );

        assertEquals("bob", SecurityUtils.getCurrentUserLogin().orElseThrow());
    }

    @Test
    void getCurrentUserLoginShouldReturnEmptyForUnsupportedPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(123, null, Collections.emptyList())
        );

        assertTrue(SecurityUtils.getCurrentUserLogin().isEmpty());
    }
}
