package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cabs.web.rest.vm.KeyAndPasswordVM;
import org.junit.jupiter.api.Test;

class KeyAndPasswordVMTest {

    @Test
    void gettersAndSettersShouldWork() {
        KeyAndPasswordVM vm = new KeyAndPasswordVM();
        vm.setKey("key");
        vm.setNewPassword("password");

        assertEquals("key", vm.getKey());
        assertEquals("password", vm.getNewPassword());
    }
}
