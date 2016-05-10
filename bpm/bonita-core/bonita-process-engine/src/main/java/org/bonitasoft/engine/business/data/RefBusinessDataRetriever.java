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

package org.bonitasoft.engine.business.data;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.operation.BusinessDataContext;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 */
public class RefBusinessDataRetriever {

    private RefBusinessDataService refBusinessDataService;
    private final FlowNodeInstanceService flowNodeInstanceService;
    private ProcessInstanceService processInstanceService;

    public RefBusinessDataRetriever(final RefBusinessDataService refBusinessDataService, FlowNodeInstanceService flowNodeInstanceService,
            ProcessInstanceService processInstanceService) {
        this.refBusinessDataService = refBusinessDataService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.processInstanceService = processInstanceService;
    }

    public SRefBusinessDataInstance getRefBusinessDataInstance(BusinessDataContext context) throws SBonitaException {
        if (isProcessContext(context)) {
            return getRefBusinessDataUsingProcessContext(context);
        }
        return getRefBusinessDataUsingFlowNodeContext(context);

    }

    private SRefBusinessDataInstance getRefBusinessDataUsingFlowNodeContext(BusinessDataContext context)
            throws SBonitaReadException, SFlowNodeReadException, SRefBusinessDataInstanceNotFoundException, SProcessInstanceReadException {
        SRefBusinessDataInstance refBusinessDataInstance = getRefBusinessDataInFlowNode(context);
        if (refBusinessDataInstance != null) {
            return refBusinessDataInstance;
        }
        return getRefBusinessDataInProcess(context, getProcessInstanceIdFromFlowNode(context));
    }

    private SRefBusinessDataInstance getRefBusinessDataInProcess(BusinessDataContext context, long rootProcessInstanceId)
            throws SBonitaReadException, SRefBusinessDataInstanceNotFoundException {
        try {
            return refBusinessDataService.getRefBusinessDataInstance(context.getName(), rootProcessInstanceId);
        } catch (SRefBusinessDataInstanceNotFoundException e) {
            return refBusinessDataService.getSARefBusinessDataInstance(context.getName(), rootProcessInstanceId);
        }
    }

    private boolean isProcessContext(BusinessDataContext context) {
        return DataInstanceContainer.PROCESS_INSTANCE.name().equals(context.getContainer().getType());
    }

    private long getProcessInstanceIdFromFlowNode(BusinessDataContext context)
            throws SFlowNodeReadException, SBonitaReadException, SProcessInstanceReadException {
        try {
            SFlowNodeInstance flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(context.getContainer().getId());
            SProcessInstance processInstance = processInstanceService.getProcessInstance(flowNodeInstance.getParentProcessInstanceId());
            if (isSubProcess(processInstance)) {
                return getParentOfSubProcess(processInstance);
            }
            return flowNodeInstance.getParentProcessInstanceId();
        } catch (SFlowNodeNotFoundException | SProcessInstanceNotFoundException e) {
            SAFlowNodeInstance lastArchivedFlowNodeInstance = flowNodeInstanceService
                    .getLastArchivedFlowNodeInstance(SAFlowNodeInstance.class, context.getContainer().getId());
            //No caller type in archived process instance, get archive business data not supported from event subprocess
            return lastArchivedFlowNodeInstance.getParentProcessInstanceId();
        }
    }

    private SRefBusinessDataInstance getRefBusinessDataInFlowNode(BusinessDataContext context) throws SBonitaReadException {
        try {
            return refBusinessDataService.getFlowNodeRefBusinessDataInstance(context.getName(), context.getContainer().getId());
        } catch (final SRefBusinessDataInstanceNotFoundException sbe) {
            try {
                return refBusinessDataService.getSAFlowNodeRefBusinessDataInstance(context.getName(), context.getContainer().getId());
            } catch (SRefBusinessDataInstanceNotFoundException e) {
                return null;
            }
        }
    }

    private SRefBusinessDataInstance getRefBusinessDataUsingProcessContext(BusinessDataContext context)
            throws SBonitaReadException, SRefBusinessDataInstanceNotFoundException, SProcessInstanceReadException, SProcessInstanceNotFoundException,
            SFlowNodeReadException, SFlowNodeNotFoundException {
        return getRefBusinessDataInProcess(context, getProcessInstanceIdThatCanContainBusinessData(context.getContainer().getId()));
    }

    /*
     * get the first process in hierarchy that can contains business data, i.e. the process instance itself or its parent if it is an event subprocess
     */
    private long getProcessInstanceIdThatCanContainBusinessData(long processInstanceId)
            throws SProcessInstanceReadException, SBonitaReadException, SProcessInstanceNotFoundException, SFlowNodeReadException, SFlowNodeNotFoundException {
        try {
            SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
            if (isSubProcess(processInstance)) {
                return getParentOfSubProcess(processInstance);
            }
            return processInstanceId;
        } catch (SProcessInstanceNotFoundException e) {
            //No caller type in archived process instance, get archive business data not supported from event subprocess
            return processInstanceId;
        }
    }

    private boolean isSubProcess(SProcessInstance processInstance) {
        return SFlowNodeType.SUB_PROCESS.equals(processInstance.getCallerType());
    }

    private long getParentOfSubProcess(SProcessInstance processInstance) throws SFlowNodeNotFoundException, SFlowNodeReadException {
        return flowNodeInstanceService.getFlowNodeInstance(processInstance.getCallerId()).getParentProcessInstanceId();
    }

}
