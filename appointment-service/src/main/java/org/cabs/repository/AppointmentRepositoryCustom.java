package org.cabs.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.List;
import org.cabs.entity.Appointment;
import org.springframework.stereotype.Repository;

@Repository
public class AppointmentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Appointment> getUserAppt(Integer userId, String predicate, String sort) {
        String sql =
            "SELECT * FROM cabs.appointment A WHERE A.patient_id = :userId ORDER BY " + predicate
                + " " + sort;

        Query query = entityManager.createNativeQuery(sql, Appointment.class);
        query.setParameter("userId", userId);

        return query.getResultList();
    }
}
