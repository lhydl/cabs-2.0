package org.cabs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Setter
@Getter
@Entity
@Table(name = "appointment")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Appointment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "appt_type", length = 100, nullable = false)
    private String apptType;

    @NotNull
    @Column(name = "appt_datetime", nullable = false)
    private Instant apptDatetime;

    @Size(max = 200)
    @Column(name = "remarks", length = 200)
    private String remarks;

    @NotNull
    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "doctor_id", nullable = true)
    private Integer doctorId;

    @Column(name = "status", nullable = true)
    private Integer status = 0;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Appointment)) {
            return false;
        }
        return getId() != null && getId().equals(((Appointment) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Appointment{" +
            "id=" + getId() +
            ", apptType='" + getApptType() + "'" +
            ", apptDatetime='" + getApptDatetime() + "'" +
            ", remarks='" + getRemarks() + "'" +
            ", patientId=" + getPatientId() +
            ", doctorId=" + getDoctorId() +
            ", status=" + getStatus() +
            "}";
    }
}
