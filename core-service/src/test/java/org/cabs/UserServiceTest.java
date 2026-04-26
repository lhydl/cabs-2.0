package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.cabs.domain.Authority;
import org.cabs.domain.User;
import org.cabs.repository.AuthorityRepository;
import org.cabs.repository.UserRepository;
import org.cabs.security.AuthoritiesConstants;
import org.cabs.service.EmailAlreadyUsedException;
import org.cabs.service.InvalidPasswordException;
import org.cabs.service.UserService;
import org.cabs.service.UsernameAlreadyUsedException;
import org.cabs.service.dto.AdminUserDTO;
import org.cabs.service.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache loginCache;

    @Mock
    private Cache emailCache;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void activateRegistrationShouldActivateUserWhenKeyExists() {
        User user = createUser(1L, "alice", "alice@example.com");
        user.setActivationKey("key");
        when(userRepository.findOneByActivationKey("key")).thenReturn(Optional.of(user));
        stubCaches();

        Optional<User> result = userService.activateRegistration("key");

        assertTrue(result.isPresent());
        assertTrue(result.orElseThrow().isActivated());
        assertEquals(null, result.orElseThrow().getActivationKey());
    }

    @Test
    void activateRegistrationShouldReturnEmptyWhenKeyMissing() {
        when(userRepository.findOneByActivationKey("missing")).thenReturn(Optional.empty());
        assertTrue(userService.activateRegistration("missing").isEmpty());
    }

    @Test
    void completePasswordResetShouldResetWhenKeyValidAndNotExpired() {
        User user = createUser(1L, "alice", "alice@example.com");
        user.setResetKey("rk");
        user.setResetDate(Instant.now());
        when(userRepository.findOneByResetKey("rk")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");
        stubCaches();

        Optional<User> result = userService.completePasswordReset("newpass", "rk");

        assertTrue(result.isPresent());
        assertEquals("encoded", user.getPassword());
        assertEquals(null, user.getResetKey());
        assertEquals(null, user.getResetDate());
    }

    @Test
    void completePasswordResetShouldReturnEmptyWhenExpired() {
        User user = createUser(1L, "alice", "alice@example.com");
        user.setResetDate(Instant.now().minus(2, ChronoUnit.DAYS));
        when(userRepository.findOneByResetKey("rk")).thenReturn(Optional.of(user));

        assertTrue(userService.completePasswordReset("newpass", "rk").isEmpty());
    }

    @Test
    void requestPasswordResetShouldSetResetFieldsForActivatedUser() {
        User user = createUser(1L, "alice", "alice@example.com");
        user.setActivated(true);
        when(userRepository.findOneByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        stubCaches();

        Optional<User> result = userService.requestPasswordReset("alice@example.com");

        assertTrue(result.isPresent());
        assertNotNull(user.getResetKey());
        assertNotNull(user.getResetDate());
    }

    @Test
    void requestPasswordResetShouldReturnEmptyForInactiveUser() {
        User user = createUser(1L, "alice", "alice@example.com");
        user.setActivated(false);
        when(userRepository.findOneByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));

        assertTrue(userService.requestPasswordReset("alice@example.com").isEmpty());
    }

    @Test
    void registerUserShouldCreateActivatedUserWithAuthority() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setLogin("JaneDoe");
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setEmail("JANE@EXAMPLE.COM");
        dto.setPhoneNumber("123");
        dto.setDob(new Date());
        dto.setGender("F");

        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.USER);

        when(userRepository.findOneByLogin("janedoe")).thenReturn(Optional.empty());
        when(userRepository.findOneByEmailIgnoreCase("JANE@EXAMPLE.COM")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(authorityRepository.findById(AuthoritiesConstants.USER)).thenReturn(Optional.of(authority));
        stubCaches();

        User result = userService.registerUser(dto, "secret");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("janedoe", saved.getLogin());
        assertEquals("jane@example.com", saved.getEmail());
        assertTrue(saved.isActivated());
        assertTrue(saved.getAuthorities().contains(authority));
        assertEquals("janedoe", result.getLogin());
    }

    @Test
    void registerUserShouldThrowWhenActivatedLoginExists() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setLogin("existing");
        dto.setEmail("new@example.com");
        User existing = createUser(1L, "existing", "old@example.com");
        existing.setActivated(true);
        when(userRepository.findOneByLogin("existing")).thenReturn(Optional.of(existing));

        assertThrows(UsernameAlreadyUsedException.class, () -> userService.registerUser(dto, "secret"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUserShouldThrowWhenActivatedEmailExists() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setLogin("newuser");
        dto.setEmail("taken@example.com");
        User existing = createUser(1L, "old", "taken@example.com");
        existing.setActivated(true);
        when(userRepository.findOneByLogin("newuser")).thenReturn(Optional.empty());
        when(userRepository.findOneByEmailIgnoreCase("taken@example.com")).thenReturn(Optional.of(existing));

        assertThrows(EmailAlreadyUsedException.class, () -> userService.registerUser(dto, "secret"));
    }

    @Test
    void registerUserShouldReplaceNonActivatedExistingLogin() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setLogin("replaceMe");
        dto.setEmail("replace@example.com");
        User existing = createUser(1L, "replaceme", "old@example.com");
        existing.setActivated(false);
        when(userRepository.findOneByLogin("replaceme")).thenReturn(Optional.of(existing));
        when(userRepository.findOneByEmailIgnoreCase("replace@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        stubCaches();

        userService.registerUser(dto, "secret");

        verify(userRepository).delete(existing);
        verify(userRepository).flush();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserShouldUseDefaultLangKeyWhenMissing() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setLogin("alice");
        dto.setEmail("alice@example.com");
        dto.setFirstName("Alice");
        dto.setLastName("Tan");
        dto.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.ADMIN);
        when(passwordEncoder.encode("P@ssw0rd")).thenReturn("encoded");
        when(authorityRepository.findById(AuthoritiesConstants.ADMIN)).thenReturn(Optional.of(authority));
        stubCaches();

        User result = userService.createUser(dto);

        assertEquals("en", result.getLangKey());
        assertTrue(result.getAuthorities().contains(authority));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserShouldReturnEmptyWhenUserMissing() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertTrue(userService.updateUser(dto).isEmpty());
    }

    @Test
    void updateUserShouldUpdateExistingUser() {
        User user = createUser(1L, "alice", "alice@example.com");
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.ADMIN);
        user.getAuthorities().add(new Authority());
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(1L);
        dto.setLogin("AliceNew");
        dto.setFirstName("Alice");
        dto.setLastName("Tan");
        dto.setEmail("ALICE@EXAMPLE.COM");
        dto.setActivated(true);
        dto.setLangKey("fr");
        dto.setImageUrl("img");
        dto.setPhoneNumber("123");
        dto.setDob(new Date());
        dto.setGender("F");
        dto.setAuthorities(Set.of(AuthoritiesConstants.ADMIN));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(authorityRepository.findById(AuthoritiesConstants.ADMIN)).thenReturn(Optional.of(authority));
        stubCaches();

        Optional<AdminUserDTO> result = userService.updateUser(dto);

        assertTrue(result.isPresent());
        assertEquals("alicenew", user.getLogin());
        assertEquals("alice@example.com", user.getEmail());
        assertTrue(user.getAuthorities().contains(authority));
        verify(userRepository).save(user);
    }

    @Test
    void deleteUserShouldDeleteWhenPresent() {
        User user = createUser(1L, "alice", "alice@example.com");
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.of(user));
        stubCaches();

        userService.deleteUser("alice");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserShouldDoNothingWhenMissing() {
        when(userRepository.findOneByLogin("missing")).thenReturn(Optional.empty());
        userService.deleteUser("missing");
        verify(userRepository, never()).delete(any());
    }

    @Test
    void updateCurrentUserShouldUpdateWhenAuthenticated() {
        User user = createUser(1L, "alice", "old@example.com");
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.of(user));
        stubCaches();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, Collections.emptyList())
        );

        userService.updateUser("Alice", "Tan", "NEW@EXAMPLE.COM", "fr", "img", "123", new Date(), "F");

        assertEquals("new@example.com", user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateCurrentUserShouldDoNothingWhenNotAuthenticated() {
        userService.updateUser("Alice", "Tan", "new@example.com", "fr", "img", "123", new Date(), "F");
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordShouldUpdateWhenCurrentPasswordMatches() {
        User user = createUser(1L, "alice", "alice@example.com");
        user.setPassword("oldhash");
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpass", "oldhash")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("newhash");
        stubCaches();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, Collections.emptyList())
        );

        userService.changePassword("oldpass", "newpass");

        assertEquals("newhash", user.getPassword());
    }

    @Test
    void changePasswordShouldThrowWhenCurrentPasswordDoesNotMatch() {
        User user = createUser(1L, "alice", "alice@example.com");
        user.setPassword("oldhash");
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("badpass", "oldhash")).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, Collections.emptyList())
        );

        assertThrows(InvalidPasswordException.class, () -> userService.changePassword("badpass", "newpass"));
    }

    @Test
    void getAllManagedUsersShouldMapDtos() {
        Page<User> page = new PageImpl<>(List.of(createUser(1L, "alice", "alice@example.com")));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<AdminUserDTO> result = userService.getAllManagedUsers(Pageable.ofSize(20));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllPublicUsersShouldMapDtos() {
        Page<User> page = new PageImpl<>(List.of(createUser(1L, "alice", "alice@example.com")));
        when(userRepository.findAllByIdNotNullAndActivatedIsTrue(any(Pageable.class))).thenReturn(page);

        Page<UserDTO> result = userService.getAllPublicUsers(Pageable.ofSize(20));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getUserWithAuthoritiesShouldDelegate() {
        User user = createUser(1L, "alice", "alice@example.com");
        when(userRepository.findOneWithAuthoritiesByLogin("alice")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("alice", null, Collections.emptyList())
        );

        assertTrue(userService.getUserWithAuthoritiesByLogin("alice").isPresent());
        assertTrue(userService.getUserWithAuthorities().isPresent());
    }

    @Test
    void removeNotActivatedUsersShouldDeleteAllReturnedUsers() {
        User user = createUser(1L, "alice", "alice@example.com");
        when(userRepository.findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(any(Instant.class)))
            .thenReturn(List.of(user));
        stubCaches();

        userService.removeNotActivatedUsers();

        verify(userRepository).delete(user);
    }

    @Test
    void getAuthoritiesAndDemoMethodsShouldDelegate() {
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.USER);
        User user = createUser(1L, "alice", "alice@example.com");
        when(authorityRepository.findAll()).thenReturn(List.of(authority));
        when(userRepository.getAdminDetails("ROLE_ADMIN")).thenReturn(List.of(user));
        when(userRepository.getUserList()).thenReturn(List.of(user));
        when(userRepository.findIdByLogin("alice")).thenReturn(1);

        assertEquals(List.of(AuthoritiesConstants.USER), userService.getAuthorities());
        assertEquals(1, userService.getAdminDetails("ROLE_ADMIN").size());
        assertEquals(1, userService.getUserList().size());
        assertEquals(1, userService.findIdByLogin("alice"));
    }

    private void stubCaches() {
        when(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).thenReturn(loginCache);
        when(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).thenReturn(emailCache);
    }

    private User createUser(Long id, String login, String email) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        user.setEmail(email);
        user.setActivated(true);
        return user;
    }
}
