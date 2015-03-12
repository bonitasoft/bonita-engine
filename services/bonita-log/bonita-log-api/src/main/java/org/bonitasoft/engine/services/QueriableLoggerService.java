/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.services;

import java.util.List;

import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;

/**
 * @author Charles Souillard
 * @author Celine Souchet
 */
public interface QueriableLoggerService {

    /**
     * Log the given queriable logs. Only logs having the action type and the severity configured
     * to as loggable will be logged
     * 
     * @param queriableLogs
     * @since 6.0
     */
    void log(String callerClassName, String callerMethodName, SQueriableLog... queriableLogs);

    /**
     * Verify if the given action type and severity are loggable
     * 
     * @param actionType
     *            the action type
     * @param severity
     *            the severity
     * @return true if the log is active for the given action type and severity; false otherwise
     * @since 6.0
     */
    boolean isLoggable(final String actionType, final SQueriableLogSeverity severity);

    /**
     * Get the queriable log from its id.
     * 
     * @param logId
     * @return the queriable log
     * @throws SQueriableLogException
     * @since 6.0
     */
    SQueriableLog getLog(long logId) throws SQueriableLogNotFoundException, SQueriableLogException;

    /**
     * Get total number of queriable logs
     * 
     * @return the number of queriable logs
     * @throws SQueriableLogException
     * @since 6.0
     */
    int getNumberOfLogs() throws SQueriableLogException;

    /**
     * Get the queriable logs having the given value for the given int index
     * 
     * @param fromIndex
     *            first result to be considered(>=0)
     * @param numberOfLogs
     *            the max number of queriable logs to be returned (>=0)
     * @param field
     * @param order
     * @return the queriable logs having the given value for the given int index
     * @throws SQueriableLogException
     * @since 6.0
     */
    List<SQueriableLog> getLogs(int startIndex, int maxResults, final String field, final OrderByType order) throws SQueriableLogException;

    /**
     * Gets the queriable logs number matching to the given QueryOptions.
     * 
     * @param QueryOptions
     *            The criterion used to search sQueriableLog
     * @return queriable logs number matching to the given searchOptions.
     * @throws SBonitaReadException
     * @since 6.0
     */
    long getNumberOfLogs(final QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Gets the queriable logs matching to the given searchOptions.
     * 
     * @param searchOptions
     *            The criterion used to search sQueriableLog
     * @return logs list matching to the given searchOptions.
     * @throws SBonitaReadException
     * @since 6.0
     */
    List<SQueriableLog> searchLogs(final QueryOptions searchOptions) throws SBonitaReadException;

}
