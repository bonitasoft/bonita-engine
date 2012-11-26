/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import java.util.List;

import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.LogNotFoundException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.log.Log;
import org.bonitasoft.engine.log.LogCriterion;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

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
