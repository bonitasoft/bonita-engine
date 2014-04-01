/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.hibernate.tool.hbm2ddl.SchemaUpdateScript;

/**
 * @author Matthieu Chaffotte
 */
public class SchemaUpdater {

    private final TechnicalLoggerService loggerService;

    private final Map<String, Object> configuration;

    private final List<Exception> exceptions = new ArrayList<Exception>();

    public SchemaUpdater(final Map<String, Object> configuration, final TechnicalLoggerService loggerService) throws HibernateException {
        this.loggerService = loggerService;
        this.configuration = configuration;
        final Object remove = this.configuration.remove("hibernate.hbm2ddl.auto");
        if (remove != null && loggerService.isLoggable(JPABusinessDataRepositoryImpl.class, TechnicalLogSeverity.INFO)) {
            this.loggerService.log(JPABusinessDataRepositoryImpl.class, TechnicalLogSeverity.INFO,
                    "'hibernate.hbm2ddl.auto' is not a valid property so it has been ignored");
        }
    }

    public void execute(Set<Class<?>> annotatedClasses) {
        exceptions.clear();
        final Configuration cfg = new Configuration();
        final Properties properties = new Properties();
        properties.putAll(configuration);
        
        for (final Class<?> entity : annotatedClasses) {
            cfg.addAnnotatedClass(entity);
        }
        
        cfg.setProperties(properties);
        
        Dialect dialect = Dialect.getDialect(properties);
        final Properties props = new Properties();
        props.putAll(dialect.getDefaultProperties());
        props.putAll(properties);
        final StandardServiceRegistryImpl serviceRegistry = createServiceRegistry(props);

        Connection connection = null;
        DatabaseMetadata meta;
        try {
            try {
                connection = serviceRegistry.getService(ConnectionProvider.class).getConnection();
                meta = new DatabaseMetadata(connection, dialect, cfg);
            } catch (final SQLException sqle) {
                exceptions.add(sqle);
                throw sqle;
            }

            if (loggerService.isLoggable(SchemaUpdater.class, TechnicalLogSeverity.INFO)) {
                loggerService.log(SchemaUpdater.class, TechnicalLogSeverity.INFO, "Updating schema");
            }

            final List<SchemaUpdateScript> scripts = cfg.generateSchemaUpdateScriptList(dialect, meta);
            executeScripts(connection, scripts);

            if (loggerService.isLoggable(SchemaUpdater.class, TechnicalLogSeverity.INFO)) {
                loggerService.log(SchemaUpdater.class, TechnicalLogSeverity.INFO, "Schema updated");
            }
        } catch (final Exception e) {
            exceptions.add(e);
        } finally {
            try {
                if (connection != null) {
                    // Sybase fails if not called
                    connection.clearWarnings();
                    connection.close();
                }
                serviceRegistry.destroy();
            } catch (final Exception e) {
                exceptions.add(e);
            }
        }
    }

    private StandardServiceRegistryImpl createServiceRegistry(final Properties properties) {
        Environment.verifyProperties(properties);
        ConfigurationHelper.resolvePlaceHolders(properties);
        return (StandardServiceRegistryImpl) new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();
    }

    private void executeScripts(final Connection connection, final List<SchemaUpdateScript> scripts) throws SQLException {
        if (scripts != null && !scripts.isEmpty()) {
            final Statement statement = connection.createStatement();
            for (final SchemaUpdateScript script : scripts) {
                if (loggerService.isLoggable(SchemaUpdater.class, TechnicalLogSeverity.DEBUG)) {
                    loggerService.log(SchemaUpdater.class, TechnicalLogSeverity.DEBUG, "Executing script: " + script.getScript());
                }

                try {
                    statement.executeUpdate(script.getScript());
                } catch (final SQLException e) {
                    if (loggerService.isLoggable(SchemaUpdater.class, TechnicalLogSeverity.WARNING)) {
                        loggerService.log(SchemaUpdater.class, TechnicalLogSeverity.WARNING, "Unsuccessful execution of script: " + script.getScript());
                    }
                    if (!script.isQuiet()) {
                        exceptions.add(e);
                    }
                }
            }
            statement.close();
        }
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

}
