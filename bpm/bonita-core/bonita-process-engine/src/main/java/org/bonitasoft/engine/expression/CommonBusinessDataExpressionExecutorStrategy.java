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
 */

package org.bonitasoft.engine.expression;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Baptiste Mesta
 */
public abstract class CommonBusinessDataExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {
    protected final RefBusinessDataService refBusinessDataService;
    protected final FlowNodeInstanceService flowNodeInstanceService;

    public CommonBusinessDataExpressionExecutorStrategy(final RefBusinessDataService refBusinessDataService, final FlowNodeInstanceService flowNodeInstanceService) {
        this.refBusinessDataService = refBusinessDataService;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    protected SRefBusinessDataInstance getRefBusinessDataInstance(final String businessDataName, final long containerId, final String containerType) throws SBonitaReadException, SRefBusinessDataInstanceNotFoundException, SFlowNodeReadException, SFlowNodeNotFoundException {
        if ("PROCESS_INSTANCE".equals(containerType)) {
            return refBusinessDataService.getRefBusinessDataInstance(businessDataName, containerId);
        }
        try {
            return refBusinessDataService.getFlowNodeRefBusinessDataInstance(businessDataName, containerId);
        } catch (SRefBusinessDataInstanceNotFoundException e) {
            final long processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);
            return refBusinessDataService.getRefBusinessDataInstance(businessDataName, processInstanceId);
        }
    }

    void setProcessInstanceId(Long containerId, String containerType, SBonitaException e) {
        if ("PROCESS_INSTANCE".equals(containerType)) {
            e.setProcessInstanceIdOnContext(containerId);
        }
    }
}
