/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;

import com.bonitasoft.engine.business.data.BusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.BusinessDataRespository;
import com.bonitasoft.engine.business.data.NonUniqueResultException;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessDataRepositoryImpl implements BusinessDataRespository {

    SessionFactory sessionFactory;

    @Override
    public void start() {
        final Configuration cfg = new Configuration().configure();
        sessionFactory = cfg.buildSessionFactory();
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> T find(final Class<T> entityClass, final Serializable primaryKey) throws BusinessDataNotFoundException {
        final Session session = sessionFactory.openSession();
        try {
            final T entity = (T) session.get(entityClass, primaryKey, LockOptions.READ);
            if (entity == null) {
                throw new BusinessDataNotFoundException("Impossible to get data with id: " + primaryKey);
            }
            return entity;
        } finally {
            session.close();
        }
    }

    @Override
    public <T> T find(final Class<T> resultClass, final String qlString, final Map<String, Object> parameters) throws BusinessDataNotFoundException,
            NonUniqueResultException {
        final Session session = sessionFactory.openSession();
        try {
            final Query query = session.createQuery(qlString);
            if (parameters != null) {
                for (final Entry<String, Object> parameter : parameters.entrySet()) {
                    query.setParameter(parameter.getKey(), parameter.getValue());
                }
            }
            try {
                final T entity = (T) query.uniqueResult();
                if (entity == null) {
                    throw new BusinessDataNotFoundException("Impossible to get data using query: " + qlString + " and parameters: " + parameters);
                }
                return entity;
            } catch (final org.hibernate.NonUniqueResultException nure) {
                throw new NonUniqueResultException(nure);
            }
        } finally {
            session.close();
        }
    }

    @Override
    public void persist(final Object entity) {
        if (entity == null) {
            return;
        }
        final Session session = sessionFactory.openSession();
        try {
            session.save(entity);
        } finally {
            session.close();
        }
    }

    @Override
    public void remove(final Object entity) {
        // TODO Auto-generated method stub

    }

}
