/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.persistence.HibernateConfigurationProviderImpl;
import org.bonitasoft.engine.persistence.HibernateResourcesConfigurationProvider;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Celine Souchet
 */
public class HibernateConfigurationProviderExt extends HibernateConfigurationProviderImpl {

    private final Map<String, String> cacheQueries;

    public HibernateConfigurationProviderExt(final Properties properties,
            final HibernateResourcesConfigurationProvider hibernateResourcesConfigurationProvider,
            final Map<String, String> interfaceToClassMapping,
            final List<String> mappingExclusions,
            final Map<String, String> cacheQueries)
            throws SPersistenceException {
        super(properties, hibernateResourcesConfigurationProvider, interfaceToClassMapping, mappingExclusions);
        this.cacheQueries = cacheQueries;
    }

    @Override
    public Map<String, String> getCacheQueries() {
        return this.cacheQueries;
    }
}
