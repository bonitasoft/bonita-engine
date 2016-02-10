/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
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
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.services.QueriableLoggerService;

import com.bonitasoft.engine.search.descriptor.SearchLogDescriptor;

/**
 * @author Julien Mege
 */
public class SearchLogs extends AbstractLogSearchEntity {

    private final QueriableLoggerService queriableLoggerService;

    public SearchLogs(final QueriableLoggerService queriableLoggerService, final SearchLogDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return queriableLoggerService.getNumberOfLogs(searchOptions);
    }

    @Override
    public List<SQueriableLog> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return queriableLoggerService.searchLogs(searchOptions);
    }

}
