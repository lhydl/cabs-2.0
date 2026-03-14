package org.cabs.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Appointment.
 */
@Setter
@Getter
@Entity
@Table(name = "appointment")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
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
    private ZonedDateTime apptDatetime;

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

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Appointment id(Long id) {
        this.setId(id);
        return this;
    }

    public Appointment apptType(String apptType) {
        this.setApptType(apptType);
        return this;
    }

    public Appointment apptDatetime(ZonedDateTime apptDatetime) {
        this.setApptDatetime(apptDatetime);
        return this;
    }

    public Appointment remarks(String remarks) {
        this.setRemarks(remarks);
        return this;
    }

    public Appointment patientId(Integer patientId) {
        this.setPatientId(patientId);
        return this;
    }

    public Appointment doctorId(Integer doctorId) {
        this.setDoctorId(doctorId);
        return this;
    }

    public Appointment status(Integer status) {
        this.setStatus(status);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
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
