/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
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

    /**
     * @param reportId
     * @return the found report
     * @throws ReportNotFoundException
     *             if no report can be found with the provided ID.
     */
    Report getReport(long reportId) throws ReportNotFoundException;

    /**
     * Retrieves the binary content of a report.
     * 
     * @param reportId
     *            the ID of the report to extract the content for.
     * @return
     *         the binary content of the report.
     * @throws ReportNotFoundException
     *             if no report can be found with the provided ID.
     */
    byte[] getReportContent(long reportId) throws ReportNotFoundException;

    /**
     * Searches for reports with specific search criteria.
     * 
     * @param searchOptions
     *            the search options for the search. See {@link SearchOptions} for search option details.
     * @return the <code>SearchResult</code> containing
     * @throws SearchException
     *             if a problem occurs during the search.
     */
    SearchResult<Report> searchReports(SearchOptions searchOptions) throws SearchException;

    /**
     * Creates a new report.
     * 
     * @param name
     *            the name of the report
     * @param description
     *            the description of the report, ready to be displayed
     * @param content
     *            the binary content of the report
     * @return the newly created report
     * @throws AlreadyExistsException
     *             if a report with this name already exists.
     * @throws CreationException
     *             if an error occurs during the creation.
     */
    Report createReport(String name, final String description, byte[] content) throws AlreadyExistsException, CreationException;

    /**
     * Deletes a report identified by its ID.
     * 
     * @param reportId
     *            the report identifier to delete.
     * @throws DeletionException
     *             if a problem occurs during deletion.
     */
    void deleteReport(long reportId) throws DeletionException;

    /**
     * Deletes a list of reports, given by their IDs.
     * 
     * @param reportIds
     *            a list of report identifiers to delete.
     * @throws DeletionException
     *             if a problem occurs during deletion.
     */
    void deleteReports(List<Long> reportIds) throws DeletionException;

}
