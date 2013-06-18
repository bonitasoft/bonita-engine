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
package org.bonitasoft.engine.core.reporting;

import java.sql.SQLException;
import java.util.List;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Matthieu Chaffotte
 */
public interface ReportingService {

    String REPORT = "REPORT";

    String selectList(String selectQuery) throws SQLException;

    SReport addReport(SReport report, byte[] content) throws SReportCreationException, SReportAlreadyExistsException;

    SReport getReport(long reportId) throws SBonitaReadException, SReportNotFoundException;

    long getNumberOfReports(QueryOptions options) throws SBonitaSearchException;

    List<SReport> searchReports(QueryOptions options) throws SBonitaSearchException;

    void deleteReport(long reportId) throws SReportDeletionException, SReportNotFoundException;

    SReportBuilder getReportBuilder();

    byte[] getReportContent(long reportId) throws SBonitaReadException, SReportNotFoundException;

}
