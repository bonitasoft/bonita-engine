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
package org.bonitasoft.engine.sequence;

import java.sql.SQLException;

import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface SequenceManager {

    void reset();

    long getNextId(String entityName, long tenantId) throws SObjectNotFoundException, SObjectModificationException;

    /**
     * clear all in memory sequence
     */
    void clear();

    /**
     * clear in memory sequence for only one tenant
     * 
     * @param tenantId
     */
    void clear(long tenantId);

    void close() throws SQLException;

}
