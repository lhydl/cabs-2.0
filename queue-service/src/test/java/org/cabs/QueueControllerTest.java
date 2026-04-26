package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.cabs.controller.QueueController;
import org.cabs.entity.Appointment;
import org.cabs.service.QueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class QueueControllerTest {

    @Mock
    private QueueService queueService;

    @InjectMocks
    private QueueController queueController;

    @Test
    void getTodayApptQueueShouldReturnAppointmentsFromService() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setApptType("Consultation");
        appointment.setApptDatetime(Instant.parse("2026-04-26T09:00:00Z"));
        appointment.setPatientId(10);

        List<Appointment> expected = List.of(appointment);
        when(queueService.getTodayApptQueue()).thenReturn(expected);

        List<Appointment> result = queueController.getTodayApptQueue();

        assertSame(expected, result);
        verify(queueService).getTodayApptQueue();
    }

    @Test
    void updateQueueStatusShouldDelegateToService() {
        when(queueService.updateQueueStatus(12, 2)).thenReturn(1);

        Integer result = queueController.updateQueueStatus(12, 2);

        assertEquals(1, result);
        verify(queueService).updateQueueStatus(12, 2);
    }

    @Test
    void healthCheckShouldReturnOkMessage() {
        ResponseEntity<String> response = queueController.healthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Service is up and running", response.getBody());
    }
}
