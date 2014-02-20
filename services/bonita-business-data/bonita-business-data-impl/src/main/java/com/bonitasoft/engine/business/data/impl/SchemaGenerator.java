/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.jdbc.Work;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;

/**
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 */
public class SchemaGenerator {

    /**
     * @author Emmanuel Duchastenier
     */
    private final class ScriptGeneratorWork implements Work {

        private String[] scripts;

        @Override
        public void execute(final Connection connection) throws SQLException {
            final DatabaseMetadata databaseMetadata = new DatabaseMetadata(connection, dialect);
            scripts = cfg.generateSchemaUpdateScript(dialect, databaseMetadata);
        }

        /**
         * @return the scripts
         */
        public String[] getScripts() {
            return scripts;
        }
    }

    private final Configuration cfg;

    private final Dialect dialect;

    private final EntityManager entityManager;

    public SchemaGenerator(final EntityManager entityManager, final Properties properties, final List<String> classNameList)
            throws SBusinessDataRepositoryDeploymentException {
        this.entityManager = entityManager;
        cfg = new Configuration();
        cfg.setProperties(properties);
        cfg.setProperty("hibernate.hbm2ddl.auto", "update");
        cfg.setProperty("hibernate.current_session_context_class", "jta");
        cfg.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JTATransactionFactory");
        cfg.setProperty("hibernate.transaction.manager_lookup_class", "org.hibernate.transaction.BTMTransactionManagerLookup");
        dialect = Dialect.getDialect(properties);
        for (final String className : classNameList) {
            Class<?> annotatedClass;
            try {
                annotatedClass = Thread.currentThread().getContextClassLoader().loadClass(className);
                cfg.addAnnotatedClass(annotatedClass);
            } catch (final ClassNotFoundException e) {
                throw new SBusinessDataRepositoryDeploymentException(e);
            }
        }
    }

    /**
     * Method that actually creates the file.
     * 
     * @param dbDialect
     *            to use
     * @throws SQLException
     */
    public String[] generate() throws SQLException {
        ScriptGeneratorWork work = new ScriptGeneratorWork();
        ((Session) entityManager.getDelegate()).doWork(work);
        return work.getScripts();
    }

}
