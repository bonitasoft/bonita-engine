/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
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
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.reporting.Report;
import com.bonitasoft.engine.reporting.ReportCreator;
import com.bonitasoft.engine.reporting.ReportNotFoundException;

/**
 * This API gives access to all reporting features. Reporting is a way to execute custom queries on Bonita BPM Database, knowing the DB structure, for custom
 * specific queries.
 * <p>
 * Only "select" statements are allowed.
 * </p>
 * <p>
 * Also allows to manipulate <code>Report</code>s, through creation, deletion, search.
 * </p>
 * 
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 * @see Report
 */
public interface ReportingAPI {

    /**
     * Executes a SELECT query (rollback only) and returns the result in a CSV format.
     * <p>
     * If the query is not a SELECT one a RetrieveException is thrown. The result can differ from a database to another one due to the way the database stores
     * values. (for example: with a boolean it can be a number for some databases)
     * 
     * @param selectQuery
     *            the SELECT query.
     * @return the result of the query in a CSV format.
     * @throws ExecutionException
     *             if an exception occurs during the execution of the query.
     */
    String selectList(String selectQuery) throws ExecutionException;

    /**
     * Retrieves a report from its ID.
     * 
     * @param reportId
     *            the Identifier of the report to retrieve
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
     * Creates a new report with no screenshot.
     * 
     * @param name
     *            the name of the report.
     * @param description
     *            the description of the report, ready to be displayed.
     * @param content
     *            the binary content of the report.
     * @return the newly created report.
     * @throws AlreadyExistsException
     *             if a report with this name already exists.
     * @throws CreationException
     *             if an error occurs during the creation.
     * @see #createReport(ReportCreator, byte[])
     */
    Report createReport(String name, final String description, byte[] content) throws AlreadyExistsException, CreationException;

    /**
     * Creates a custom report.
     * 
     * @param reportCreator
     *            the creator object to instantiate the new report.
     * @param content
     *            the binary content of the report.
     * @return the newly created report.
     * @throws AlreadyExistsException
     *             if a report with this name already exists.
     * @throws CreationException
     *             if an error occurs during the creation.
     */
    Report createReport(ReportCreator reportCreator, final byte[] content) throws AlreadyExistsException, CreationException;

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
