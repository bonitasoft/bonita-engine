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
package org.bonitasoft.engine.api.impl.transaction.process;

import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;

/**
 * @author Baptiste Mesta
 */
public final class GetProcessDefinitionDeployInfo implements TransactionContentWithResult<SProcessDefinitionDeployInfo> {

    private final Long processDefinitionUUID;

    private final ProcessDefinitionService processDefinitionService;

    private SProcessDefinitionDeployInfo processDefinitionDI;

    public GetProcessDefinitionDeployInfo(final Long processDefinitionUUID, final ProcessDefinitionService processDefinitionService) {
        this.processDefinitionUUID = processDefinitionUUID;
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public void execute() throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException {
        processDefinitionDI = processDefinitionService.getProcessDeploymentInfo(processDefinitionUUID);
    }

    @Override
    public SProcessDefinitionDeployInfo getResult() {
        return processDefinitionDI;
    }
}
