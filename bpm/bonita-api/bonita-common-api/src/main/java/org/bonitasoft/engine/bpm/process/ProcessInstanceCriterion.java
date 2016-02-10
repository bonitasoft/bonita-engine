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
package org.bonitasoft.engine.bpm.process;

/**
 * Sort criterion used to specify the sort order of the {@link ProcessInstance}. <br>
 * Used by {@link org.bonitasoft.engine.api.ProcessRuntimeAPI} methods like
 * {@link org.bonitasoft.engine.api.ProcessRuntimeAPI#getProcessInstances(int, int, ProcessInstanceCriterion)} to indicate in what order we should return the
 * list of the results.
 * 
 * @author Emmanuel Duchastenier
 * @author Yanyan Liu
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 * @see org.bonitasoft.engine.api.ProcessRuntimeAPI
 */
public enum ProcessInstanceCriterion {

    /**
     * Process State ascending order
     */
    STATE_ASC,

    /**
     * Process State descending order
     */
    STATE_DESC,

    /**
     * Process instance name ascending order
     */
    NAME_ASC,

    /**
     * Process instance name descending order
     */
    NAME_DESC,

    /**
     * Creation date ascending order
     */
    CREATION_DATE_ASC,

    /**
     * Creation date descending order
     */
    CREATION_DATE_DESC,

    /**
     * Last update ascending order
     */
    LAST_UPDATE_ASC,

    /**
     * Last update descending order
     */
    LAST_UPDATE_DESC,

    /**
     * Archive date ascending order
     */
    ARCHIVE_DATE_ASC,

    /**
     * Archive date descending order
     */
    ARCHIVE_DATE_DESC,

    /**
     * Default sort criterion
     */
    DEFAULT;
}
