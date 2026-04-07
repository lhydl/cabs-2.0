package org.cabs.service;

import java.util.List;
import java.util.Optional;
import org.cabs.entity.Appointment;
import org.cabs.repository.AppointmentRepository.PatientDetailsProjection;
import org.cabs.repository.AppointmentRepository.PatientMappingsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link org.cabs.entity.Appointment}.
 */
public interface AppointmentService {

    /**
     * Save an appointment.
     *
     * @param appointment the entity to save.
     * @return the persisted entity.
     */
    Appointment save(Appointment appointment);

    /**
     * Updates a appointment.
     *
     * @param appointment the entity to update.
     * @return the persisted entity.
     */
    Appointment update(Appointment appointment);

    /**
     * Partially updates an appointment.
     *
     * @param appointment the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Appointment> partialUpdate(Appointment appointment);

    /**
     * Get all the appointments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Appointment> findAll(Pageable pageable);

    /**
     * Get the "id" appointment.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Appointment> findOne(Long id);

    /**
     * Delete the "id" appointment.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    List<Appointment> getUserAppt(String userId, String predicate, String sort);

    List<String> getExistingTimeSlots(String selectedDate);

    void deleteUserAppointments(Integer userId);

    PatientDetailsProjection getPatientDetails(Long userId);

    List<PatientMappingsProjection> getPatientMappings();
}
