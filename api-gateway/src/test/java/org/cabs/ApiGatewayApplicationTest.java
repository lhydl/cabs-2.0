package org.cabs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

class ApiGatewayApplicationTest {

    @Test
    void configureShouldReturnSameBuilder() {
        ApiGatewayApplication application = new ApiGatewayApplication();
        SpringApplicationBuilder inputBuilder = new SpringApplicationBuilder();

        SpringApplicationBuilder builder = application.configure(inputBuilder);

        assertNotNull(builder.application());
        assertSame(inputBuilder, builder);
    }

    @Test
    void configureShouldRegisterApplicationClassAsSource() throws Exception {
        ApiGatewayApplication application = new ApiGatewayApplication();
        SpringApplicationBuilder builder = application.configure(new SpringApplicationBuilder());

        Field sourcesField = SpringApplicationBuilder.class.getDeclaredField("sources");
        sourcesField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Set<Object> sources = (Set<Object>) sourcesField.get(builder);

        assertTrue(sources.contains(ApiGatewayApplication.class));
    }
}
