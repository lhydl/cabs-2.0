package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.cabs.domain.User;
import org.cabs.service.dto.UserDTO;
import org.junit.jupiter.api.Test;

class UserDTOTest {

    @Test
    void defaultConstructorShouldCreateDto() {
        assertNotNull(new UserDTO());
    }

    @Test
    void constructorShouldMapUser() {
        User user = new User();
        user.setId(4L);
        user.setLogin("alice");

        UserDTO dto = new UserDTO(user);

        assertEquals(4L, dto.getId());
        assertEquals("alice", dto.getLogin());
        assertTrue(dto.toString().contains("alice"));
    }
}
