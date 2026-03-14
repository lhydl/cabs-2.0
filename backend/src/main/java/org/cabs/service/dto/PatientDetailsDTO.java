package org.cabs.service.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PatientDetailsDTO {

    // Setters
    // Getters
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    // Constructor
    public PatientDetailsDTO(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Override toString(), equals(), and hashCode() if necessary
    @Override
    public String toString() {
        return (
            "PatientDetailsDTO{" +
            "firstName='" +
            firstName +
            '\'' +
            ", lastName='" +
            lastName +
            '\'' +
            ", email='" +
            email +
            '\'' +
            ", phoneNumber='" +
            phoneNumber +
            '\'' +
            '}'
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatientDetailsDTO that = (PatientDetailsDTO) o;

        if (!firstName.equals(that.firstName)) return false;
        if (!lastName.equals(that.lastName)) return false;
        if (!email.equals(that.email)) return false;
        return phoneNumber.equals(that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
