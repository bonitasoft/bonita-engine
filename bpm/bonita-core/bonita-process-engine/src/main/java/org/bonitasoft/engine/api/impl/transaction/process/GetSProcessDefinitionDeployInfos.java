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

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.persistence.OrderByType;

/**
 * @author Matthieu Chaffotte
 */
public class GetSProcessDefinitionDeployInfos implements TransactionContentWithResult<List<SProcessDefinitionDeployInfo>> {

    private final ProcessDefinitionService processDefinitionService;

    private final List<Long> processDefinitionIds;

    private final int fromIndex;

    private final int numberOfProcesses;

    private final String field;

    private final OrderByType order;

    private List<SProcessDefinitionDeployInfo> processDefinitionDeployInfos;

    public GetSProcessDefinitionDeployInfos(final ProcessDefinitionService processDefinitionService, final List<Long> processDefinitionIds,
            final int fromIndex, final int numberOfProcesses, final String field, final OrderByType order) {
        super();
        this.processDefinitionService = processDefinitionService;
        this.processDefinitionIds = processDefinitionIds;
        this.fromIndex = fromIndex;
        this.numberOfProcesses = numberOfProcesses;
        this.field = field;
        this.order = order;
    }

    @Override
    public void execute() throws SBonitaException {
        processDefinitionDeployInfos = processDefinitionService.getProcessDeploymentInfos(processDefinitionIds, fromIndex, numberOfProcesses, field,
                order);
    }

    @Override
    public List<SProcessDefinitionDeployInfo> getResult() {
        return processDefinitionDeployInfos;
    }

}
