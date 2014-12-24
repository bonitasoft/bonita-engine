package org.bonitasoft.engine.data;

import java.util.ArrayList;
import java.util.List;

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
    public List<Pair<Long, String>> getContainerHierarchy(final Pair<Long, String> currentContainer) throws SObjectNotFoundException, SObjectReadException {
        Pair<Long, String> container = new Pair<Long, String>(currentContainer.getLeft(), currentContainer.getRight());

        final List<Pair<Long, String>> containerHierarchy = new ArrayList<Pair<Long, String>>();
        containerHierarchy.add(container);


        do {
            if (DataInstanceContainer.ACTIVITY_INSTANCE.name().equals(container.getRight())) {
                SFlowNodeInstance flowNodeInstance;
                try {
                    flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(container.getLeft());
                } catch (SFlowNodeNotFoundException e) {
                    throw new SObjectNotFoundException(e);
                } catch (SFlowNodeReadException e) {
                    throw new SObjectReadException(e);
                }
                container = new Pair<Long, String>(flowNodeInstance.getParentContainerId(), getContainerType(flowNodeInstance.getParentContainerType()));
                containerHierarchy.add(container);
                if (flowNodeInstance.getParentContainerType().equals(SFlowElementsContainerType.PROCESS) && flowNodeInstance.getParentContainerId() == flowNodeInstance.getRootContainerId()) {
                    container = null;
                }
            } else if (DataInstanceContainer.MESSAGE_INSTANCE.name().equals(container.getRight())) {
                container = null;
            } else if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(container.getRight())) {
                try {
                    final SProcessInstance processInstance = processInstanceService.getProcessInstance(container.getLeft());
                    final SFlowNodeType callerType = processInstance.getCallerType();
                    if (callerType != null && callerType.equals(SFlowNodeType.SUB_PROCESS)) {
                        container = new Pair<Long, String>(processInstance.getCallerId(), DataInstanceContainer.PROCESS_INSTANCE.name());
                        containerHierarchy.add(container);
                    } else {
                        container = null;
                    }
                } catch (SProcessInstanceNotFoundException e) {
                    throw new SObjectNotFoundException(e);
                } catch (SProcessInstanceReadException e) {
                    throw new SObjectReadException(e);
                }
            } else {
                throw new SObjectNotFoundException("Unknown container type: " + container.getRight());
            }
        } while(container != null);
        return containerHierarchy;
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
