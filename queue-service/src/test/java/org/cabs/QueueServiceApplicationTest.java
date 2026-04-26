package org.cabs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

class QueueServiceApplicationTest {

    @Test
    void configureShouldRegisterApplicationSource() {
        QueueServiceApplication application = new QueueServiceApplication();
        SpringApplicationBuilder inputBuilder = new SpringApplicationBuilder();

        SpringApplicationBuilder builder = application.configure(inputBuilder);

        assertNotNull(builder.application());
        assertSame(inputBuilder, builder);
    }
}
