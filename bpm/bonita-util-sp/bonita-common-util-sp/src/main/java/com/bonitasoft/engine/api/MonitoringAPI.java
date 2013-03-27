/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.api;

import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.MonitoringException;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public interface MonitoringAPI {

    /**
     * Get the number of all active transactions
     * If no active transactions there, return 0
     * 
     * @return the total number of active transaction
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfActiveTransactions() throws MonitoringException, InvalidSessionException;

    /**
     * Get the number of all executing processes
     * If no executing processes there, return 0
     * 
     * @return the total number of executing process
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfExecutingProcesses() throws MonitoringException, InvalidSessionException;

    /**
     * Get the number of all users
     * If no users there, return 0
     * 
     * @return the total number of user
     * @throws MonitoringException
     *             occurs when an exception is thrown during monitoring
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfUsers() throws MonitoringException, InvalidSessionException;

}
