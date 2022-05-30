/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.business.data.SchemaManager;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Matthieu Chaffotte
 */
public class SchemaManagerUpdate implements SchemaManager {

    private static final Logger log = LoggerFactory.getLogger(SchemaManagerUpdate.class);

    private final Map<String, Object> configuration;

    public SchemaManagerUpdate(final Map<String, Object> configuration)
            throws HibernateException {
        this.configuration = new HashMap<>(configuration);
        final Object remove = this.configuration.remove("hibernate.hbm2ddl.auto");
        if (remove != null && log.isInfoEnabled()) {
            log.info("'hibernate.hbm2ddl.auto' is not a valid property so it has been ignored");
        }
    }

    private Metadata buildConfiguration(final Set<String> managedClasses) {
        MetadataSources metadata = new MetadataSources(
                new StandardServiceRegistryBuilder()
                        .applySettings(configuration)
                        .build());
        for (final String entity : managedClasses) {
            metadata.addAnnotatedClass(getMappedClass(entity));
        }
        return metadata.buildMetadata();
    }

    public Class<?> getMappedClass(final String className) throws MappingException {
        if (className == null) {
            return null;
        }
        try {
            return ReflectHelper.classForName(className);
        } catch (final ClassNotFoundException notFound) {
            throw new MappingException("entity class not found: " + className, notFound);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Exception> drop(final Set<String> managedClasses) {
        log.info("Dropping classes: {}", managedClasses);

        SchemaExport schemaExport = new SchemaExport();
        schemaExport.drop(EnumSet.of(TargetType.DATABASE), buildConfiguration(managedClasses));

        log.info("Drop operation done");
        return schemaExport.getExceptions();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Exception> update(final Set<String> managedClasses) {
        log.info("Updating classes: {}", managedClasses);

        final SchemaUpdate schemaUpdate = new SchemaUpdate();
        schemaUpdate.execute(EnumSet.of(TargetType.DATABASE), buildConfiguration(managedClasses));

        log.info("Update operation done");
        return schemaUpdate.getExceptions();
    }

}
