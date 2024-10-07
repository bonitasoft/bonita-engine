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

import static org.bonitasoft.engine.services.Vendor.POSTGRES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.SharedCacheMode;

import lombok.Getter;
import org.bonitasoft.engine.services.Vendor;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.BasicType;

/**
 * @author Charles Souillard
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class HibernateConfigurationProviderImpl implements HibernateConfigurationProvider {

    private final HibernateResourcesConfigurationProvider hibernateResourcesConfigurationProvider;
    protected final Properties properties;
    private final List<String> mappingExclusions;
    @Getter
    protected Vendor vendor;
    @Getter
    private SessionFactory sessionFactory;
    @Getter
    private final List<Class<? extends PersistentObject>> mappedClasses = new ArrayList<>();

    public HibernateConfigurationProviderImpl(final Properties properties,
            final HibernateResourcesConfigurationProvider hibernateResourcesConfigurationProvider,
            final List<String> mappingExclusions) {
        this.properties = properties;
        this.hibernateResourcesConfigurationProvider = hibernateResourcesConfigurationProvider;
        this.mappingExclusions = mappingExclusions;
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

    @Override
    public void bootstrap(Properties extraHibernateProperties) {
        StandardServiceRegistryBuilder standardRegistryBuilder = new StandardServiceRegistryBuilder(
                new BootstrapServiceRegistryBuilder().build());
        Properties allProps = gatherAllProperties(extraHibernateProperties, standardRegistryBuilder);
        StandardServiceRegistry standardRegistry = standardRegistryBuilder.build();

        this.vendor = Vendor.fromHibernateDialectProperty(allProps.getProperty("hibernate.dialect"));
        setCustomHibernateDataTypesAndProperties();

        Metadata metadata = buildHibernateMetadata(standardRegistry);

        this.sessionFactory = applyInterceptors(metadata, allProps).build();

        for (PersistentClass entityBinding : metadata.getEntityBindings()) {
            mappedClasses.add(entityBinding.getMappedClass());
        }
    }

    /**
     * Set custom Hibernate data types using {@link CustomDataTypesRegistration} and set Hibernate system properties.
     */
    protected void setCustomHibernateDataTypesAndProperties() {
        switch (vendor) {
            case POSTGRES -> {
                CustomDataTypesRegistration.addTypeOverride(new PostgresMaterializedBlobType());
                CustomDataTypesRegistration.addTypeOverride(new PostgresMaterializedClobType());
                CustomDataTypesRegistration.addTypeOverride(new PostgresXMLType());
            }
            case OTHER -> CustomDataTypesRegistration.addTypeOverride(new XMLType());
            default -> throw new IllegalStateException("Unsupported vendor: " + vendor);
        }
    }

    /**
     * Build the hibernate metadata from the provided resources and entities.
     *
     * @param standardRegistry the standard registry of services needed to create metadata sources
     * @return the hibernate metadata
     */
    private Metadata buildHibernateMetadata(ServiceRegistry standardRegistry) {
        MetadataSources metadataSources = new MetadataSources(standardRegistry) {

            @Override
            public MetadataBuilder getMetadataBuilder() {
                MetadataBuilder metadataBuilder = super.getMetadataBuilder();
                for (BasicType typeOverride : CustomDataTypesRegistration.getTypeOverrides()) {
                    metadataBuilder.applyBasicType(typeOverride);
                }
                applyCacheMode(metadataBuilder);
                return metadataBuilder;
            }
        };
        metadataSources.addPackage("org.bonitasoft.engine.persistence");
        for (final String resource : hibernateResourcesConfigurationProvider.getResources()) {
            metadataSources.addResource(resource);
        }
        for (Class entity : hibernateResourcesConfigurationProvider.getEntities()) {
            metadataSources.addAnnotatedClass(entity);
        }
        return metadataSources.buildMetadata();
    }

    /**
     * Apply interceptors to a new session factory.
     *
     * @param metadata the hibernate metadata
     * @param allProps the hibernate properties
     * @return a new session factory builder
     * @throws IllegalStateException if an interceptor class is unknown or cannot be instantiated
     */
    protected SessionFactoryBuilder applyInterceptors(Metadata metadata, Properties allProps)
            throws IllegalStateException {
        SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
        final String className = allProps.getProperty("hibernate.interceptor");
        if (className != null && !className.isEmpty()) {
            try {
                sessionFactoryBuilder.applyInterceptor(
                        (Interceptor) Class.forName(className).getDeclaredConstructor().newInstance());
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException("Unknown interceptor class " + className, e);
            }
        }
        if (vendor == POSTGRES) {
            sessionFactoryBuilder.applyInterceptor(new PostgresInterceptor());
        }
        return sessionFactoryBuilder;
    }

    protected void applyCacheMode(MetadataBuilder metadataBuilder) {
        metadataBuilder.applySharedCacheMode(SharedCacheMode.NONE);
    }

    protected Properties gatherAllProperties(Properties extraHibernateProperties,
            StandardServiceRegistryBuilder standardRegistryBuilder) {
        Properties allProps = new Properties();
        allProps.putAll(properties);
        allProps.putAll(extraHibernateProperties);

        for (Map.Entry<Object, Object> prop : allProps.entrySet()) {
            standardRegistryBuilder.applySetting(prop.getKey().toString(), prop.getValue());
        }
        return allProps;
    }

}
