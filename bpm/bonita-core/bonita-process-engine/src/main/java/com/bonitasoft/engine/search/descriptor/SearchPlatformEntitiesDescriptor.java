/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.search.descriptor;

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
