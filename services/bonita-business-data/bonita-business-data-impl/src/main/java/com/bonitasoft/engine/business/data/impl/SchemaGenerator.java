package com.bonitasoft.engine.business.data.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.ConnectionProviderFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;

/**
 * @author Romain Bioteau
 */
public class SchemaGenerator {

    private final Configuration cfg;

    private final Dialect dialect;

    public SchemaGenerator(final Dialect dialect, final Properties properties, final List<String> classNameList)
            throws SBusinessDataRepositoryDeploymentException {
        cfg = new Configuration();
        cfg.setProperties(properties);
        cfg.setProperty("hibernate.hbm2ddl.auto", "update");
        cfg.setProperty("hibernate.current_session_context_class", "jta");
        cfg.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JTATransactionFactory");
        cfg.setProperty("hibernate.transaction.manager_lookup_class", "org.hibernate.transaction.BTMTransactionManagerLookup");
        this.dialect = dialect;
        cfg.setProperty("hibernate.dialect", dialect.getClass().getName());
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
        final ConnectionProvider connectionProvider = ConnectionProviderFactory.newConnectionProvider(cfg.getProperties());
        final Connection connection = connectionProvider.getConnection();
        final DatabaseMetadata databaseMetadata = new DatabaseMetadata(connection, dialect);
        return cfg.generateSchemaUpdateScript(dialect, databaseMetadata);
    }

}
