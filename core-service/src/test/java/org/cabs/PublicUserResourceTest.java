package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cabs.service.UserService;
import org.cabs.service.dto.UserDTO;
import org.cabs.web.rest.PublicUserResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class PublicUserResourceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PublicUserResource publicUserResource;

    @Test
    void getAllPublicUsersShouldRejectDisallowedSortProperty() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("password"));

        ResponseEntity<List<UserDTO>> response = publicUserResource.getAllPublicUsers(pageable);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getAllPublicUsersShouldReturnPageWhenSortAllowed() {
        UserDTO dto = new UserDTO();
        when(userService.getAllPublicUsers(org.mockito.ArgumentMatchers.any())).thenReturn(new PageImpl<>(List.of(dto)));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

        ResponseEntity<List<UserDTO>> response = publicUserResource.getAllPublicUsers(PageRequest.of(0, 20, Sort.by("login")));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getAuthoritiesShouldDelegate() {
        when(userService.getAuthorities()).thenReturn(List.of("ROLE_USER"));
        assertTrue(publicUserResource.getAuthorities().contains("ROLE_USER"));
    }
}
