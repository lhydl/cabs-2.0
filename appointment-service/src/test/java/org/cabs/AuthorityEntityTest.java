package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.cabs.entity.Authority;
import org.junit.jupiter.api.Test;

class AuthorityEntityTest {

    @Test
    void equalsShouldCoverBranches() {
        Authority left = new Authority();
        left.setName("ROLE_USER");
        Authority right = new Authority();
        right.setName("ROLE_USER");
        Authority other = new Authority();
        other.setName("ROLE_ADMIN");

        assertEquals(left, left);
        assertEquals(left, right);
        assertNotEquals(left, other);
        assertNotEquals(left, "role");
        assertEquals("ROLE_USER".hashCode(), left.hashCode());
        assertTrue(left.toString().contains("ROLE_USER"));
    }
}
