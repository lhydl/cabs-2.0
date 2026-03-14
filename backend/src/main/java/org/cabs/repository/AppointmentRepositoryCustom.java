package org.cabs.repository;

import org.cabs.domain.Appointment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Appointment> getUserAppt(Integer userId, String predicate, String sort) {
        String sql = "SELECT * FROM cabs.appointment A WHERE A.patient_id = :userId ORDER BY " + predicate + " " + sort;

        Query query = entityManager.createNativeQuery(sql, Appointment.class);
        query.setParameter("userId", userId);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Appointment> getTodaysAppointments() {
        String sql =
            " SELECT * " +
            " FROM " +
            "     cabs.appointment " +
            " WHERE " +
            "     DATE(appt_datetime) = CURDATE() AND STATUS = 0" +
            " ORDER BY " +
            "     appt_datetime ASC ";

        Query query = entityManager.createNativeQuery(sql, Appointment.class);

        return query.getResultList();
    }

    @Transactional
    public Integer updateApptStatus(Integer id, Integer status) {
        String sql = "UPDATE cabs.appointment " + "SET status = :status " + "WHERE id = :id ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("status", status);
        query.setParameter("id", id);

        return query.executeUpdate();
    }
}
