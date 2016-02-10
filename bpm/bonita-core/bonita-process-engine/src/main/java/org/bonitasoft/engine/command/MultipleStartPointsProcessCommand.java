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
package org.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This command starts the process in the specified activity(ies). Connectors on process start will be executed.
 * <p>It can be executed using the {@link org.bonitasoft.engine.api.CommandAPI#execute(String, java.util.Map)}.
 * Example: {@code commandAPI.execute("multipleStartPointsProcessCommand", parameters)}</p>
 * Parameters:
 * <ul>
 * <li> started_by: the user id (long) is used as the process starter. It's a mandatory parameter.</li>
 * <li> process_definition_id: the process definition id (long) identifies the process to start. It's a mandatory parameter.</li>
 * <li> activity_names: list of activity names (ArrayList<String>) defining where the process will start the execution. It's a mandatory parameter.</li>
 * <li> operations: the operations (ArrayList<Operation>) are executed when the process starts (set variables and documents). It's an optional parameter.</li>
 * <li> context: the context (HashMap<String, Serializable>) is used during operations execution. It's an optional parameter.</li>
 * </ul>
 * Limitations: It is not possible to start the execution of a process from a gateway, a boundary event or an event sub-process
 * <p>Use this command carefully: note that no validation will be done concerning the start points coherence.</p>
 *
 * @author Elias Ricken de Medeiros
 * @since 6.5.0
 */
public class MultipleStartPointsProcessCommand extends AbstractStartProcessCommand {

    public static final String ACTIVITY_NAMES = "activity_names";

    @Override
    protected List<String> getActivityNames(final Map<String, Serializable> parameters) throws SCommandParameterizationException {
        return getMandatoryParameter(parameters, ACTIVITY_NAMES, "Missing mandatory field: " + ACTIVITY_NAMES);
    }

}
