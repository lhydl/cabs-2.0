package org.cabs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.cabs.configuration.JwtConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtConfigTest {

    @Test
    void initShouldCreateSecretKeyFromBase64Secret() {
        SecretKey expectedKey = Keys.hmacShaKeyFor("01234567890123456789012345678901".getBytes());
        String base64Secret = Encoders.BASE64.encode(expectedKey.getEncoded());

        JwtConfig jwtConfig = new JwtConfig();
        ReflectionTestUtils.setField(jwtConfig, "jwtSecret", base64Secret);

        jwtConfig.init();

        assertNotNull(jwtConfig.getSecretKey());
        assertArrayEquals(expectedKey.getEncoded(), jwtConfig.getSecretKey().getEncoded());
    }
}
