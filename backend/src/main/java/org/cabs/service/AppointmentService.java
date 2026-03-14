package org.cabs.service;

import org.cabs.domain.Appointment;
import org.cabs.repository.AppointmentRepository.PatientDetailsProjection;
import org.cabs.repository.AppointmentRepository.PatientMappingsProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link cabs.domain.Appointment}.
 */
public interface AppointmentService {
    /**
     * Save a appointment.
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
     * Partially updates a appointment.
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

    public List<Appointment> getUserAppt(String userId, String predicate, String sort);

    public List<String> getExistingTimeSlots(String selectedDate);

    void deleteUserAppointments(Integer userId);

    public PatientDetailsProjection getPatientDetails(Long userId);

    public List<PatientMappingsProjection> getPatientMappings();

    public List<Appointment> getTodaysAppointments();

    public Integer updateApptStatus(Integer id, Integer status);
}
