package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Optional;
import org.cabs.constants.AuthoritiesConstants;
import org.cabs.dto.AdminUserDTO;
import org.cabs.entity.Authority;
import org.cabs.entity.User;
import org.cabs.exception.EmailAlreadyUsedException;
import org.cabs.exception.UsernameAlreadyUsedException;
import org.cabs.repository.AuthorityRepository;
import org.cabs.repository.UserRepository;
import org.cabs.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorityRepository authorityRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUserShouldCreateActivatedUserWithEncodedPassword() {
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("JaneDoe");
        userDTO.setFirstName("Jane");
        userDTO.setLastName("Doe");
        userDTO.setEmail("JANE@EXAMPLE.COM");
        userDTO.setPhoneNumber("12345678");
        userDTO.setDob(new Date());
        userDTO.setGender("Female");

        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.USER);

        when(userRepository.findOneByLogin("janedoe")).thenReturn(Optional.empty());
        when(userRepository.findOneByEmailIgnoreCase("JANE@EXAMPLE.COM")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(authorityRepository.findById(AuthoritiesConstants.USER)).thenReturn(Optional.of(authority));

        User result = userService.registerUser(userDTO, "secret");

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());

        User persisted = savedUser.getValue();
        assertEquals("janedoe", persisted.getLogin());
        assertEquals("jane@example.com", persisted.getEmail());
        assertEquals("encoded-secret", persisted.getPassword());
        assertTrue(persisted.isActivated());
        assertNotNull(persisted.getActivationKey());
        assertTrue(persisted.getAuthorities().contains(authority));

        assertEquals("janedoe", result.getLogin());
        assertEquals("jane@example.com", result.getEmail());
    }

    @Test
    void registerUserShouldThrowWhenActivatedLoginAlreadyExists() {
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("existing");
        userDTO.setEmail("new@example.com");

        User existingUser = new User();
        existingUser.setActivated(true);
        existingUser.setLogin("existing");

        when(userRepository.findOneByLogin("existing")).thenReturn(Optional.of(existingUser));

        assertThrows(UsernameAlreadyUsedException.class, () -> userService.registerUser(userDTO, "secret"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserShouldThrowWhenActivatedEmailAlreadyExists() {
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("newuser");
        userDTO.setEmail("taken@example.com");

        User existingUser = new User();
        existingUser.setActivated(true);
        existingUser.setEmail("taken@example.com");

        when(userRepository.findOneByLogin("newuser")).thenReturn(Optional.empty());
        when(userRepository.findOneByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyUsedException.class, () -> userService.registerUser(userDTO, "secret"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserShouldReplaceNonActivatedExistingLogin() {
        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("replaceMe");
        userDTO.setEmail("replace@example.com");

        User existingUser = new User();
        existingUser.setActivated(false);
        existingUser.setLogin("replaceme");

        when(userRepository.findOneByLogin("replaceme")).thenReturn(Optional.of(existingUser));
        when(userRepository.findOneByEmailIgnoreCase("replace@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(authorityRepository.findById(AuthoritiesConstants.USER)).thenReturn(Optional.empty());

        userService.registerUser(userDTO, "secret");

        verify(userRepository).delete(existingUser);
        verify(userRepository).flush();
        verify(userRepository).save(any(User.class));
    }
}
