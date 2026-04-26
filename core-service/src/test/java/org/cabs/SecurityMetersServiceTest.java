package org.cabs;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.cabs.management.SecurityMetersService;
import org.junit.jupiter.api.Test;

class SecurityMetersServiceTest {

    @Test
    void shouldRegisterAndIncrementAllCounters() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SecurityMetersService service = new SecurityMetersService(registry);

        service.trackTokenInvalidSignature();
        service.trackTokenExpired();
        service.trackTokenUnsupported();
        service.trackTokenMalformed();

        assertNotNull(registry.find(SecurityMetersService.INVALID_TOKENS_METER_NAME).tag("cause", "invalid-signature").counter());
        assertNotNull(registry.find(SecurityMetersService.INVALID_TOKENS_METER_NAME).tag("cause", "expired").counter());
        assertNotNull(registry.find(SecurityMetersService.INVALID_TOKENS_METER_NAME).tag("cause", "unsupported").counter());
        assertNotNull(registry.find(SecurityMetersService.INVALID_TOKENS_METER_NAME).tag("cause", "malformed").counter());
    }
}
