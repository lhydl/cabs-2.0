package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import java.util.Date;
import javax.crypto.SecretKey;
import org.cabs.configuration.JwtConfig;
import org.cabs.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class JwtAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterShouldPopulateSecurityContextForValidJwt() throws Exception {
        SecretKey secretKey = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(buildJwtConfig(secretKey));

        String token = Jwts.builder()
            .subject("alice")
            .claim("auth", "ROLE_USER")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 60_000))
            .signWith(secretKey)
            .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("alice", authentication.getName());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterShouldContinueWithoutAuthenticationWhenHeaderMissing() throws Exception {
        SecretKey secretKey = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(buildJwtConfig(secretKey));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterShouldContinueWithoutAuthenticationWhenHeaderIsNotBearer() throws Exception {
        SecretKey secretKey = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(buildJwtConfig(secretKey));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterShouldAuthenticateWithoutAuthoritiesWhenAuthClaimMissing() throws Exception {
        SecretKey secretKey = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(buildJwtConfig(secretKey));

        String token = Jwts.builder()
            .subject("bob")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 60_000))
            .signWith(secretKey)
            .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("bob", authentication.getName());
        assertTrue(authentication.getAuthorities().isEmpty());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterShouldNotAuthenticateWhenSubjectIsEmpty() throws Exception {
        SecretKey secretKey = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(buildJwtConfig(secretKey));

        String token = Jwts.builder()
            .subject("")
            .claim("auth", "ROLE_USER")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 60_000))
            .signWith(secretKey)
            .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterShouldReturnUnauthorizedForInvalidJwt() throws Exception {
        SecretKey expectedKey = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes());
        SecretKey wrongKey = Keys.hmacShaKeyFor("abcdefghijklmnopqrstuvwxyz123456".getBytes());
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(buildJwtConfig(expectedKey));

        String invalidToken = Jwts.builder()
            .subject("alice")
            .claim("auth", "ROLE_USER")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 60_000))
            .signWith(wrongKey)
            .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + invalidToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(filterChain);
    }

    private JwtConfig buildJwtConfig(SecretKey secretKey) {
        JwtConfig jwtConfig = new JwtConfig();
        ReflectionTestUtils.setField(jwtConfig, "jwtSecret", Encoders.BASE64.encode(secretKey.getEncoded()));
        jwtConfig.init();
        return jwtConfig;
    }
}
