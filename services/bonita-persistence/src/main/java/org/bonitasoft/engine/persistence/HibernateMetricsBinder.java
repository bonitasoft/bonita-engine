package org.bonitasoft.engine.persistence;

import org.hibernate.SessionFactory;

public interface HibernateMetricsBinder {

    void bindMetrics(SessionFactory sessionFactory);
}
