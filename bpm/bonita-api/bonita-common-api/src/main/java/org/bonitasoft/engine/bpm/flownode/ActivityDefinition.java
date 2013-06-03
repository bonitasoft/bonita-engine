/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

import java.util.List;

import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.operation.Operation;

/**
 * An Activity is work that is performed within a Business Process. An Activity can be atomic or non-atomic
 * (compound). The types of Activities that are a part of a Process are: Task, Sub-Process, and Call Activity, which
 * allows the inclusion of re-usable Tasks and Processes.
 * 
 * @author Baptiste Mesta
 * @author Feng Hui
 * @author Matthieu Chaffotte
 */
public interface ActivityDefinition extends FlowNodeDefinition {

    LoopCharacteristics getLoopCharacteristics();

    List<DataDefinition> getDataDefinitions();

    List<Operation> getOperations();

    List<BoundaryEventDefinition> getBoundaryEventDefinitions();

}
