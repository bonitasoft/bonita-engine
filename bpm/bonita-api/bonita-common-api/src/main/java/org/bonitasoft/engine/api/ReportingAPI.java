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
package org.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.reporting.Report;
import org.bonitasoft.engine.reporting.ReportNotFoundException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ReportingAPI {

    /**
     * Executes a SELECT query (rollback only) and returns the result in a CSV format.
     * <p>
     * If the query is not a SELECT one a RetrieveException is thrown.
     * 
     * @param selectQuery
     *            the SELECT query.
     * @return the result of the query in a CSV format.
     * @throws ExecutionException
     *             if an exception occurs during the execution of the query.
     */
    String selectList(String selectQuery) throws ExecutionException;

    Report getReport(long reportId) throws ReportNotFoundException;

    byte[] getReportContent(long reportId) throws ReportNotFoundException;

    SearchResult<Report> searchReports(SearchOptions searchOptions) throws SearchException;

}
