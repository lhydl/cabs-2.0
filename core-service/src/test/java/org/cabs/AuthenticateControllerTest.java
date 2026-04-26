package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.cabs.web.rest.AuthenticateController;
import org.cabs.web.rest.vm.LoginVM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthenticateControllerTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Mock
    private AuthenticationManager authenticationManager;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authorizeShouldAuthenticateAndReturnBearerToken() {
        AuthenticateController controller = new AuthenticateController(jwtEncoder, authenticationManagerBuilder);
        ReflectionTestUtils.setField(controller, "tokenValidityInSeconds", 60L);
        ReflectionTestUtils.setField(controller, "tokenValidityInSecondsForRememberMe", 120L);

        LoginVM loginVM = new LoginVM();
        loginVM.setUsername("alice");
        loginVM.setPassword("password");
        loginVM.setRememberMe(false);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "alice",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        org.springframework.security.oauth2.jwt.Jwt jwt = org.mockito.Mockito.mock(org.springframework.security.oauth2.jwt.Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token-value");

        when(authenticationManagerBuilder.getObject()).thenReturn(authenticationManager);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        ResponseEntity<?> response = controller.authorize(loginVM);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bearer token-value", response.getHeaders().getFirst("Authorization"));
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void createTokenShouldUseRememberMeValidity() {
        AuthenticateController controller = new AuthenticateController(jwtEncoder, authenticationManagerBuilder);
        ReflectionTestUtils.setField(controller, "tokenValidityInSeconds", 60L);
        ReflectionTestUtils.setField(controller, "tokenValidityInSecondsForRememberMe", 120L);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "alice",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        org.springframework.security.oauth2.jwt.Jwt jwt = org.mockito.Mockito.mock(org.springframework.security.oauth2.jwt.Jwt.class);
        when(jwt.getTokenValue()).thenReturn("remember-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = controller.createToken(authentication, true);

        assertEquals("remember-token", token);
    }

    @Test
    void isAuthenticatedShouldReturnRemoteUser() {
        AuthenticateController controller = new AuthenticateController(jwtEncoder, authenticationManagerBuilder);
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        when(request.getRemoteUser()).thenReturn("alice");

        assertEquals("alice", controller.isAuthenticated(request));
    }
}
