package org.cabs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.cabs.configuration.SecurityConfiguration;
import org.cabs.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityConfigurationTest {

    @Test
    void passwordEncoderShouldBeBCryptEncoder() {
        SecurityConfiguration securityConfiguration = new SecurityConfiguration(Mockito.mock(JwtAuthenticationFilter.class));

        PasswordEncoder passwordEncoder = securityConfiguration.passwordEncoder();

        assertNotNull(passwordEncoder);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }
}
