/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.reporting;

import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

import com.bonitasoft.engine.core.reporting.ReportingService;
import com.bonitasoft.engine.core.reporting.SReport;
import com.bonitasoft.engine.reporting.Report;
import com.bonitasoft.engine.service.SPModelConvertor;

/**
 * @author Matthieu Chaffotte
 */
public class SearchReports extends AbstractSearchEntity<Report, SReport> {

    private final ReportingService reportingService;

    public SearchReports(final ReportingService reportingService, final SearchEntityDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
        this.reportingService = reportingService;
    }

    @Override
    public long executeCount(final QueryOptions queryOptions) throws SBonitaReadException {
        return reportingService.getNumberOfReports(queryOptions);
    }

    @Override
    public List<SReport> executeSearch(final QueryOptions queryOptions) throws SBonitaReadException {
        return reportingService.searchReports(queryOptions);
    }

    @Override
    public List<Report> convertToClientObjects(final List<SReport> reports) {
        return SPModelConvertor.toReports(reports);
    }

}
