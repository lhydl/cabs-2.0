package org.cabs.web.rest;

import org.cabs.domain.Appointment;
import org.cabs.domain.User;
import org.cabs.repository.AppointmentRepository;
import org.cabs.repository.AppointmentRepository.PatientDetailsProjection;
import org.cabs.repository.AppointmentRepository.PatientMappingsProjection;
import org.cabs.service.AppointmentService;
import org.cabs.service.UserService;
import org.cabs.service.dto.AdminUserDTO;
import org.cabs.service.dto.AppointmentDTO;
import org.cabs.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link cabs.domain.Appointment}.
 */
@RestController
@RequestMapping("/api/appointments")
@Slf4j
public class AppointmentResource {

    private final Logger log = LoggerFactory.getLogger(AppointmentResource.class);

    private static final String ENTITY_NAME = "appointment";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AppointmentService appointmentService;

    private final AppointmentRepository appointmentRepository;

    private final UserService userService;

    public AppointmentResource(
        AppointmentService appointmentService,
        AppointmentRepository appointmentRepository,
        UserService userService
    ) {
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
    }

    /**
     * {@code POST  /appointments} : Create a new appointment.
     *
     * @param appointment the appointment to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new appointment, or with status {@code 400 (Bad Request)} if the appointment has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) throws URISyntaxException {
        Appointment appointment = new Appointment();
        appointment.setId(appointmentDTO.getId());
        appointment.setApptType(appointmentDTO.getApptType());
        appointment.setApptDatetime(appointmentDTO.getApptDatetime());
        appointment.setRemarks(appointmentDTO.getRemarks());
        appointment.setPatientId(appointmentDTO.getPatientId());
        appointment.setDoctorId(appointmentDTO.getDoctorId());

        log.debug("REST request to save Appointment : {}", appointment);
        if (appointment.getId() != null) {
            throw new BadRequestAlertException("A new appointment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (appointmentDTO.getPatientId() == 1) {
            AdminUserDTO user = new AdminUserDTO();
            user.setFirstName(appointmentDTO.getFirstName());
            user.setLastName(appointmentDTO.getLastName());
            user.setPhoneNumber(appointmentDTO.getPhoneNumber());
            user.setEmail(appointmentDTO.getEmail());
            user.setLogin(appointmentDTO.getFirstName().replaceAll("\\s+", "") + appointmentDTO.getLastName().replaceAll("\\s+", ""));
            user.setDob(appointmentDTO.getDob());
            user.setGender(appointmentDTO.getGender());
            User newUser = userService.registerUser(user, "P@ssw0rd");
            appointment.setPatientId(newUser.getId().intValue());
        }
        Appointment result = appointmentService.save(appointment);
        return ResponseEntity
            .created(new URI("/api/appointments/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /appointments/:id} : Updates an existing appointment.
     *
     * @param id the id of the appointment to save.
     * @param appointment the appointment to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appointment,
     * or with status {@code 400 (Bad Request)} if the appointment is not valid,
     * or with status {@code 500 (Internal Server Error)} if the appointment couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Appointment appointment
    ) throws URISyntaxException {
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
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, appointment.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /appointments/:id} : Partial updates given fields of an existing appointment, field will ignore if it is null
     *
     * @param id the id of the appointment to save.
     * @param appointment the appointment to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated appointment,
     * or with status {@code 400 (Bad Request)} if the appointment is not valid,
     * or with status {@code 404 (Not Found)} if the appointment is not found,
     * or with status {@code 500 (Internal Server Error)} if the appointment couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Appointment> partialUpdateAppointment(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Appointment appointment
    ) throws URISyntaxException {
        log.debug("REST request to partial update Appointment partially : {}, {}", id, appointment);
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

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, appointment.getId().toString())
        );
    }

    /**
     * {@code GET  /appointments} : get all the appointments.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of appointments in body.
     */
    @GetMapping("")
    public ResponseEntity<List<Appointment>> getAllAppointments(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Appointments");
        Page<Appointment> page = appointmentService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /appointments/:id} : get the "id" appointment.
     *
     * @param id the id of the appointment to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the appointment, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointment(@PathVariable("id") Long id) {
        log.debug("REST request to get Appointment : {}", id);
        Optional<Appointment> appointment = appointmentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(appointment);
    }

    /**
     * {@code DELETE  /appointments/:id} : delete the "id" appointment.
     *
     * @param id the id of the appointment to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable("id") Long id) {
        log.debug("REST request to delete Appointment : {}", id);
        appointmentService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    @GetMapping("/getuserappt")
    public List<Appointment> getUserAppt(
        @RequestParam(value = "userId") String userId,
        @RequestParam(value = "predicate") String predicate,
        @RequestParam(value = "sort") String sort
    ) {
        return appointmentService.getUserAppt(userId, predicate, sort);
    }

    @GetMapping("/getTime")
    public List<String> getExistingTimeSlots(@RequestParam(value = "selectedDate") String selectedDate) {
        return appointmentService.getExistingTimeSlots(selectedDate);
    }

    @GetMapping("/getPatientDetails")
    public PatientDetailsProjection getPatientDetails(@RequestParam(value = "userId") String userId) {
        return appointmentService.getPatientDetails(Long.parseLong(userId));
    }

    @GetMapping("/getPatientMappings")
    public List<PatientMappingsProjection> getPatientMappings() {
        return appointmentService.getPatientMappings();
    }

    @GetMapping("/getTodayAppt")
    public List<Appointment> getTodaysAppointments() {
        return appointmentService.getTodaysAppointments();
    }

    @PostMapping("/updateApptStatus")
    public Integer updateApptStatus(@RequestParam(value = "id") Integer id, @RequestParam(value = "status") Integer status) {
        return appointmentService.updateApptStatus(id, status);
    }
}
