/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;

/**
 * @author Hongwen Zang
 * 
 */
public class GetProcessDeploymentInfosFromIds implements TransactionContentWithResult<List<SProcessDefinitionDeployInfo>> {

    private final List<Long> processDefinitionIds;

    private final ProcessDefinitionService processDefinitionService;

    private List<SProcessDefinitionDeployInfo> processDefinitionDeployInfos;

    public GetProcessDeploymentInfosFromIds(final List<Long> processDefinitionIds, final ProcessDefinitionService processDefinitionService) {
        this.processDefinitionIds = processDefinitionIds;
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public void execute() throws SBonitaException {
        processDefinitionDeployInfos = processDefinitionService.getProcessDeploymentInfos(processDefinitionIds);

    }

    @Override
    public List<SProcessDefinitionDeployInfo> getResult() {
        return processDefinitionDeployInfos;
    }

}
