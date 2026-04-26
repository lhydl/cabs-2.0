package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.cabs.web.rest.vm.ManagedUserVM;
import org.junit.jupiter.api.Test;

class ManagedUserVMTest {

    @Test
    void gettersSettersAndToStringShouldWork() {
        ManagedUserVM vm = new ManagedUserVM();
        vm.setLogin("alice");
        vm.setPassword("secret");

        assertNotNull(vm);
        assertEquals("secret", vm.getPassword());
        assertTrue(vm.toString().contains("ManagedUserVM"));
    }
}
