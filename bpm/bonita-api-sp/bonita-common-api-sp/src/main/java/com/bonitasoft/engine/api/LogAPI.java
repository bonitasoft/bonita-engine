/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.LogCriterion;
import com.bonitasoft.engine.log.LogNotFoundException;

/**
 * When some actions are performed in Bonita BPM Execution Engine, some business logs are stored.<br>
 * The LogAPI allows to get / search those logs.
 * <p>
 * The stored logs depend on which bonita objects have been enabled for logging (this is configurable in bonita-home configuration files)
 * </p>
 * 
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @see "QueriableLoggerService implementations and configuration (in bonita-home)"
 */
public interface LogAPI {

    /**
     * Retrieves the log.
     * 
     * @param logId
     *            the identifier of the log
     * @return the role
     * @throws LogNotFoundException
     *             If the log identifier does not refer to an existing log
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the role retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    Log getLog(long logId) throws LogNotFoundException;

    /**
     * Returns the total number of logs.
     * 
     * @return the total number of logs
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the count retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    int getNumberOfLogs();

    /**
     * Retrieves the paginated list of logs.
     * <p>
     * It retrieves from the startIndex to the startIndex + maxResults.
     * </p>
     * @param startIndex
     *            the start index
     * @param maxResults
     *            the max number of logs
     * @param criterion
     *            the sorting criterion
     * @return the paginated list of logs
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *             If an exception occurs during the log retrieving
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    List<Log> getLogs(int startIndex, int maxResults, LogCriterion criterion);

    /**
     * Searches logs according to the criteria containing in the options.
     * 
     * @param searchOptions
     *            the search criteria
     * @return the search result
     * @throws SearchException
     *             If an exception occurs during the log searching
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    SearchResult<Log> searchLogs(SearchOptions searchOptions) throws SearchException;

}
