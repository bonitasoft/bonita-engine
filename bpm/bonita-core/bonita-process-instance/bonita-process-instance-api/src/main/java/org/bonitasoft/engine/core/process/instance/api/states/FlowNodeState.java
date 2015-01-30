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
package org.bonitasoft.engine.core.process.instance.api.states;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface FlowNodeState extends State<SFlowNodeInstance> {

    /**
     * @param processDefinition
     *            TODO
     * @param flowNodeInstance
     * @return true the state must be executed, false if the execution must skip this state and go directly to the next one
     * @throws SActivityExecutionException
     */
    boolean shouldExecuteState(SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException;

    /**
     * Return true if flowNodeInstance instance of SHumanTaskInstance
     * 
     * @param flowNodeInstance
     * @return true or false
     * @since 6.0
     */
    boolean mustAddSystemComment(SFlowNodeInstance flowNodeInstance);

    /**
     * Add a system comment "User XYZ has XYZ(state change) task XYZ(task name)"
     * 
     * @param flowNodeInstance
     * @return system comment "User XYZ has XYZ(state change) task XYZ(task name)"
     * @since 6.0
     */
    String getSystemComment(SFlowNodeInstance flowNodeInstance);

}
