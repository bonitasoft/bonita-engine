/**
 * Copyright (C) 2015-2018 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.business.data.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.bonitasoft.engine.business.data.SchemaManager;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
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
public class SchemaManagerUpdate implements SchemaManager {

    private final TechnicalLogger log;

    private final Map<String, Object> configuration;

    public SchemaManagerUpdate(final Map<String, Object> configuration, final TechnicalLoggerService loggerService) throws HibernateException {
        this.log = loggerService.asLogger(getClass());
        this.configuration = new HashMap<>(configuration);
        final Object remove = this.configuration.remove("hibernate.hbm2ddl.auto");
        if (remove != null && log.isInfoEnabled()) {
            log.info("'hibernate.hbm2ddl.auto' is not a valid property so it has been ignored");
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

    @Override
    @SuppressWarnings("unchecked")
    public List<Exception> drop(final Set<String> managedClasses) {
        log.info("Dropping classes: {}", managedClasses);
        final SchemaExport export = new SchemaExport(buildConfiguration(managedClasses));
        export.drop(Target.EXPORT);
        log.info("Drop operation done");
        return export.getExceptions();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Exception> update(final Set<String> managedClasses) {
        log.info("Updating classes: {}", managedClasses);
        final SchemaUpdate schemaUpdate = new SchemaUpdate(buildConfiguration(managedClasses));
        schemaUpdate.execute(Target.EXPORT);
        log.info("Update operation done");
        return schemaUpdate.getExceptions();
    }

}
