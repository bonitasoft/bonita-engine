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
package org.bonitasoft.engine.execution.transition;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;

/**
 * @author Elias Ricken de Medeiros
 */
public class DefaultTransitionGetter {

    STransitionDefinition getDefaultTransition(FlowNodeTransitionsWrapper transitions, SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance)
            throws SActivityExecutionException {
        STransitionDefinition defaultTransition = transitions.getDefaultTransition();
        if(defaultTransition == null) {
            final SActivityExecutionException exception = new SActivityExecutionException("There is no default transition on " + flowNodeInstance.getName()
                    + ", but no outgoing transition had a valid condition.");
            exception.setProcessDefinitionNameOnContext(processDefinition.getName());
            exception.setProcessDefinitionVersionOnContext(processDefinition.getVersion());
            exception.setProcessInstanceIdOnContext(flowNodeInstance.getParentProcessInstanceId());
            throw exception;
        }
        return defaultTransition;
    }

}
