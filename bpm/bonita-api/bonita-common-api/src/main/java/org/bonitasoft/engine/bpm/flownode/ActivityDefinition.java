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

import java.util.List;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.operation.Operation;

/**
 * An Activity is work that is performed within a Business Process. An Activity can be atomic or non-atomic
 * (compound). The types of Activities that are a part of a Process are: {@link TaskDefinition}, {@link org.bonitasoft.engine.bpm.process.SubProcessDefinition},
 * and {@link CallActivityDefinition}, which allows the inclusion of re-usable Tasks and Processes.
 *
 * @author Baptiste Mesta
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ActivityDefinition extends FlowNodeDefinition {

    /**
     * @return The list of loops on this activity
     */
    LoopCharacteristics getLoopCharacteristics();

    /**
     * @return The list of the definitions of business data of the activity.
     */
    List<BusinessDataDefinition> getBusinessDataDefinitions();

    /**
     * @return The list of the definition of data on this activity
     */
    List<DataDefinition> getDataDefinitions();

    /**
     * @return The list of operations on this activity
     */
    List<Operation> getOperations();

    /**
     * @return The list of the definition of boundary events on this activity
     */
    List<BoundaryEventDefinition> getBoundaryEventDefinitions();

    DataDefinition getDataDefinition(String name);

    BusinessDataDefinition getBusinessDataDefinition(String name);

}
