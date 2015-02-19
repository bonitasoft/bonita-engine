/**
 * Copyright (C) 2013-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This command starts the process in the specified activity. Connectors on process start will be executed.
 * Parameters:
 * - started_by: the user id (long) is used as the process starter. It's a mandatory parameter.
 * - process_definition_id: the process definition id (long) identifies the process to start. It's a mandatory parameter.
 * - activity_name: the name of the activity (String) where the process will start the execution. It's a mandatory
 * parameter.
 * - operations: the operations (ArrayList<Operation>) are executed when the process starts (set variables and documents). It's an optional parameter.
 * - context: the context (HashMap<String, Serializable>) is used during operations execution. It's an optional parameter.
 * Limitations:
 * - It is not possible to start the execution of a process from a gateway, a boundary event or an event sub-process
 * - The process must be started when there is only one active branch.
 * Example:
 * start -> step1 -> gateway1 -> (step2 || step3) -> gateway2 -> step4 -> end
 * - Ok: start from "start" or "step1" or "step4" or "end"
 * - All other start points are invalid.
 * 
 * @author Vincent Elcrin
 */
public class AdvancedStartProcessCommand extends AbstractStartProcessCommand {

    public static final String ACTIVITY_NAME = "activity_name";

    protected List<String> getActivityNames(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        return Collections.singletonList(getStringMandadoryParameter(parameters, ACTIVITY_NAME));
    }

}
