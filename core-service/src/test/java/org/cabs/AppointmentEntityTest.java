package org.cabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import org.cabs.domain.Appointment;
import org.junit.jupiter.api.Test;

class AppointmentEntityTest {

    @Test
    void fluentSettersShouldReturnSameInstance() {
        Appointment appointment = new Appointment();
        ZonedDateTime dateTime = ZonedDateTime.now();

        Appointment result = appointment.id(1L).apptType("Consultation").apptDatetime(dateTime).remarks("Check").patientId(10).doctorId(20).status(1);

        assertEquals(appointment, result);
        assertEquals(1L, appointment.getId());
        assertEquals("Consultation", appointment.getApptType());
        assertEquals(dateTime, appointment.getApptDatetime());
        assertEquals("Check", appointment.getRemarks());
        assertEquals(10, appointment.getPatientId());
        assertEquals(20, appointment.getDoctorId());
        assertEquals(1, appointment.getStatus());
    }

    @Test
    void equalsShouldCoverBranches() {
        Appointment left = new Appointment();
        left.setId(1L);
        Appointment same = new Appointment();
        same.setId(1L);
        Appointment other = new Appointment();
        other.setId(2L);

        assertEquals(left, left);
        assertEquals(left, same);
        assertNotEquals(left, other);
        assertNotEquals(new Appointment(), new Appointment());
        assertNotEquals(left, "appt");
        assertEquals(left.hashCode(), left.getClass().hashCode());
    }

    @Test
    void toStringShouldIncludeFields() {
        Appointment appointment = new Appointment();
        appointment.setId(3L);
        appointment.setApptType("Consultation");
        appointment.setRemarks("Check");
        appointment.setPatientId(10);
        appointment.setDoctorId(20);
        appointment.setStatus(1);

        String result = appointment.toString();

        assertTrue(result.contains("id=3"));
        assertTrue(result.contains("Consultation"));
        assertTrue(result.contains("Check"));
    }
}
