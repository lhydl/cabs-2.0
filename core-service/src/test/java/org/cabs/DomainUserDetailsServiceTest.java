package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.cabs.domain.Authority;
import org.cabs.domain.User;
import org.cabs.repository.UserRepository;
import org.cabs.security.DomainUserDetailsService;
import org.cabs.security.UserNotActivatedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class DomainUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DomainUserDetailsService domainUserDetailsService;

    @Test
    void loadUserByUsernameShouldLoadByEmailWhenInputIsEmail() {
        User user = createActivatedUser("alice", "alice@example.com", Set.of("ROLE_USER"));
        when(userRepository.findOneWithAuthoritiesByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));

        UserDetails result = domainUserDetailsService.loadUserByUsername("alice@example.com");

        assertEquals("alice", result.getUsername());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsernameShouldThrowWhenEmailNotFound() {
        when(userRepository.findOneWithAuthoritiesByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> domainUserDetailsService.loadUserByUsername("alice@example.com"));
    }

    @Test
    void loadUserByUsernameShouldLoadByLowercaseLoginWhenNotEmail() {
        User user = createActivatedUser("alice", "alice@example.com", Set.of("ROLE_ADMIN"));
        when(userRepository.findOneWithAuthoritiesByLogin("alice")).thenReturn(Optional.of(user));

        UserDetails result = domainUserDetailsService.loadUserByUsername("ALICE");

        assertEquals("alice", result.getUsername());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsernameShouldThrowWhenLoginNotFound() {
        when(userRepository.findOneWithAuthoritiesByLogin("alice")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> domainUserDetailsService.loadUserByUsername("alice"));
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserNotActivated() {
        User user = createActivatedUser("alice", "alice@example.com", Set.of("ROLE_USER"));
        user.setActivated(false);
        when(userRepository.findOneWithAuthoritiesByLogin("alice")).thenReturn(Optional.of(user));

        assertThrows(UserNotActivatedException.class, () -> domainUserDetailsService.loadUserByUsername("alice"));
    }

    private User createActivatedUser(String login, String email, Set<String> authorityNames) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setPassword("pw");
        user.setActivated(true);
        authorityNames.forEach(name -> {
            Authority authority = new Authority();
            authority.setName(name);
            user.getAuthorities().add(authority);
        });
        return user;
    }
}
