/**
 * Copyright (C) 2016 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.form;

import java.util.List;

/**
 * @author Laurent Leseigneur
 */
public interface AuthorizationRuleMapping {

    /**
     * @return a list of rule identifiers applied when a form or page is used to start a process
     */
    List<String> getProcessStartRuleKeys();

    /**
     * @return a list of rule identifiers applied when a form or page is used to display process overview
     */
    List<String> getProcessOverviewRuleKeys();

    /**
     * @return a list of rule identifiers applied when a form or page is used to execute a task
     */
    List<String> getTaskRuleKeys();
}
