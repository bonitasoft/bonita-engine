/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.search;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.search.SearchOptions;

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
    public long executeCount(final QueryOptions searchOptions) throws SBonitaSearchException {
        return platformService.getNumberOfTenants(searchOptions);
    }

    @Override
    public List<STenant> executeSearch(final QueryOptions searchOptions) throws SBonitaSearchException {
        return platformService.searchTenants(searchOptions);
    }

}
