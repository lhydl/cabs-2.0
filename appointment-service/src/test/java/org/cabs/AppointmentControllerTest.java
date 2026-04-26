package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.cabs.controller.AppointmentController;
import org.cabs.dto.AppointmentDTO;
import org.cabs.entity.Appointment;
import org.cabs.entity.User;
import org.cabs.exception.BadRequestAlertException;
import org.cabs.repository.AppointmentRepository;
import org.cabs.service.AppointmentService;
import org.cabs.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AppointmentController appointmentController;

    @Test
    void createAppointmentShouldSaveDirectlyForExistingPatient() throws URISyntaxException {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setApptType("Consultation");
        dto.setApptDatetime(Instant.parse("2026-04-26T09:00:00Z"));
        dto.setRemarks("Checkup");
        dto.setPatientId(10);
        dto.setDoctorId(2);

        Appointment saved = new Appointment();
        saved.setId(5L);
        saved.setApptType(dto.getApptType());
        saved.setApptDatetime(dto.getApptDatetime());
        saved.setRemarks(dto.getRemarks());
        saved.setPatientId(dto.getPatientId());
        saved.setDoctorId(dto.getDoctorId());

        when(appointmentService.save(any(Appointment.class))).thenReturn(saved);

        ResponseEntity<Appointment> response = appointmentController.createAppointment(dto);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentService).save(captor.capture());
        Appointment toSave = captor.getValue();
        assertEquals(10, toSave.getPatientId());
        assertEquals("Consultation", toSave.getApptType());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/api/appointments/5", response.getHeaders().getLocation().toString());
        assertEquals("Appointment created", response.getHeaders().getFirst("X-appointment-alert"));
    }

    @Test
    void createAppointmentShouldCreateNewUserWhenSentinelPatientIdIsOne() throws URISyntaxException {
        ReflectionTestUtils.setField(appointmentController, "password", "defaultpw");

        AppointmentDTO dto = new AppointmentDTO();
        dto.setApptType("Dental");
        dto.setApptDatetime(Instant.parse("2026-04-26T10:00:00Z"));
        dto.setPatientId(1);
        dto.setFirstName("Jane ");
        dto.setLastName(" Doe");
        dto.setPhoneNumber("12345678");
        dto.setEmail("JANE@EXAMPLE.COM");
        dto.setDob(new Date());
        dto.setGender("Female");

        User newUser = new User();
        newUser.setId(42L);
        when(userService.registerUser(any(), any())).thenReturn(newUser);

        Appointment saved = new Appointment();
        saved.setId(7L);
        saved.setPatientId(42);
        when(appointmentService.save(any(Appointment.class))).thenReturn(saved);

        ResponseEntity<Appointment> response = appointmentController.createAppointment(dto);

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentService).save(appointmentCaptor.capture());
        assertEquals(42, appointmentCaptor.getValue().getPatientId());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(42, response.getBody().getPatientId());
    }

    @Test
    void createAppointmentShouldRejectExistingId() {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(99L);

        assertThrows(BadRequestAlertException.class, () -> appointmentController.createAppointment(dto));
    }

    @Test
    void updateAppointmentShouldRejectNullId() {
        Appointment appointment = new Appointment();

        assertThrows(BadRequestAlertException.class, () -> appointmentController.updateAppointment(1L, appointment));
    }

    @Test
    void updateAppointmentShouldRejectMismatchedId() {
        Appointment appointment = new Appointment();
        appointment.setId(2L);

        assertThrows(BadRequestAlertException.class, () -> appointmentController.updateAppointment(1L, appointment));
    }

    @Test
    void updateAppointmentShouldRejectMissingEntity() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        when(appointmentRepository.existsById(1L)).thenReturn(false);

        assertThrows(BadRequestAlertException.class, () -> appointmentController.updateAppointment(1L, appointment));
    }

    @Test
    void updateAppointmentShouldReturnUpdatedEntity() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        when(appointmentRepository.existsById(1L)).thenReturn(true);
        when(appointmentService.update(appointment)).thenReturn(appointment);

        ResponseEntity<Appointment> response = appointmentController.updateAppointment(1L, appointment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(appointment, response.getBody());
        assertEquals("Appointment updated", response.getHeaders().getFirst("X-appointment-alert"));
    }

    @Test
    void partialUpdateShouldRejectNullId() {
        Appointment appointment = new Appointment();

        assertThrows(BadRequestAlertException.class, () -> appointmentController.partialUpdateAppointment(1L, appointment));
    }

    @Test
    void partialUpdateShouldRejectMismatchedId() {
        Appointment appointment = new Appointment();
        appointment.setId(2L);

        assertThrows(BadRequestAlertException.class, () -> appointmentController.partialUpdateAppointment(1L, appointment));
    }

    @Test
    void partialUpdateShouldRejectMissingEntity() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        when(appointmentRepository.existsById(1L)).thenReturn(false);

        assertThrows(BadRequestAlertException.class, () -> appointmentController.partialUpdateAppointment(1L, appointment));
    }

    @Test
    void partialUpdateShouldReturnUpdatedEntityWhenPresent() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        when(appointmentRepository.existsById(1L)).thenReturn(true);
        when(appointmentService.partialUpdate(appointment)).thenReturn(Optional.of(appointment));

        ResponseEntity<Appointment> response = appointmentController.partialUpdateAppointment(1L, appointment);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(appointment, response.getBody());
    }

    @Test
    void partialUpdateShouldReturnNotFoundWhenServiceReturnsEmpty() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        when(appointmentRepository.existsById(1L)).thenReturn(true);
        when(appointmentService.partialUpdate(appointment)).thenReturn(Optional.empty());

        ResponseEntity<Appointment> response = appointmentController.partialUpdateAppointment(1L, appointment);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllAppointmentsShouldReturnPageContentAndHeaders() {
        Appointment appointment = new Appointment();
        Page<Appointment> page = new PageImpl<>(List.of(appointment), Pageable.ofSize(20), 1);
        when(appointmentService.findAll(any(Pageable.class))).thenReturn(page);

        ResponseEntity<List<Appointment>> response = appointmentController.getAllAppointments(Pageable.ofSize(20));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("1", response.getHeaders().getFirst("X-Total-Count"));
        assertEquals("1", response.getHeaders().getFirst("X-Total-Pages"));
    }

    @Test
    void getAppointmentShouldReturnFoundEntity() {
        Appointment appointment = new Appointment();
        when(appointmentService.findOne(1L)).thenReturn(Optional.of(appointment));

        ResponseEntity<Appointment> response = appointmentController.getAppointment(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(appointment, response.getBody());
    }

    @Test
    void getAppointmentShouldReturnNotFoundWhenMissing() {
        when(appointmentService.findOne(1L)).thenReturn(Optional.empty());

        ResponseEntity<Appointment> response = appointmentController.getAppointment(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteAppointmentShouldDelegateAndReturnNoContent() {
        ResponseEntity<Void> response = appointmentController.deleteAppointment(9L);

        verify(appointmentService).delete(9L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals("Appointment deleted", response.getHeaders().getFirst("X-appointment-alert"));
    }

    @Test
    void customEndpointsShouldDelegateToService() {
        List<Appointment> appointments = List.of(new Appointment());
        List<String> timeSlots = List.of("09:00");
        AppointmentRepository.PatientDetailsProjection patientDetails = new AppointmentRepository.PatientDetailsProjection() {
            @Override public String getLogin() { return "alice"; }
            @Override public String getFirstName() { return "Alice"; }
            @Override public String getLastName() { return "Tan"; }
            @Override public String getEmail() { return "alice@example.com"; }
            @Override public String getPhoneNumber() { return "123"; }
            @Override public String getDob() { return "2000-01-01"; }
            @Override public String getGender() { return "F"; }
        };
        AppointmentRepository.PatientMappingsProjection mapping = new AppointmentRepository.PatientMappingsProjection() {
            @Override public String getLogin() { return "alice"; }
            @Override public String getId() { return "1"; }
            @Override public String getFirstName() { return "Alice"; }
            @Override public String getLastName() { return "Tan"; }
        };

        when(appointmentService.getUserAppt("7", "appt_datetime", "ASC")).thenReturn(appointments);
        when(appointmentService.getExistingTimeSlots("2026-04-26")).thenReturn(timeSlots);
        when(appointmentService.getPatientDetails(5L)).thenReturn(patientDetails);
        when(appointmentService.getPatientMappings()).thenReturn(List.of(mapping));

        assertSame(appointments, appointmentController.getUserAppt("7", "appt_datetime", "ASC"));
        assertSame(timeSlots, appointmentController.getExistingTimeSlots("2026-04-26"));
        assertSame(patientDetails, appointmentController.getPatientDetails("5"));
        assertEquals(1, appointmentController.getPatientMappings().size());
    }

    @Test
    void healthCheckShouldReturnOkMessage() {
        ResponseEntity<String> response = appointmentController.healthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Service is up and running"));
    }
}
