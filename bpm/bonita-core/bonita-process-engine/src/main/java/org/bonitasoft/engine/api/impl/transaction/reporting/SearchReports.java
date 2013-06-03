/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl.transaction.reporting;

import java.util.List;

import org.bonitasoft.engine.core.reporting.ReportingService;
import org.bonitasoft.engine.core.reporting.SReport;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.reporting.Report;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

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
    public long executeCount(final QueryOptions queryOptions) throws SBonitaSearchException {
        return reportingService.getNumberOfReports(queryOptions);
    }

    @Override
    public List<SReport> executeSearch(final QueryOptions queryOptions) throws SBonitaSearchException {
        return reportingService.searchReports(queryOptions);
    }

    @Override
    public List<Report> convertToClientObjects(final List<SReport> reports) {
        return ModelConvertor.toReports(reports);
    }

}
