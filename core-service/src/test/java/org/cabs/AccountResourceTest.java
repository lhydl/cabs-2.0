package org.cabs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import org.cabs.domain.User;
import org.cabs.repository.UserRepository;
import org.cabs.security.SecurityUtils;
import org.cabs.service.MailService;
import org.cabs.service.UserService;
import org.cabs.service.dto.AdminUserDTO;
import org.cabs.service.dto.PasswordChangeDTO;
import org.cabs.web.rest.AccountResource;
import org.cabs.web.rest.errors.EmailAlreadyUsedException;
import org.cabs.web.rest.errors.InvalidPasswordException;
import org.cabs.web.rest.vm.KeyAndPasswordVM;
import org.cabs.web.rest.vm.ManagedUserVM;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AccountResourceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private AccountResource accountResource;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registerAccountShouldRejectInvalidPassword() {
        ManagedUserVM vm = new ManagedUserVM();
        vm.setPassword("123");

        assertThrows(InvalidPasswordException.class, () -> accountResource.registerAccount(vm));
    }

    @Test
    void registerAccountShouldDelegateForValidPassword() {
        ManagedUserVM vm = new ManagedUserVM();
        vm.setLogin("alice");
        vm.setPassword("1234");

        assertDoesNotThrow(() -> accountResource.registerAccount(vm));
        verify(userService).registerUser(vm, "1234");
    }

    @Test
    void activateAccountShouldThrowWhenUserMissing() {
        when(userService.activateRegistration("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> accountResource.activateAccount("missing"));
    }

    @Test
    void activateAccountShouldSucceedWhenUserFound() {
        when(userService.activateRegistration("key")).thenReturn(Optional.of(new User()));
        assertDoesNotThrow(() -> accountResource.activateAccount("key"));
    }

    @Test
    void getAccountShouldReturnMappedUser() {
        User user = new User();
        user.setLogin("alice");
        when(userService.getUserWithAuthorities()).thenReturn(Optional.of(user));

        AdminUserDTO result = accountResource.getAccount();

        assertEquals("alice", result.getLogin());
    }

    @Test
    void getAccountShouldThrowWhenMissing() {
        when(userService.getUserWithAuthorities()).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, accountResource::getAccount);
    }

    @Test
    void saveAccountShouldThrowWhenNoCurrentLogin() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setEmail("alice@example.com");

        assertThrows(RuntimeException.class, () -> accountResource.saveAccount(dto));
    }

    @Test
    void saveAccountShouldThrowWhenEmailBelongsToOtherUser() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, Collections.emptyList())
        );
        AdminUserDTO dto = new AdminUserDTO();
        dto.setEmail("taken@example.com");
        User existing = new User();
        existing.setLogin("bob");
        when(userRepository.findOneByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.of(existing));

        assertThrows(EmailAlreadyUsedException.class, () -> accountResource.saveAccount(dto));
    }

    @Test
    void saveAccountShouldThrowWhenCurrentUserMissing() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, Collections.emptyList())
        );
        AdminUserDTO dto = new AdminUserDTO();
        dto.setEmail("alice@example.com");
        when(userRepository.findOneByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> accountResource.saveAccount(dto));
    }

    @Test
    void saveAccountShouldUpdateWhenValid() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, Collections.emptyList())
        );
        AdminUserDTO dto = new AdminUserDTO();
        dto.setFirstName("Alice");
        dto.setLastName("Tan");
        dto.setEmail("alice@example.com");
        dto.setLangKey("en");
        dto.setImageUrl("img");
        dto.setPhoneNumber("123");
        dto.setDob(new Date());
        dto.setGender("F");
        User current = new User();
        current.setLogin("alice");
        when(userRepository.findOneByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(current));
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.of(current));

        assertDoesNotThrow(() -> accountResource.saveAccount(dto));
        verify(userService).updateUser(
            eq("Alice"), eq("Tan"), eq("alice@example.com"), eq("en"), eq("img"), eq("123"), any(Date.class), eq("F")
        );
    }

    @Test
    void changePasswordShouldRejectInvalidNewPassword() {
        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setNewPassword("123");

        assertThrows(InvalidPasswordException.class, () -> accountResource.changePassword(dto));
    }

    @Test
    void changePasswordShouldDelegateWhenValid() {
        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setCurrentPassword("oldpass");
        dto.setNewPassword("newpass");

        assertDoesNotThrow(() -> accountResource.changePassword(dto));
        verify(userService).changePassword("oldpass", "newpass");
    }

    @Test
    void requestPasswordResetShouldSendEmailWhenUserFound() {
        User user = new User();
        when(userService.requestPasswordReset("alice@example.com")).thenReturn(Optional.of(user));

        accountResource.requestPasswordReset("alice@example.com");

        verify(mailService).sendPasswordResetMail(user);
    }

    @Test
    void requestPasswordResetShouldNotSendEmailWhenUserMissing() {
        when(userService.requestPasswordReset("missing@example.com")).thenReturn(Optional.empty());

        accountResource.requestPasswordReset("missing@example.com");

        verify(mailService, never()).sendPasswordResetMail(any());
    }

    @Test
    void finishPasswordResetShouldRejectInvalidPassword() {
        KeyAndPasswordVM vm = new KeyAndPasswordVM();
        vm.setNewPassword("123");

        assertThrows(InvalidPasswordException.class, () -> accountResource.finishPasswordReset(vm));
    }

    @Test
    void finishPasswordResetShouldThrowWhenUserMissing() {
        KeyAndPasswordVM vm = new KeyAndPasswordVM();
        vm.setKey("k");
        vm.setNewPassword("1234");
        when(userService.completePasswordReset("1234", "k")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> accountResource.finishPasswordReset(vm));
    }

    @Test
    void finishPasswordResetShouldSucceedWhenUserFound() {
        KeyAndPasswordVM vm = new KeyAndPasswordVM();
        vm.setKey("k");
        vm.setNewPassword("1234");
        when(userService.completePasswordReset("1234", "k")).thenReturn(Optional.of(new User()));

        assertDoesNotThrow(() -> accountResource.finishPasswordReset(vm));
    }
}
