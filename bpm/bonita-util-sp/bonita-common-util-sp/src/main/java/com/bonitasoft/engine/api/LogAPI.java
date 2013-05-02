/*******************************************************************************
 * Copyright (C) 2009, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.platform.InvalidSessionException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.exception.LogNotFoundException;
import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.LogCriterion;

/**
 * @author Bole Zhang
 * @author Matthieu Chaffotte
 */
public interface LogAPI {

    /**
     * Retrieves the log by giving its identifier
     * 
     * @param logId
     *            the log identifier
     * @return the Log object meet the criteria
     * @throws LogNotFoundException
     * @throws InvalidSessionException
     *             since 6.0
     */
    Log getLog(long logId) throws LogNotFoundException, InvalidSessionException;

    /**
     * get the total number of logs
     * 
     * @return the total number of logs
     * @throws InvalidSessionException
     * @Deprecated use {@link #searchLogs(SearchOptions)} instead.
     *             since 6.0
     */
    @Deprecated
    int getNumberOfLogs() throws InvalidSessionException;

    /**
     * retrieve logs to support pagination
     * 
     * @param pageIndex
     *            the starting point
     * @param numberPerPage
     *            the number of Logs to be retrieved
     * @param pagingCriterion
     *            the criterion used to sort the retried logs
     * @throws PageOutOfRangeException
     * @return the list of Log objects
     * @throws InvalidSessionException
     * @Deprecated use {@link #searchLogs(SearchOptions)} instead.
     *             since 6.0
     */
    @Deprecated
    List<Log> getLogs(int pageIndex, int numberPerPage, LogCriterion pagingCriterion) throws PageOutOfRangeException, InvalidSessionException;

    /**
     * retrieve logs to support search functionality
     * 
     * @param SearchOptions
     *            searchOptions
     * @return the SearchResult<Log>
     * @throws InvalidSessionException
     *             since 6.0
     */
    SearchResult<Log> searchLogs(SearchOptions searchOptions) throws InvalidSessionException;

}
