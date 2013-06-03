/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class GetAllProcessDefinitionIdsInState implements TransactionContentWithResult<Set<Long>> {

    private final ProcessDefinitionService definitionService;

    private final ActivationState activationState;

    private Set<Long> processDefinitionIds;

    public GetAllProcessDefinitionIdsInState(final ProcessDefinitionService definitionService, final ActivationState activationState) {
        super();
        this.definitionService = definitionService;
        this.activationState = activationState;
    }

    @Override
    public void execute() throws SBonitaException {
        final long numberOfProcesses = definitionService.getNumberOfProcessDeploymentInfo(activationState);
        final List<Long> processDefIds = definitionService.getProcessDefinitionIds(activationState, 0, numberOfProcesses);
        processDefinitionIds = new HashSet<Long>(processDefIds);
    }

    @Override
    public Set<Long> getResult() {
        return processDefinitionIds;
    }

}
