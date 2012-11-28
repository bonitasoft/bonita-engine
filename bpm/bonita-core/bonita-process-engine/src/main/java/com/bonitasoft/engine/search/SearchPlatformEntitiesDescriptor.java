/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.search;

import org.bonitasoft.engine.platform.model.builder.STenantBuilder;

/**
 * @author Zhao Na
 */
public final class SearchPlatformEntitiesDescriptor {

    private final SearchTenantDescriptor searchTenantDescriptor;

    public SearchPlatformEntitiesDescriptor(final STenantBuilder tenantBuilder) {
        searchTenantDescriptor = new SearchTenantDescriptor(tenantBuilder);
    }

    public SearchTenantDescriptor getSearchTenantDescriptor() {
        return searchTenantDescriptor;
    }

}
