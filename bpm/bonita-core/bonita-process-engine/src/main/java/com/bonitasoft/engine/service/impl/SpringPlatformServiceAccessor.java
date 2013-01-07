/*
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.service.impl;

import org.bonitasoft.engine.service.impl.SpringPlatformFileSystemBeanAccessor;

import com.bonitasoft.engine.search.SearchPlatformEntitiesDescriptor;
import com.bonitasoft.engine.service.PlatformServiceAccessor;

/**
 * @author Matthieu Chaffotte
 */
public class SpringPlatformServiceAccessor extends org.bonitasoft.engine.service.impl.SpringPlatformServiceAccessor implements PlatformServiceAccessor {

    private SearchPlatformEntitiesDescriptor searchPlatformEntitiesDescriptor;

    @Override
    public SearchPlatformEntitiesDescriptor getSearchPlatformEntitiesDescriptor() {
        if (searchPlatformEntitiesDescriptor == null) {
            searchPlatformEntitiesDescriptor = SpringPlatformFileSystemBeanAccessor.getService(SearchPlatformEntitiesDescriptor.class);
        }
        return searchPlatformEntitiesDescriptor;
    }

}
