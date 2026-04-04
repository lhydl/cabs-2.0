package org.cabs.repository;

import java.util.List;
import org.cabs.entity.Appointment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Appointment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query(
        value = " SELECT " +
            "     appt_datetime " +
            " FROM " +
            "     cabs.appointment " +
            " where " +
            "     DATE(appt_datetime) = :selectedDate ",
        nativeQuery = true
    )
    List<String> getExistingTimeSlots(@Param("selectedDate") String selectedDate);

    @Modifying
    @Query(value = " DELETE FROM " + "     cabs.appointment A " + " where "
        + "     A.patient_id = :userId ", nativeQuery = true)
    void deleteUserAppointments(@Param("userId") Integer userId);

    // @Query(
    //     "SELECT new cabs.service.dto.PatientDetailsDTO(u.firstName, u.lastName, u.email, u.phoneNumber) FROM User u WHERE u.id = :userId"
    // )
    // PatientDetailsDTO getPatientDetails(@Param("userId") Long userId);

    public interface PatientDetailsProjection {

        String getLogin();

        String getFirstName();

        String getLastName();

        String getEmail();

        String getPhoneNumber();

        String getDob();

        String getGender();
    }

    @Query(
        value = "SELECT U.login AS login, U.first_name AS firstName, U.last_name AS lastName, U.email AS email, U.phone_number AS phoneNumber, U.dob AS dob, U.gender AS gender FROM cabs.jhi_user U WHERE U.id = :userId",
        nativeQuery = true
    )
    PatientDetailsProjection getPatientDetails(@Param("userId") Long userId);

    public interface PatientMappingsProjection {

        String getLogin();

        String getId();

        String getFirstName();

        String getLastName();
    }

    @Query(
        value = "SELECT U.login AS login, U.id AS id, U.first_name AS firstName, U.last_name AS lastName FROM cabs.jhi_user U",
        nativeQuery = true
    )
    List<PatientMappingsProjection> getPatientMappings();
}
