package org.cabs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cabs.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import(SecurityConfig.class)
@TestPropertySource(properties = "server.servlet.context-path=")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPermitConfiguredGatewayRoutes() throws Exception {
        mockMvc.perform(get("/api-gateway/cabs/test")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api-gateway/appt/test")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api-gateway/queue/test")).andExpect(status().isNotFound());
    }

    @Test
    void shouldRequireAuthenticationForOtherRoutes() throws Exception {
        mockMvc.perform(get("/secured")).andExpect(result -> {
            int status = result.getResponse().getStatus();
            assertTrue(
                status == 401 || status == 302 || status == 403,
                "Expected security to block anonymous access before MVC, but got status " + status
            );
        });
    }

    @Test
    @WithMockUser(username = "tester")
    void shouldAllowAuthenticatedAccessToOtherRoutes() throws Exception {
        mockMvc.perform(get("/secured")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "tester")
    void shouldAllowPostWithoutCsrfBecauseCsrfIsDisabled() throws Exception {
        mockMvc.perform(post("/secured-post")).andExpect(status().isNotFound());
    }
}
