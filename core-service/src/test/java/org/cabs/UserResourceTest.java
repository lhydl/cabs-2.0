package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.cabs.domain.User;
import org.cabs.repository.UserRepository;
import org.cabs.security.AuthoritiesConstants;
import org.cabs.service.AppointmentService;
import org.cabs.service.MailService;
import org.cabs.service.UserService;
import org.cabs.service.dto.AdminUserDTO;
import org.cabs.web.rest.UserResource;
import org.cabs.web.rest.errors.BadRequestAlertException;
import org.cabs.web.rest.errors.EmailAlreadyUsedException;
import org.cabs.web.rest.errors.LoginAlreadyUsedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailService mailService;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private UserResource userResource;

    @Test
    void createUserShouldRejectExistingId() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(1L);
        assertThrows(BadRequestAlertException.class, () -> userResource.createUser(dto));
    }

    @Test
    void createUserShouldRejectDuplicateLogin() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setLogin("alice");
        dto.setEmail("alice@example.com");
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.of(new User()));

        assertThrows(LoginAlreadyUsedException.class, () -> userResource.createUser(dto));
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setLogin("alice");
        dto.setEmail("alice@example.com");
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.empty());
        when(userRepository.findOneByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyUsedException.class, () -> userResource.createUser(dto));
    }

    @Test
    void createUserShouldCreateAndSendMail() throws URISyntaxException {
        ReflectionTestUtils.setField(userResource, "applicationName", "cabsApp");
        AdminUserDTO dto = new AdminUserDTO();
        dto.setLogin("alice");
        dto.setEmail("alice@example.com");
        User user = new User();
        user.setLogin("alice");
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.empty());
        when(userRepository.findOneByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
        when(userService.createUser(dto)).thenReturn(user);

        ResponseEntity<User> response = userResource.createUser(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(mailService).sendCreationEmail(user);
    }

    @Test
    void updateUserShouldRejectDuplicateEmail() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(1L);
        dto.setLogin("alice");
        dto.setEmail("alice@example.com");
        User other = new User();
        other.setId(2L);
        when(userRepository.findOneByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(other));

        assertThrows(EmailAlreadyUsedException.class, () -> userResource.updateUser(dto));
    }

    @Test
    void updateUserShouldRejectDuplicateLogin() {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(1L);
        dto.setLogin("alice");
        dto.setEmail("alice@example.com");
        User other = new User();
        other.setId(2L);
        when(userRepository.findOneByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.of(other));

        assertThrows(LoginAlreadyUsedException.class, () -> userResource.updateUser(dto));
    }

    @Test
    void updateUserShouldReturnUpdatedUser() {
        ReflectionTestUtils.setField(userResource, "applicationName", "cabsApp");
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(1L);
        dto.setLogin("alice");
        dto.setEmail("alice@example.com");
        when(userRepository.findOneByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.findOneByLogin("alice")).thenReturn(Optional.empty());
        when(userService.updateUser(dto)).thenReturn(Optional.of(dto));

        ResponseEntity<AdminUserDTO> response = userResource.updateUser(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllUsersShouldRejectDisallowedSortProperty() {
        ResponseEntity<List<AdminUserDTO>> response = userResource.getAllUsers(PageRequest.of(0, 20, Sort.by("password")));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getAllUsersShouldReturnPageWhenSortAllowed() {
        when(userService.getAllManagedUsers(any())).thenReturn(new PageImpl<>(List.of(new AdminUserDTO())));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        ResponseEntity<List<AdminUserDTO>> response = userResource.getAllUsers(PageRequest.of(0, 20, Sort.by("login")));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getUserShouldReturnWrappedUser() {
        User user = new User();
        user.setLogin("alice");
        when(userService.getUserWithAuthoritiesByLogin("alice")).thenReturn(Optional.of(user));
        ResponseEntity<AdminUserDTO> response = userResource.getUser("alice");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteUserShouldDelegateDeletion() {
        ReflectionTestUtils.setField(userResource, "applicationName", "cabsApp");
        when(userService.findIdByLogin("alice")).thenReturn(1);

        ResponseEntity<Void> response = userResource.deleteUser("alice");

        verify(appointmentService).deleteUserAppointments(1);
        verify(userService).deleteUser("alice");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void demoEndpointsShouldDelegate() {
        User user = new User();
        when(userService.getAdminDetails(AuthoritiesConstants.ADMIN)).thenReturn(List.of(user));
        when(userService.getUserList()).thenReturn(List.of(user));

        assertEquals(1, userResource.getAdminDetails(AuthoritiesConstants.ADMIN).size());
        assertTrue(userResource.getUserList().contains(user));
    }
}
