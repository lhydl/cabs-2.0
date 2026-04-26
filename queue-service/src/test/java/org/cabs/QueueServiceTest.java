package org.cabs;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.cabs.entity.Appointment;
import org.cabs.repository.AppointmentRepository;
import org.cabs.service.QueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private QueueService queueService;

    @Test
    void getTodayApptQueueShouldDelegateToRepository() {
        List<Appointment> expected = List.of(new Appointment());

        when(appointmentRepository.getTodayApptQueue()).thenReturn(expected);

        List<Appointment> result = queueService.getTodayApptQueue();

        assertSame(expected, result);
        verify(appointmentRepository).getTodayApptQueue();
    }

    @Test
    void updateQueueStatusShouldDelegateToRepository() {
        when(appointmentRepository.updateQueueStatus(12, 1)).thenReturn(1);

        Integer result = queueService.updateQueueStatus(12, 1);

        assertSame(1, result);
        verify(appointmentRepository).updateQueueStatus(12, 1);
    }
}
