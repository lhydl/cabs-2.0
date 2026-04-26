package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import org.cabs.domain.Authority;
import org.cabs.domain.User;
import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    void setLoginShouldLowercaseValue() {
        User user = new User();
        user.setLogin("Jane.DOE");
        assertEquals("jane.doe", user.getLogin());
    }

    @Test
    void equalsShouldCoverBranches() {
        User left = new User();
        left.setId(1L);
        User same = new User();
        same.setId(1L);
        User other = new User();
        other.setId(2L);

        assertEquals(left, left);
        assertEquals(left, same);
        assertNotEquals(left, other);
        assertNotEquals(new User(), new User());
        assertNotEquals(left, "user");
        assertEquals(left.hashCode(), left.getClass().hashCode());
    }

    @Test
    void toStringShouldIncludeMainFields() {
        User user = new User();
        user.setId(3L);
        user.setLogin("alice");
        user.setFirstName("Alice");
        user.setLastName("Tan");
        user.setEmail("alice@example.com");
        user.setImageUrl("image");
        user.setActivated(true);
        user.setLangKey("en");
        user.setActivationKey("key");
        user.setPhoneNumber("1234");
        user.setDob(new Date(0));
        user.setGender("F");
        Authority authority = new Authority();
        authority.setName("ROLE_USER");
        user.getAuthorities().add(authority);

        String result = user.toString();
        assertTrue(result.contains("alice"));
        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("alice@example.com"));
        assertTrue(result.contains("activated='true'"));
    }
}
