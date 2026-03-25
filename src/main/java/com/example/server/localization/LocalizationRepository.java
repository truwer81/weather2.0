package com.example.server.localization;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class LocalizationRepository {

    private final SessionFactory sessionFactory;

    public LocalizationRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Localization save(Localization localization) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(localization);
            transaction.commit();
            return localization;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public List<Localization> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Localization", Localization.class).getResultList();
        }
    }

    public Localization findOne(Long localizationId) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(Localization.class, localizationId);
        }
    }
}
