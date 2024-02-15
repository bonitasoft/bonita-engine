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

import static org.bonitasoft.engine.services.Vendor.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.SharedCacheMode;

import org.bonitasoft.engine.services.Vendor;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.PersistentClass;
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
    private Vendor vendor;
    private SessionFactory sessionFactory;
    private List<Class<? extends PersistentObject>> mappedClasses = new ArrayList<>();

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
        BootstrapServiceRegistryBuilder bootstrapRegistryBuilder = new BootstrapServiceRegistryBuilder();
        BootstrapServiceRegistry bootstrapRegistry = bootstrapRegistryBuilder.build();
        StandardServiceRegistryBuilder standardRegistryBuilder = new StandardServiceRegistryBuilder(bootstrapRegistry);
        Properties allProps = gatherAllProperties(extraHibernateProperties, standardRegistryBuilder);
        this.vendor = Vendor.fromHibernateDialectProperty(allProps.getProperty("hibernate.dialect"));
        StandardServiceRegistry standardRegistry = standardRegistryBuilder.build();
        switch (vendor) {
            case ORACLE:
            case SQLSERVER:
            case MYSQL:
                System.setProperty("hibernate.dialect.storage_engine", "innodb");
            case OTHER:
                CustomDataTypesRegistration.addTypeOverride(new XMLType());
                break;
            case POSTGRES:
                CustomDataTypesRegistration.addTypeOverride(new PostgresMaterializedBlobType());
                CustomDataTypesRegistration.addTypeOverride(new PostgresMaterializedClobType());
                CustomDataTypesRegistration.addTypeOverride(new PostgresXMLType());
                break;
        }

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

        Metadata metadata = metadataSources.buildMetadata();
        SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
        final String className = allProps.getProperty("hibernate.interceptor");
        if (className != null && !className.isEmpty()) {
            try {
                final Interceptor interceptor = (Interceptor) Class.forName(className).newInstance();
                sessionFactoryBuilder.applyInterceptor(interceptor);
            } catch (final ClassNotFoundException | IllegalAccessException | InstantiationException cnfe) {
                throw new IllegalStateException("Unknown interceptor class " + className, cnfe);
            }
        }
        if (vendor == POSTGRES) {
            sessionFactoryBuilder.applyInterceptor(new PostgresInterceptor());
        }
        if (vendor == SQLSERVER) {
            sessionFactoryBuilder.applyInterceptor(new SQLServerInterceptor());
        }
        if (vendor == ORACLE) {
            sessionFactoryBuilder.applyInterceptor(new OracleInterceptor());
        }
        this.sessionFactory = sessionFactoryBuilder.build();
        for (PersistentClass entityBinding : metadata.getEntityBindings()) {
            mappedClasses.add(entityBinding.getMappedClass());
        }
    }

    protected void applyCacheMode(MetadataBuilder metadataBuilder) {
        metadataBuilder.applySharedCacheMode(SharedCacheMode.NONE);
    }

    protected Properties gatherAllProperties(Properties extraHibernateProperties,
            StandardServiceRegistryBuilder standardRegistryBuilder) {
        Properties allProps = new Properties();
        for (Map.Entry<Object, Object> extraProp : properties.entrySet()) {
            allProps.put(extraProp.getKey(), extraProp.getValue());
        }
        for (Map.Entry<Object, Object> extraProp : extraHibernateProperties.entrySet()) {
            allProps.put(extraProp.getKey(), extraProp.getValue());
        }

        for (Map.Entry<Object, Object> prop : allProps.entrySet()) {
            standardRegistryBuilder.applySetting(prop.getKey().toString(), prop.getValue());
        }
        return allProps;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public List<Class<? extends PersistentObject>> getMappedClasses() {
        return mappedClasses;
    }

}
