package org.cabs.service;

import org.cabs.domain.Appointment;
import org.cabs.repository.AppointmentRepository.PatientDetailsProjection;
import org.cabs.repository.AppointmentRepository.PatientMappingsProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link org.cabs.domain.Appointment}.
 */
public interface AppointmentService {

    void deleteUserAppointments(Integer userId);

}
