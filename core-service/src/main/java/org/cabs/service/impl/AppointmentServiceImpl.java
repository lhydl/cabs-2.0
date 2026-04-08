package org.cabs.service.impl;

import lombok.RequiredArgsConstructor;
import org.cabs.repository.AppointmentRepository;
import org.cabs.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.cabs.domain.Appointment}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Override
    public void deleteUserAppointments(Integer userId) {
        appointmentRepository.deleteUserAppointments(userId);
    }
}
