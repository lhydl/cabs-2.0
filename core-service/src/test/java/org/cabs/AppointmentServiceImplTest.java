package org.cabs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

import org.cabs.repository.AppointmentRepository;
import org.cabs.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    void deleteUserAppointmentsShouldDelegate() {
        assertDoesNotThrow(() -> appointmentService.deleteUserAppointments(7));
        verify(appointmentRepository).deleteUserAppointments(7);
    }
}
