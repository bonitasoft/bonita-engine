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
package org.bonitasoft.engine.bpm.flownode;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public enum ActivityInstanceCriterion {

    /**
     * Last update ascending order
     */
    LAST_UPDATE_ASC,

    /**
     * Started date ascending order
     */
    REACHED_STATE_DATE_ASC,

    /**
     * Expected End Date ascending order
     */
    EXPECTED_END_DATE_ASC,

    /**
     * Name ascending order
     */
    NAME_ASC,

    /**
     * Priority ascending order
     */
    PRIORITY_ASC,

    /**
     * Last update descending order
     */
    LAST_UPDATE_DESC,

    /**
     * Started date descending order
     */
    REACHED_STATE_DATE_DESC,

    /**
     * Expected End Date descending order
     */
    EXPECTED_END_DATE_DESC,

    /**
     * Name descending order
     */
    NAME_DESC,

    /**
     * Priority descending order
     */
    PRIORITY_DESC,

    /**
     * 
     */
    DEFAULT

}
