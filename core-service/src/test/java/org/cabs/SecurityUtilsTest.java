package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.cabs.security.AuthoritiesConstants;
import org.cabs.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;

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
    void getCurrentUserLoginShouldSupportUserDetailsPrincipal() {
        User principal = new User("alice", "pw", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, "token", Collections.emptyList())
        );

        assertEquals("alice", SecurityUtils.getCurrentUserLogin().orElseThrow());
    }

    @Test
    void getCurrentUserLoginShouldSupportJwtPrincipal() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS512").subject("bob").build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(jwt, "token", Collections.emptyList())
        );

        assertEquals("bob", SecurityUtils.getCurrentUserLogin().orElseThrow());
    }

    @Test
    void getCurrentUserLoginShouldSupportStringPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("charlie", "token", Collections.emptyList())
        );

        assertEquals("charlie", SecurityUtils.getCurrentUserLogin().orElseThrow());
    }

    @Test
    void getCurrentUserLoginShouldReturnEmptyForUnsupportedPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(123, "token", Collections.emptyList())
        );

        assertTrue(SecurityUtils.getCurrentUserLogin().isEmpty());
    }

    @Test
    void getCurrentUserJwtShouldReturnCredentialWhenString() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", "jwt-token", Collections.emptyList())
        );

        assertEquals("jwt-token", SecurityUtils.getCurrentUserJWT().orElseThrow());
    }

    @Test
    void getCurrentUserJwtShouldReturnEmptyWhenCredentialsNotString() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", 123, Collections.emptyList())
        );

        assertTrue(SecurityUtils.getCurrentUserJWT().isEmpty());
    }

    @Test
    void isAuthenticatedAndAuthorityChecksShouldCoverBranches() {
        assertFalse(SecurityUtils.isAuthenticated());
        assertFalse(SecurityUtils.hasCurrentUserAnyOfAuthorities("ROLE_USER"));
        assertTrue(SecurityUtils.hasCurrentUserNoneOfAuthorities("ROLE_USER"));

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "alice",
                "token",
                List.of(new SimpleGrantedAuthority(AuthoritiesConstants.USER))
            )
        );

        assertTrue(SecurityUtils.isAuthenticated());
        assertTrue(SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.USER));
        assertFalse(SecurityUtils.hasCurrentUserNoneOfAuthorities(AuthoritiesConstants.USER));
        assertTrue(SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.USER));
    }

    @Test
    void isAuthenticatedShouldReturnFalseForAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "anon",
                "token",
                List.of(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS))
            )
        );

        assertFalse(SecurityUtils.isAuthenticated());
    }
}
