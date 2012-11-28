/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.search;

import java.util.List;

import org.bonitasoft.engine.businesslogger.model.SBusinessLog;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.services.BusinessLoggerService;

/**
 * @author Julien Mege
 */
public class SearchLogs extends AbstractLogSearchEntity {

    private final BusinessLoggerService businessLoggerService;

    public SearchLogs(final BusinessLoggerService businessLoggerService, final SearchLogDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.businessLoggerService = businessLoggerService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaSearchException {
        return businessLoggerService.getNumberOfLogs(searchOptions);
    }

    @Override
    public List<SBusinessLog> executeSearch(final QueryOptions searchOptions) throws SBonitaSearchException {
        return businessLoggerService.searchLogs(searchOptions);
    }

}
