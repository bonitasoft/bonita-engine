package org.bonitasoft.engine.data;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SObjectReadException;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;

public class ParentContainerResolverImpl implements ParentContainerResolver {

    private final FlowNodeInstanceService flowNodeInstanceService;

    private final ProcessInstanceService processInstanceService;

    public ParentContainerResolverImpl(FlowNodeInstanceService flowNodeInstanceService, ProcessInstanceService processInstanceService) {
        super();
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.processInstanceService = processInstanceService;
    }

    @Override
    public Pair<Long, String> getParentContainer(final Pair<Long, String> container) throws SObjectNotFoundException, SObjectReadException {
        final long containerId = container.getLeft();
        final String containerType = container.getRight();
        if (DataInstanceContainer.ACTIVITY_INSTANCE.name().equals(containerType)) {
            SFlowNodeInstance flowNodeInstance;
            try {
                flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(containerId);
            } catch (SFlowNodeNotFoundException e) {
                throw new SObjectNotFoundException(e);
            } catch (SFlowNodeReadException e) {
                throw new SObjectReadException(e);
            }
            return new Pair<Long, String>(flowNodeInstance.getParentContainerId(), getContainerType(flowNodeInstance.getParentContainerType()));
        } else if (DataInstanceContainer.MESSAGE_INSTANCE.name().equals(containerType)) {
            return null;
        } else if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
            try {
                final SProcessInstance processInstance = processInstanceService.getProcessInstance(containerId);
                final SFlowNodeType callerType = processInstance.getCallerType();
                if (callerType.equals(SFlowNodeType.SUB_PROCESS)) {
                    return new Pair<Long, String>(processInstance.getCallerId(), DataInstanceContainer.PROCESS_INSTANCE.name());
                } else {
                    return null;
                }
            } catch (SProcessInstanceNotFoundException e) {
                throw new SObjectNotFoundException(e);
            } catch (SProcessInstanceReadException e) {
                throw new SObjectReadException(e);
            }
        } else {
            throw new SObjectNotFoundException("Unknown container type: " + containerType);
        }
    }

    private String getContainerType(final SFlowElementsContainerType flowElementsContainerType) throws SObjectNotFoundException {
        switch (flowElementsContainerType) {
            case FLOWNODE:
                return DataInstanceContainer.ACTIVITY_INSTANCE.name();
            case PROCESS:
                return DataInstanceContainer.PROCESS_INSTANCE.name();
            default:
                throw new SObjectNotFoundException("unable to convert from " + SFlowElementsContainerType.class + ": " + flowElementsContainerType + " to "
                        + DataInstanceContainer.class + " type");

        }
    }
}
