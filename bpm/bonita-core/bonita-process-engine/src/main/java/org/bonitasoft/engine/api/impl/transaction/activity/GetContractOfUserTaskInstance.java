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
package org.bonitasoft.engine.api.impl.transaction.activity;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserTaskDefinition;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;

/**
 * @author Matthieu Chaffotte
 */
public class GetContractOfUserTaskInstance implements TransactionContentWithResult<SContractDefinition> {

    private final ProcessDefinitionService definitionService;

    private final SUserTaskInstance userTaskInstance;

    private SContractDefinition contract;

    public GetContractOfUserTaskInstance(final ProcessDefinitionService definitionService, final SUserTaskInstance userTaskInstance) {
        this.definitionService = definitionService;
        this.userTaskInstance = userTaskInstance;
    }

    @Override
    public void execute() throws SBonitaException {
        final SProcessDefinition processDefinition = definitionService.getProcessDefinition(userTaskInstance.getProcessDefinitionId());
        final SUserTaskDefinition userTaskDefinition = (SUserTaskDefinition) processDefinition.getProcessContainer().getFlowNode(
                userTaskInstance.getFlowNodeDefinitionId());
        contract = userTaskDefinition.getContract();
    }

    @Override
    public SContractDefinition getResult() {
        return contract;
    }

}
