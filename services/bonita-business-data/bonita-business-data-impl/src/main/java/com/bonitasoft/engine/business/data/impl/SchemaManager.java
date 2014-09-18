/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.hbm2ddl.Target;

/**
 * @author Matthieu Chaffotte
 */
public class SchemaManager {

    private final TechnicalLoggerService loggerService;

    private final Map<String, Object> configuration;

    public SchemaManager(final Map<String, Object> configuration, final TechnicalLoggerService loggerService) throws HibernateException {
        this.loggerService = loggerService;
        this.configuration = new HashMap<String, Object>(configuration);
        final Object remove = this.configuration.remove("hibernate.hbm2ddl.auto");
        if (remove != null && loggerService.isLoggable(SchemaManager.class, TechnicalLogSeverity.INFO)) {
            this.loggerService.log(SchemaManager.class, TechnicalLogSeverity.INFO, "'hibernate.hbm2ddl.auto' is not a valid property so it has been ignored");
        }
    }

    private Configuration buildConfiguration(final Set<String> managedClasses) {
        final Configuration cfg = new Configuration();
        final Properties properties = new Properties();
        properties.putAll(configuration);
        for (final String entity : managedClasses) {
            cfg.addAnnotatedClass(getMappedClass(entity));
        }
        cfg.setProperties(properties);
        return cfg;
    }

    public Class<?> getMappedClass(final String className) throws MappingException {
        if (className == null) {
            return null;
        }
        try {
            return ReflectHelper.classForName(className);
        } catch (final ClassNotFoundException cnfe) {
            throw new MappingException("entity class not found: " + className, cnfe);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Exception> drop(final Set<String> managedClasses) {
        final SchemaExport export = new SchemaExport(buildConfiguration(managedClasses));
        export.drop(Target.EXPORT);
        return export.getExceptions();
    }

    @SuppressWarnings("unchecked")
    public List<Exception> update(final Set<String> managedClasses) {
        final SchemaUpdate schemaUpdate = new SchemaUpdate(buildConfiguration(managedClasses));
        schemaUpdate.execute(Target.EXPORT);
        return schemaUpdate.getExceptions();
    }

}
