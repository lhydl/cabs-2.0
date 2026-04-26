package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.cabs.web.rest.vm.LoginVM;
import org.junit.jupiter.api.Test;

class LoginVMTest {

    @Test
    void gettersSettersAndToStringShouldWork() {
        LoginVM vm = new LoginVM();
        vm.setUsername("alice");
        vm.setPassword("secret");
        vm.setRememberMe(true);

        assertEquals("alice", vm.getUsername());
        assertEquals("secret", vm.getPassword());
        assertTrue(vm.isRememberMe());
        assertTrue(vm.toString().contains("alice"));
    }
}
