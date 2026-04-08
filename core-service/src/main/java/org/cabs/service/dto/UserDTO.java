package org.cabs.service.dto;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.cabs.domain.User;
import java.io.Serializable;

/**
 * A DTO representing a user, with only the public attributes.
 */
@Setter
@Getter
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String login;

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user) {
        this.id = user.getId();
        // Customize it here if you need, or not, firstName/lastName/etc
        this.login = user.getLogin();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserDTO{" +
            "id='" + id + '\'' +
            ", login='" + login + '\'' +
            "}";
    }
}
