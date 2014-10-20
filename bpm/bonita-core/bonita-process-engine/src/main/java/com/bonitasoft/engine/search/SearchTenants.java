/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.search;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.search.SearchOptions;

import com.bonitasoft.engine.search.descriptor.SearchTenantDescriptor;

/**
 * @author Zhao Na
 */
public class SearchTenants extends AbstractTenantSearchEntity {

    private final PlatformService platformService;

    public SearchTenants(final PlatformService platformService, final SearchTenantDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.platformService = platformService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return platformService.getNumberOfTenants(searchOptions);
    }

    @Override
    public List<STenant> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return platformService.searchTenants(searchOptions);
    }

}
