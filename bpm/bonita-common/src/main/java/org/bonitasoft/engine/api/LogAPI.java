/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.LogNotFoundException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.log.LogCriterion;

/**
 * @author Bole Zhang
 */
public interface LogAPI {

    /**
     * retrieving the log by logId
     * 
     * @param logId
     * @return the Log object meet the criteria
     * @throws LogNotFoundException
     * @throws InvalidSessionException
     */
    Log getLog(long logId) throws LogNotFoundException, InvalidSessionException;

    /**
     * get the total number of logs
     * 
     * @return the total number of logs
     * @throws InvalidSessionException
     * @Deprecated use {@link #searchLogs(SearchOptions)} instead.
     */
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
     */
    List<Log> getLogs(int pageIndex, int numberPerPage, LogCriterion pagingCriterion) throws PageOutOfRangeException, InvalidSessionException;

    /**
     * retrieve logs to support search functionality
     * 
     * @param SearchOptions
     *            searchOptions
     * @return the SearchResult<Log>
     * @throws InvalidSessionException
     */
    SearchResult<Log> searchLogs(SearchOptions searchOptions) throws InvalidSessionException;

}
