package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.cabs.entity.Appointment;
import org.junit.jupiter.api.Test;

class AppointmentEntityTest {

    @Test
    void equalsShouldReturnTrueForSameInstance() {
        Appointment appointment = new Appointment();
        assertEquals(appointment, appointment);
    }

    @Test
    void equalsShouldReturnFalseForDifferentType() {
        Appointment appointment = new Appointment();
        appointment.setId(1L);

        assertNotEquals(appointment, "not-an-appointment");
    }

    @Test
    void equalsShouldReturnFalseWhenIdIsNull() {
        Appointment left = new Appointment();
        Appointment right = new Appointment();

        assertNotEquals(left, right);
    }

    @Test
    void equalsShouldReturnTrueWhenIdsMatch() {
        Appointment left = new Appointment();
        left.setId(1L);
        Appointment right = new Appointment();
        right.setId(1L);

        assertEquals(left, right);
        assertEquals(left.hashCode(), left.getClass().hashCode());
    }

    @Test
    void toStringShouldIncludeMainFields() {
        Appointment appointment = new Appointment();
        appointment.setId(3L);
        appointment.setApptType("Consultation");
        appointment.setApptDatetime(Instant.parse("2026-04-26T09:00:00Z"));
        appointment.setRemarks("Test remarks");
        appointment.setPatientId(10);
        appointment.setDoctorId(20);
        appointment.setStatus(1);

        String result = appointment.toString();

        assertTrue(result.contains("id=3"));
        assertTrue(result.contains("Consultation"));
        assertTrue(result.contains("Test remarks"));
        assertTrue(result.contains("patientId=10"));
        assertTrue(result.contains("doctorId=20"));
        assertTrue(result.contains("status=1"));
    }
}
