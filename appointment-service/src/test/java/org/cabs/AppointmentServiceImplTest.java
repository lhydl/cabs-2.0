package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.cabs.entity.Appointment;
import org.cabs.repository.AppointmentRepository;
import org.cabs.repository.AppointmentRepositoryCustom;
import org.cabs.service.AppointmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentRepositoryCustom appointmentRepositoryCustom;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void partialUpdateShouldOnlyOverwriteProvidedFields() {
        Appointment existing = new Appointment();
        existing.setId(1L);
        existing.setApptType("Consultation");
        existing.setApptDatetime(Instant.parse("2026-04-26T09:00:00Z"));
        existing.setRemarks("Original remarks");
        existing.setPatientId(10);
        existing.setDoctorId(20);

        Appointment patch = new Appointment();
        patch.setId(1L);
        patch.setRemarks("Updated remarks");
        patch.setPatientId(42);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(existing)).thenReturn(existing);

        Optional<Appointment> result = appointmentService.partialUpdate(patch);

        assertTrue(result.isPresent());
        assertEquals("Consultation", existing.getApptType());
        assertEquals(Instant.parse("2026-04-26T09:00:00Z"), existing.getApptDatetime());
        assertEquals("Updated remarks", existing.getRemarks());
        assertEquals(42, existing.getPatientId());
        assertEquals(20, existing.getDoctorId());

        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository).save(existing);
        verifyNoMoreInteractions(appointmentRepositoryCustom);
    }

    @Test
    void partialUpdateShouldReturnEmptyWhenAppointmentDoesNotExist() {
        Appointment patch = new Appointment();
        patch.setId(99L);

        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Appointment> result = appointmentService.partialUpdate(patch);

        assertFalse(result.isPresent());
        verify(appointmentRepository).findById(99L);
        verifyNoMoreInteractions(appointmentRepository);
        verifyNoMoreInteractions(appointmentRepositoryCustom);
    }

    @Test
    void getUserApptShouldParseUserIdAndDelegateToCustomRepository() {
        Appointment appointment = new Appointment();
        List<Appointment> expected = List.of(appointment);

        when(appointmentRepositoryCustom.getUserAppt(7, "appt_datetime", "ASC")).thenReturn(expected);

        List<Appointment> result = appointmentService.getUserAppt("7", "appt_datetime", "ASC");

        assertSame(expected, result);
        verify(appointmentRepositoryCustom).getUserAppt(7, "appt_datetime", "ASC");
    }

    @Test
    void getExistingTimeSlotsShouldDelegateToRepository() {
        List<String> expected = List.of("2026-04-26 09:00:00");

        when(appointmentRepository.getExistingTimeSlots("2026-04-26")).thenReturn(expected);

        List<String> result = appointmentService.getExistingTimeSlots("2026-04-26");

        assertSame(expected, result);
        verify(appointmentRepository).getExistingTimeSlots("2026-04-26");
    }
}
