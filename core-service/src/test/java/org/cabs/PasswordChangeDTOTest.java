package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.cabs.service.dto.PasswordChangeDTO;
import org.junit.jupiter.api.Test;

class PasswordChangeDTOTest {

    @Test
    void constructorsAndSettersShouldWork() {
        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setCurrentPassword("old");
        dto.setNewPassword("new");
        assertEquals("old", dto.getCurrentPassword());
        assertEquals("new", dto.getNewPassword());

        PasswordChangeDTO constructed = new PasswordChangeDTO("one", "two");
        assertNotNull(constructed);
        assertEquals("one", constructed.getCurrentPassword());
        assertEquals("two", constructed.getNewPassword());
    }
}
