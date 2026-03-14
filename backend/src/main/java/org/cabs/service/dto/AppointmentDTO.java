package org.cabs.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class AppointmentDTO implements Serializable {

    private Long id;
    private String apptType;
    private ZonedDateTime apptDatetime;
    private String remarks;
    private Integer patientId;
    private Integer doctorId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private Date dob;
    private String gender;
    private Integer status;

    @Override
    public String toString() {
        return "AppointmentDTO{" +
            "id=" + id +
            ", apptType='" + apptType + '\'' +
            ", apptDatetime=" + apptDatetime +
            ", remarks='" + remarks + '\'' +
            ", patientId=" + patientId +
            ", doctorId=" + doctorId +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", email='" + email + '\'' +
            ", dob=" + dob +
            ", gender='" + gender + '\'' +
            ", status=" + status +
            '}';
    }
}
