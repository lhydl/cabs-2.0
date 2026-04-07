package org.cabs.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.cabs.dto.AdminUserDTO;
import org.cabs.dto.AppointmentDTO;
import org.cabs.entity.Appointment;
import org.cabs.entity.User;
import org.cabs.exception.BadRequestAlertException;
import org.cabs.repository.AppointmentRepository;
import org.cabs.repository.AppointmentRepository.PatientDetailsProjection;
import org.cabs.repository.AppointmentRepository.PatientMappingsProjection;
import org.cabs.service.AppointmentService;
import org.cabs.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
@Slf4j
public class AppointmentController {

    private static final String ENTITY_NAME = "appointment";

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final UserService userService;

    public AppointmentController(
        AppointmentService appointmentService,
        AppointmentRepository appointmentRepository,
        UserService userService
    ) {
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
    }

    // ========================= CREATE =========================
    @PostMapping("")
    public ResponseEntity<Appointment> createAppointment(
        @Valid @RequestBody AppointmentDTO appointmentDTO
    ) throws URISyntaxException {

        Appointment appointment = new Appointment();
        appointment.setId(appointmentDTO.getId());
        appointment.setApptType(appointmentDTO.getApptType());
        appointment.setApptDatetime(appointmentDTO.getApptDatetime());
        appointment.setRemarks(appointmentDTO.getRemarks());
        appointment.setPatientId(appointmentDTO.getPatientId());
        appointment.setDoctorId(appointmentDTO.getDoctorId());

        log.debug("REST request to save Appointment : {}", appointment);

        if (appointment.getId() != null) {
            throw new BadRequestAlertException(
                "A new appointment cannot already have an ID",
                ENTITY_NAME,
                "idexists"
            );
        }

        // Create new user if needed
        if (appointmentDTO.getPatientId() == 1) {
            AdminUserDTO user = new AdminUserDTO();
            user.setFirstName(appointmentDTO.getFirstName());
            user.setLastName(appointmentDTO.getLastName());
            user.setPhoneNumber(appointmentDTO.getPhoneNumber());
            user.setEmail(appointmentDTO.getEmail());
            user.setLogin(
                appointmentDTO.getFirstName().replaceAll("\\s+", "") +
                    appointmentDTO.getLastName().replaceAll("\\s+", "")
            );
            user.setDob(appointmentDTO.getDob());
            user.setGender(appointmentDTO.getGender());

            User newUser = userService.registerUser(user, "P@ssw0rd");
            appointment.setPatientId(newUser.getId().intValue());
        }

        Appointment result = appointmentService.save(appointment);

        HttpHeaders headers = createAlert("Appointment created", result.getId().toString());

        return ResponseEntity
            .created(new URI("/api/appointments/" + result.getId()))
            .headers(headers)
            .body(result);
    }

    // ========================= UPDATE =========================
    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Appointment appointment
    ) {

        log.debug("REST request to update Appointment : {}, {}", id, appointment);

        if (appointment.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if (!Objects.equals(id, appointment.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!appointmentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Appointment result = appointmentService.update(appointment);

        HttpHeaders headers = createAlert("Appointment updated", appointment.getId().toString());

        return ResponseEntity.ok().headers(headers).body(result);
    }

    // ========================= PARTIAL UPDATE =========================
    @PatchMapping(value = "/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<Appointment> partialUpdateAppointment(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Appointment appointment
    ) {

        log.debug("REST request to partial update Appointment : {}, {}", id, appointment);

        if (appointment.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if (!Objects.equals(id, appointment.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!appointmentRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Appointment> result = appointmentService.partialUpdate(appointment);

        return result
            .map(updated -> {
                HttpHeaders headers = createAlert("Appointment updated",
                    updated.getId().toString());
                return ResponseEntity.ok().headers(headers).body(updated);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ========================= GET ALL =========================
    @GetMapping("")
    public ResponseEntity<List<Appointment>> getAllAppointments(Pageable pageable) {

        log.debug("REST request to get a page of Appointments");

        Page<Appointment> page = appointmentService.findAll(pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    // ========================= GET ONE =========================
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointment(@PathVariable("id") Long id) {

        log.debug("REST request to get Appointment : {}", id);

        Optional<Appointment> appointment = appointmentService.findOne(id);

        return ResponseEntity.of(appointment);
    }

    // ========================= DELETE =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable("id") Long id) {

        log.debug("REST request to delete Appointment : {}", id);

        appointmentService.delete(id);

        HttpHeaders headers = createAlert("Appointment deleted", id.toString());

        return ResponseEntity.noContent().headers(headers).build();
    }

    // ========================= CUSTOM APIs =========================

    @GetMapping("/getuserappt")
    public List<Appointment> getUserAppt(
        @RequestParam("userId") String userId,
        @RequestParam("predicate") String predicate,
        @RequestParam("sort") String sort
    ) {
        return appointmentService.getUserAppt(userId, predicate, sort);
    }

    @GetMapping("/getTime")
    public List<String> getExistingTimeSlots(@RequestParam("selectedDate") String selectedDate) {
        return appointmentService.getExistingTimeSlots(selectedDate);
    }

    @GetMapping("/getPatientDetails")
    public PatientDetailsProjection getPatientDetails(@RequestParam("userId") String userId) {
        return appointmentService.getPatientDetails(Long.parseLong(userId));
    }

    @GetMapping("/getPatientMappings")
    public List<PatientMappingsProjection> getPatientMappings() {
        return appointmentService.getPatientMappings();
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body("Service is up and running");
    }

    // ========================= HELPER =========================
    private HttpHeaders createAlert(String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-" + ENTITY_NAME + "-alert", message);
        headers.add("X-" + ENTITY_NAME + "-params", param);
        return headers;
    }

}