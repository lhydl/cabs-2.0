package org.cabs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import tech.jhipster.config.JHipsterConstants;

class CabsAppTest {

    @Test
    void initApplicationShouldHandleDevAndProdProfiles() {
        Environment env = new MockEnvironment().withProperty("spring.profiles.active", "dev,prod");
        CabsApp app = new CabsApp(env);
        assertDoesNotThrow(app::initApplication);
    }

    @Test
    void initApplicationShouldHandleDevAndCloudProfiles() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT, JHipsterConstants.SPRING_PROFILE_CLOUD);
        CabsApp app = new CabsApp(env);
        assertDoesNotThrow(app::initApplication);
    }

    @Test
    void initApplicationShouldHandleNormalProfiles() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("local");
        CabsApp app = new CabsApp(env);
        assertDoesNotThrow(app::initApplication);
    }
}
