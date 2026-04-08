package org.cabs.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.List;
import org.cabs.entity.Appointment;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Appointment> getTodayApptQueue() {
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
    public Integer updateQueueStatus(Integer id, Integer status) {
        String sql = "UPDATE cabs.appointment " + "SET status = :status " + "WHERE id = :id ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("status", status);
        query.setParameter("id", id);

        return query.executeUpdate();
    }
}
