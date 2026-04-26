package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;
import org.cabs.dto.AdminUserDTO;
import org.cabs.entity.Authority;
import org.cabs.entity.User;
import org.junit.jupiter.api.Test;

class AdminUserDTOTest {

    @Test
    void defaultConstructorShouldCreateDto() {
        AdminUserDTO dto = new AdminUserDTO();
        assertNotNull(dto);
    }

    @Test
    void constructorShouldMapFieldsFromUser() {
        User user = new User();
        user.setId(5L);
        user.setLogin("alice");
        user.setFirstName("Alice");
        user.setLastName("Tan");
        user.setEmail("alice@example.com");
        user.setActivated(true);
        user.setImageUrl("img");
        user.setLangKey("en");
        user.setCreatedBy("system");
        user.setCreatedDate(Instant.parse("2026-04-26T00:00:00Z"));
        user.setLastModifiedBy("admin");
        user.setLastModifiedDate(Instant.parse("2026-04-26T01:00:00Z"));
        user.setPhoneNumber("1234");
        user.setDob(new Date(0));
        user.setGender("F");
        Authority authority = new Authority();
        authority.setName("ROLE_USER");
        user.getAuthorities().add(authority);

        AdminUserDTO dto = new AdminUserDTO(user);

        assertEquals(5L, dto.getId());
        assertEquals("alice", dto.getLogin());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Tan", dto.getLastName());
        assertEquals("alice@example.com", dto.getEmail());
        assertTrue(dto.isActivated());
        assertEquals("img", dto.getImageUrl());
        assertEquals("en", dto.getLangKey());
        assertEquals("system", dto.getCreatedBy());
        assertEquals("admin", dto.getLastModifiedBy());
        assertEquals("1234", dto.getPhoneNumber());
        assertEquals("F", dto.getGender());
        assertTrue(dto.getAuthorities().contains("ROLE_USER"));
        assertTrue(dto.toString().contains("alice"));
    }
}
