/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.persistence;

import java.util.Map;

import org.bonitasoft.engine.persistence.HibernateResourcesConfigurationProviderImpl;

/**
 * @author Celine Souchet
 */
public class HibernateResourcesConfigurationProviderExt extends HibernateResourcesConfigurationProviderImpl {

    protected final Map<String, String> cacheConcurrencyStrategies;

    public HibernateResourcesConfigurationProviderExt(final Map<String, String> cacheConcurrencyStrategies) {
        super();
        this.cacheConcurrencyStrategies = cacheConcurrencyStrategies;
    }

    @Override
    public Map<String, String> getCacheConcurrencyStrategies() {
        return cacheConcurrencyStrategies;
    }
}
