/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.persistence.SBonitaReadException;

public class ParentContainerResolverImpl implements ParentContainerResolver {

    private final FlowNodeInstanceService flowNodeInstanceService;
    private final ProcessInstanceService processInstanceService;
    private boolean allowUnknownContainer;

    public ParentContainerResolverImpl(final FlowNodeInstanceService flowNodeInstanceService,
            final ProcessInstanceService processInstanceService) {
        super();
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.processInstanceService = processInstanceService;
    }

    public boolean getAllowUnknownContainer() {
        return allowUnknownContainer;
    }

    public void setAllowUnknownContainer(boolean allowUnknownContainer) {
        this.allowUnknownContainer = allowUnknownContainer;
    }

    @Override
    public List<DataContainer> getContainerHierarchy(final DataContainer currentContainer)
            throws SObjectNotFoundException, SObjectReadException {
        return getContainerHierarchy(currentContainer, false);
    }

    @Override
    public List<DataContainer> getArchivedContainerHierarchy(final DataContainer currentContainer)
            throws SObjectNotFoundException, SObjectReadException {
        try {
            return getContainerHierarchy(currentContainer, true);
        } catch (SObjectNotFoundException e) {
            return Collections.singletonList(currentContainer);
        }
    }

    private List<DataContainer> getContainerHierarchy(DataContainer currentContainer, boolean isArchived)
            throws SObjectNotFoundException, SObjectReadException {
        DataContainer container = new DataContainer(currentContainer.getId(), currentContainer.getType());
        final List<DataContainer> containerHierarchy = new ArrayList<>();
        containerHierarchy.add(container);
        try {
            do {
                container = getNextContainer(isArchived, container, containerHierarchy);
            } while (container != null);
            return containerHierarchy;
        } catch (SProcessInstanceNotFoundException | SFlowNodeNotFoundException e) {
            throw new SObjectNotFoundException(e);
        } catch (SProcessInstanceReadException | SBonitaReadException | SFlowNodeReadException e) {
            throw new SObjectReadException(e);
        }
    }

    private DataContainer getNextContainer(boolean isArchived, DataContainer container,
            List<DataContainer> containerHierarchy)
            throws SFlowNodeReadException, SFlowNodeNotFoundException, SBonitaReadException,
            SProcessInstanceNotFoundException, SProcessInstanceReadException, SObjectNotFoundException {
        if (DataInstanceContainer.ACTIVITY_INSTANCE.name().equals(container.getType())) {
            container = handleActivityContainer(containerHierarchy,
                    getsFlowNodeInstance(container.getId(), isArchived));
        } else if (DataInstanceContainer.MESSAGE_INSTANCE.name().equals(container.getType())) {
            container = null;
        } else if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(container.getType())) {
            container = handleProcessContainer(container.getId(), containerHierarchy, isArchived);
        } else {
            if (allowUnknownContainer) {
                return null;
            } else {
                throw new SObjectNotFoundException("Unknown container type: " + container.getType());
            }
        }
        return container;
    }

    private DataContainer handleActivityContainer(List<DataContainer> containerHierarchy,
            ActivityContainer flowNodeInstance) {
        String containerType;
        if (flowNodeInstance.parentActivityInstanceId > 0) {
            containerType = DataInstanceContainer.ACTIVITY_INSTANCE.name();
        } else {
            containerType = DataInstanceContainer.PROCESS_INSTANCE.name();
        }
        DataContainer container = new DataContainer(flowNodeInstance.parentContainerId, containerType);
        containerHierarchy.add(container);
        if (flowNodeInstance.parentActivityInstanceId <= 0
                && flowNodeInstance.parentContainerId == flowNodeInstance.rootContainerId) {
            container = null;
        }
        return container;
    }

    private DataContainer handleProcessContainer(long id, List<DataContainer> containerHierarchy, boolean isArchived)
            throws SProcessInstanceNotFoundException, SProcessInstanceReadException, SFlowNodeNotFoundException,
            SBonitaReadException, SFlowNodeReadException {
        final long callerId = getCallerId(id, isArchived);
        if (callerId >= 0) {
            ActivityContainer callerFlowNodeInstance = getsFlowNodeInstance(callerId, isArchived);
            final SFlowNodeType callerType = callerFlowNodeInstance.type;
            if (callerType != null && callerType.equals(SFlowNodeType.SUB_PROCESS)) {
                final long callerProcessInstanceId = callerFlowNodeInstance.parentProcessInstanceId;
                DataContainer container = new DataContainer(callerProcessInstanceId,
                        DataInstanceContainer.PROCESS_INSTANCE.name());
                containerHierarchy.add(container);
                return container;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private ActivityContainer getSaFlowNodeInstance(long id) throws SBonitaReadException, SFlowNodeNotFoundException {
        SAFlowNodeInstance flowNodeInstance;
        flowNodeInstance = flowNodeInstanceService.getLastArchivedFlowNodeInstance(SAFlowNodeInstance.class, id);
        if (flowNodeInstance == null) {
            throw new SFlowNodeNotFoundException(id);
        }
        return new ActivityContainer(flowNodeInstance);
    }

    private ActivityContainer getsFlowNodeInstance(long id) throws SFlowNodeReadException, SFlowNodeNotFoundException {
        SFlowNodeInstance flowNodeInstance;
        flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(id);
        return new ActivityContainer(flowNodeInstance);
    }

    private long getCallerId(long id, boolean isArchived)
            throws SProcessInstanceNotFoundException, SProcessInstanceReadException, SBonitaReadException {
        if (isArchived) {
            return getArchivedCallerId(id);
        } else {
            return getCallerId(id);
        }
    }

    private ActivityContainer getsFlowNodeInstance(long callerId, boolean isArchived)
            throws SFlowNodeReadException, SFlowNodeNotFoundException, SBonitaReadException {
        if (isArchived) {
            return getSaFlowNodeInstance(callerId);
        } else {
            return getsFlowNodeInstance(callerId);
        }
    }

    private long getCallerId(Long processInstanceId)
            throws SProcessInstanceNotFoundException, SProcessInstanceReadException {
        SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        return processInstance.getCallerId();
    }

    private long getArchivedCallerId(Long processInstanceId)
            throws SProcessInstanceNotFoundException, SProcessInstanceReadException, SBonitaReadException {
        SAProcessInstance processInstance = processInstanceService.getLastArchivedProcessInstance(processInstanceId);
        if (processInstance == null) {
            throw new SProcessInstanceNotFoundException(processInstanceId);
        }
        return processInstance.getCallerId();
    }

    private class ActivityContainer {

        private long parentContainerId;
        private long parentActivityInstanceId;
        private long rootContainerId;
        public SFlowNodeType type;
        public long parentProcessInstanceId;

        public ActivityContainer(SFlowNodeInstance flowNodeInstance) {
            parentContainerId = flowNodeInstance.getParentContainerId();
            parentActivityInstanceId = flowNodeInstance.getParentActivityInstanceId();
            rootContainerId = flowNodeInstance.getRootContainerId();
            type = flowNodeInstance.getType();
            parentProcessInstanceId = flowNodeInstance.getParentProcessInstanceId();
        }

        public ActivityContainer(SAFlowNodeInstance flowNodeInstance) {
            parentContainerId = flowNodeInstance.getParentContainerId();
            parentActivityInstanceId = flowNodeInstance.getParentActivityInstanceId();
            rootContainerId = flowNodeInstance.getRootContainerId();
            type = flowNodeInstance.getType();
            parentProcessInstanceId = flowNodeInstance.getParentProcessInstanceId();
        }
    }

}
