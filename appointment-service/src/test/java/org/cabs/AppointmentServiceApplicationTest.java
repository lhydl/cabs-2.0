package org.cabs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

class AppointmentServiceApplicationTest {

    @Test
    void configureShouldReturnSameBuilder() {
        AppointmentServiceApplication application = new AppointmentServiceApplication();
        SpringApplicationBuilder inputBuilder = new SpringApplicationBuilder();

        SpringApplicationBuilder builder = application.configure(inputBuilder);

        assertNotNull(builder.application());
        assertSame(inputBuilder, builder);
    }
}
