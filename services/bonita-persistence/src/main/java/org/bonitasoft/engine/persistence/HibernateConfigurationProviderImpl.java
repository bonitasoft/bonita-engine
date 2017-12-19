/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.services.SPersistenceException;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class HibernateConfigurationProviderImpl implements HibernateConfigurationProvider {

    protected final HibernateResourcesConfigurationProvider hibernateResourcesConfigurationProvider;

    protected final Properties properties;

    protected final Map<String, Class<? extends PersistentObject>> interfaceToClassMapping;

    protected final List<String> mappingExclusions;

    protected final Configuration configuration;

    public HibernateConfigurationProviderImpl(final Properties properties,
            final HibernateResourcesConfigurationProvider hibernateResourcesConfigurationProvider, final Map<String, String> interfaceToClassMapping,
            final List<String> mappingExclusions) throws SPersistenceException {
        this.properties = properties;
        this.hibernateResourcesConfigurationProvider = hibernateResourcesConfigurationProvider;

        configuration = buildConfiguration(properties, hibernateResourcesConfigurationProvider);
        final Iterator<PersistentClass> it = configuration.getClassMappings();

        final StringBuilder sb = new StringBuilder();
        sb.append("\n\nFOUND MAPPING FOR CLASS: \n");
        while (it.hasNext()) {
            sb.append(it.next().getEntityName());
            sb.append("\n");
        }
        sb.append("\n");

        this.interfaceToClassMapping = new HashMap<String, Class<? extends PersistentObject>>();
        for (final Map.Entry<String, String> entry : interfaceToClassMapping.entrySet()) {
            final String interfaceClassName = entry.getKey();
            final String mappedClassName = entry.getValue();

            PersistentClass persistentClass = configuration.getClassMapping(mappedClassName);
            if (persistentClass == null) {
                // try to keep only the simpleName if the class which is sometimes used as "entity-name" in the mapping file
                final String classSimpleName = mappedClassName.substring(mappedClassName.lastIndexOf('.') + 1);
                persistentClass = configuration.getClassMapping(classSimpleName);
                if (persistentClass == null) {
                    throw new SPersistenceException("Unable to locate an hibernate mapping file for class: " + mappedClassName + ", found mappings are: " + sb.toString());
                }
            }
            final Class<? extends PersistentObject> mappedClass = persistentClass.getMappedClass();
            this.interfaceToClassMapping.put(interfaceClassName, mappedClass);
        }
        this.mappingExclusions = mappingExclusions;
    }

    protected Configuration buildConfiguration(final Properties properties, final HibernateResourcesConfigurationProvider hibernateResourcesConfigurationProvider) {
        final Configuration configuration = new Configuration();
        configuration.addProperties(properties);
        for (final String resource : hibernateResourcesConfigurationProvider.getResources()) {
            configuration.addResource(resource);
        }
        configuration.buildMappings();
        return configuration;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public HibernateResourcesConfigurationProvider getResources() {
        return hibernateResourcesConfigurationProvider;
    }

    @Override
    public Map<String, String> getClassAliasMappings() {
        return hibernateResourcesConfigurationProvider.getClassAliasMappings();
    }

    @Override
    public Map<String, Class<? extends PersistentObject>> getInterfaceToClassMapping() {
        return Collections.unmodifiableMap(interfaceToClassMapping);
    }

    @Override
    public List<String> getMappingExclusions() {
        return Collections.unmodifiableList(mappingExclusions);
    }

    @Override
    public Map<String, String> getCacheQueries() {
        return null;
    }

}
