package org.cabs.service;

import java.util.List;
import java.util.Optional;
import org.cabs.entity.Appointment;
import org.cabs.repository.AppointmentRepository;
import org.cabs.repository.AppointmentRepository.PatientDetailsProjection;
import org.cabs.repository.AppointmentRepository.PatientMappingsProjection;
import org.cabs.repository.AppointmentRepositoryCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link org.cabs.entity.Appointment}.
 */
@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final AppointmentRepository appointmentRepository;

    private final AppointmentRepositoryCustom appointmentRepositoryCustom;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
        AppointmentRepositoryCustom appointmentRepositoryCustom) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentRepositoryCustom = appointmentRepositoryCustom;
    }

    @Override
    public Appointment save(Appointment appointment) {
        log.debug("Request to save Appointment : {}", appointment);
        return appointmentRepository.save(appointment);
    }

    @Override
    public Appointment update(Appointment appointment) {
        log.debug("Request to update Appointment : {}", appointment);
        return appointmentRepository.save(appointment);
    }

    @Override
    public Optional<Appointment> partialUpdate(Appointment appointment) {
        log.debug("Request to partially update Appointment : {}", appointment);

        return appointmentRepository
            .findById(appointment.getId())
            .map(existingAppointment -> {
                if (appointment.getApptType() != null) {
                    existingAppointment.setApptType(appointment.getApptType());
                }
                if (appointment.getApptDatetime() != null) {
                    existingAppointment.setApptDatetime(appointment.getApptDatetime());
                }
                if (appointment.getRemarks() != null) {
                    existingAppointment.setRemarks(appointment.getRemarks());
                }
                if (appointment.getPatientId() != null) {
                    existingAppointment.setPatientId(appointment.getPatientId());
                }
                // if (appointment.getDoctorId() != null) {
                //     existingAppointment.setDoctorId(appointment.getDoctorId());
                // }

                return existingAppointment;
            })
            .map(appointmentRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Appointment> findAll(Pageable pageable) {
        log.debug("Request to get all Appointments");
        return appointmentRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Appointment> findOne(Long id) {
        log.debug("Request to get Appointment : {}", id);
        return appointmentRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Appointment : {}", id);
        appointmentRepository.deleteById(id);
    }

    @Override
    public List<Appointment> getUserAppt(String userId, String predicate, String sort) {
        Integer id = Integer.parseInt(userId);
        return appointmentRepositoryCustom.getUserAppt(id, predicate, sort);
    }

    @Override
    public List<String> getExistingTimeSlots(String selectedDate) {
        return appointmentRepository.getExistingTimeSlots(selectedDate);
    }

    @Override
    public void deleteUserAppointments(Integer userId) {
        appointmentRepository.deleteUserAppointments(userId);
    }

    @Override
    public PatientDetailsProjection getPatientDetails(Long userId) {
        return appointmentRepository.getPatientDetails(userId);
    }

    @Override
    public List<PatientMappingsProjection> getPatientMappings() {
        return appointmentRepository.getPatientMappings();
    }

    @Override
    public List<Appointment> getTodaysAppointments() {
        return appointmentRepositoryCustom.getTodaysAppointments();
    }

    @Override
    public Integer updateApptStatus(Integer id, Integer status) {
        return appointmentRepositoryCustom.updateApptStatus(id, status);
    }
}
