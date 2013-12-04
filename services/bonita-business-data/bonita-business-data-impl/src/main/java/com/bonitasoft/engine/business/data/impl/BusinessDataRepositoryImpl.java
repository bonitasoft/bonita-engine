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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.ConnectionProviderFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import com.bonitasoft.engine.business.data.BusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.BusinessDataRespository;
import com.bonitasoft.engine.business.data.NonUniqueResultException;

/**
 * @author Matthieu Chaffotte
 */
public class BusinessDataRepositoryImpl implements BusinessDataRespository {

    public enum Strategy {
        DROP_CREATE, UPDATE;
    }

    private SessionFactory sessionFactory;

    private final Strategy strategy;

    public BusinessDataRepositoryImpl() {
        strategy = Strategy.UPDATE;
    }

    public BusinessDataRepositoryImpl(final Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void start() {
        final Configuration cfg = new Configuration().configure();
        cfg.getProperties().remove("hibernate.hbm2ddl.auto");
        sessionFactory = cfg.buildSessionFactory();

        final Dialect dialect = Dialect.getDialect(cfg.getProperties());
        if (strategy == Strategy.UPDATE) {
            updateSchema(cfg, dialect);
        } else {
            dropCreateSchema(cfg, dialect);
        }
    }

    private void dropCreateSchema(final Configuration cfg, final Dialect dialect) {
        final String[] dropScript = cfg.generateDropSchemaScript(dialect);
        final String[] createScript = cfg.generateSchemaCreationScript(dialect);
        final String[] script = ArrayUtils.addAll(dropScript, createScript);
        executeQueries(script);
    }

    private void updateSchema(final Configuration cfg, final Dialect dialect) {
        final ConnectionProvider connectionProvider = ConnectionProviderFactory.newConnectionProvider(cfg.getProperties());
        try {
            final Connection connection = connectionProvider.getConnection();
            try {
                final DatabaseMetadata meta = new DatabaseMetadata(connection, dialect);
                executeQueries(cfg.generateSchemaUpdateScript(dialect, meta));
            } finally {
                connection.close();
            }
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            connectionProvider.close();
        }
    }

    private void executeQueries(final String... sqlQuerys) {
        final Session session = getSession();
        try {
            for (final String sqlQuery : sqlQuerys) {
                final SQLQuery query = session.createSQLQuery(sqlQuery);
                query.executeUpdate();
            }
        } finally {
            session.close();
        }
    }

    @Override
    public void stop() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }

    @Override
    public <T> T find(final Class<T> entityClass, final Serializable primaryKey) throws BusinessDataNotFoundException {
        final Session session = getSession();
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
        final Session session = getSession();
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

    private Session getSession() {
        if (sessionFactory == null) {
            throw new IllegalStateException("The BDR is not started");
        }
        return sessionFactory.openSession();
    }

    @Override
    public void persist(final Object entity) {
        if (entity == null) {
            return;
        }
        final Session session = getSession();
        try {
            session.save(entity);
        } finally {
            session.close();
        }
    }

}
