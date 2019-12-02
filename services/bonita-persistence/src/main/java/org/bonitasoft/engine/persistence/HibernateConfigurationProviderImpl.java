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
package org.bonitasoft.engine.persistence;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.services.Vendor;
import org.hibernate.cfg.Configuration;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class HibernateConfigurationProviderImpl implements HibernateConfigurationProvider {

    private final HibernateResourcesConfigurationProvider hibernateResourcesConfigurationProvider;
    protected final Properties properties;
    private final List<String> mappingExclusions;
    protected final Configuration configuration;

    public HibernateConfigurationProviderImpl(final Properties properties,
                                              final HibernateResourcesConfigurationProvider hibernateResourcesConfigurationProvider,
                                              final List<String> mappingExclusions) {
        this.properties = properties;
        this.hibernateResourcesConfigurationProvider = hibernateResourcesConfigurationProvider;

        configuration = buildConfiguration(properties, hibernateResourcesConfigurationProvider);
        this.mappingExclusions = mappingExclusions;
    }

    protected Configuration buildConfiguration(final Properties properties, final HibernateResourcesConfigurationProvider hibernateResourcesConfigurationProvider) {
        final Configuration configuration = new Configuration();
        configuration.addProperties(properties);
        Vendor vendor = Vendor.fromHibernateConfiguration(configuration);

        //register type before loading mappings/entities, type should be present before loading JPA entities
        switch (vendor) {
            case ORACLE:
            case MYSQL:
            case OTHER:
                configuration.registerTypeOverride(XMLType.INSTANCE);
                break;
            case SQLSERVER:
                configuration.setInterceptor(new SQLServerInterceptor());
                configuration.registerTypeOverride(XMLType.INSTANCE);
                break;
            case POSTGRES:
                configuration.setInterceptor(new PostgresInterceptor());
                configuration.registerTypeOverride(new PostgresMaterializedBlobType());
                configuration.registerTypeOverride(new PostgresMaterializedClobType());
                configuration.registerTypeOverride(PostgresXMLType.INSTANCE);
                break;
        }
        for (final String resource : hibernateResourcesConfigurationProvider.getResources()) {
            configuration.addResource(resource);
        }
        for (Class entity : hibernateResourcesConfigurationProvider.getEntities()) {
            configuration.addAnnotatedClass(entity);
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
    public List<String> getMappingExclusions() {
        return Collections.unmodifiableList(mappingExclusions);
    }

    @Override
    public Map<String, String> getCacheQueries() {
        return null;
    }

}
