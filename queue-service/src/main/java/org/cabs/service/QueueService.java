package org.cabs.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.cabs.entity.Appointment;
import org.cabs.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueService {
       private final AppointmentRepository appointmentRepository;

    public List<Appointment> getTodayApptQueue() {
        return appointmentRepository.getTodayApptQueue();
    }


    public Integer updateQueueStatus(Integer id, Integer status) {
        return appointmentRepository.updateQueueStatus(id, status);
    }
}
