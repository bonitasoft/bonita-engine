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
package org.bonitasoft.engine.operation;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceNotFoundException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.expression.exception.SExpressionException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class TransientDataLeftOperandHandler implements LeftOperandHandler {

    private static final String TRANSIENT_DATA = "%TRANSIENT_DATA%_";
    private final TransientDataService transientDataService;

    private final FlowNodeInstanceService flownodeInstanceService;

    private final ProcessDefinitionService processDefinitionService;

    private final BPMInstancesCreator bpmInstancesCreator;

    private final TechnicalLoggerService logger;

    public TransientDataLeftOperandHandler(final TransientDataService transientDataService, final FlowNodeInstanceService flownodeInstanceService,
            final ProcessDefinitionService processDefinitionService, final BPMInstancesCreator bpmInstancesCreator, final TechnicalLoggerService logger) {
        this.transientDataService = transientDataService;
        this.flownodeInstanceService = flownodeInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.bpmInstancesCreator = bpmInstancesCreator;
        this.logger = logger;
    }

    @Override
    public String getType() {
        return LeftOperand.TYPE_TRANSIENT_DATA;
    }

    @Override
    public Object update(final SLeftOperand sLeftOperand, Map<String, Object> inputValues, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        SDataInstance dataInstance;
        try {

            dataInstance = (SDataInstance) inputValues.get(TRANSIENT_DATA + sLeftOperand.getName());
            if(dataInstance == null){
                dataInstance = retrieve(sLeftOperand, containerId, containerType);
            }
            final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
            descriptor.addField("value", newValue);
            logger.log(
                    getClass(),
                    TechnicalLogSeverity.WARNING,
                    "The value of the transient data "
                            + sLeftOperand.getName()
                            + " of "
                            + containerId
                            + " "
                            + containerType
                            + " is being updated, be carefull if the application server is restarted this new value will be lost and the data will be reset to its initial value. "
                            + "We advise you to change the design of your process. If you understand the risks and want to hide this warning, change the logging level of this class to error.");
            transientDataService.updateDataInstance(dataInstance, descriptor);
            return newValue;
        } catch (final SDataInstanceException | SBonitaReadException e) {
            throw new SOperationExecutionException("Unable to update the transient data", e);
        }
    }

    @Override
    public void delete(final SLeftOperand leftOperand, final long containerId, final String containerType) throws SOperationExecutionException {
        throw new SOperationExecutionException("Deleting a transient data is not supported");
    }

    @Override
    public void loadLeftOperandInContext(final SLeftOperand sLeftOperand, final SExpressionContext expressionContext, Map<String, Object> contextToSet) throws SBonitaReadException {
        final Long containerId = expressionContext.getContainerId();
        final String containerType = expressionContext.getContainerType();
        String name = sLeftOperand.getName();
        SDataInstance dataInstance = retrieve(sLeftOperand, containerId, containerType);
        contextToSet.put(TRANSIENT_DATA + name, dataInstance);
        if (!contextToSet.containsKey(name)) {
            contextToSet.put(name, dataInstance.getValue());
        }
    }

    private SDataInstance retrieve(final SLeftOperand sLeftOperand, final Long containerId, final String containerType) throws SBonitaReadException {
        try {
            // if not found reevaluate
            return transientDataService.getDataInstance(sLeftOperand.getName(), containerId, containerType);
        } catch (final SDataInstanceNotFoundException e) {
            try {
                logger.log(getClass(), TechnicalLogSeverity.WARNING, "The value of the transient data " + sLeftOperand.getName() + "  " + containerId + " "
                        + containerType);
                reevaluateTransientData(sLeftOperand.getName(), containerId, containerType, flownodeInstanceService, processDefinitionService,
                        bpmInstancesCreator);
                return transientDataService.getDataInstance(sLeftOperand.getName(), containerId, containerType);
            } catch (final SDataInstanceException e1) {
                throw new SBonitaReadException("Unable to read the transient data", e);
            }
        } catch (final SDataInstanceException e) {
            throw new SBonitaReadException("Unable to read the transient data", e);
        }
    }

    public static void reevaluateTransientData(final String name, final long containerId, final String containerType,
            final FlowNodeInstanceService flowNodeInstanceService, final ProcessDefinitionService processDefinitionService,
            final BPMInstancesCreator bpmInstancesCreator) throws SBonitaReadException {

        try {
            final SFlowNodeInstance flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(containerId);
            final long flowNodeDefinitionId = flowNodeInstance.getFlowNodeDefinitionId();
            final long processDefinitionId = flowNodeInstance.getProcessDefinitionId();
            final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            final SActivityDefinition flowNode = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(flowNodeDefinitionId);
            final List<SDataDefinition> sDataDefinitions = flowNode.getSDataDefinitions();
            SDataDefinition theTransientData = null;
            for (final SDataDefinition sDataDefinition : sDataDefinitions) {
                if (sDataDefinition.getName().equals(name)) {
                    theTransientData = sDataDefinition;
                    break;
                }
            }
            if (theTransientData == null) {
                throw new SBonitaReadException(
                        "Transient data was not found and we were unable to reevaluate it because it was not found in the definition, name=<" + name
                        + "> process definition=<" + processDefinition.getName() + "," + processDefinition.getVersion() + "> flow node=<"
                                + flowNode.getName() + ">");
            }
            bpmInstancesCreator.createDataInstances(Collections.singletonList(theTransientData), containerId, DataInstanceContainer.ACTIVITY_INSTANCE,
                    new SExpressionContext(containerId, containerType, processDefinitionId));
        } catch (final SDataInstanceException | SProcessDefinitionNotFoundException | SBonitaReadException | SFlowNodeReadException | SFlowNodeNotFoundException | SExpressionException e) {
            throwBonitaReadException(name, e);
        }
    }

    private static void throwBonitaReadException(final String name, final Exception e) throws SBonitaReadException {
        throw new SBonitaReadException("Transient data was not found and we were unable to reevaluate it, name=<" + name + ">", e);
    }


    @Override
    public void loadLeftOperandInContext(final List<SLeftOperand> sLeftOperand, final SExpressionContext expressionContext, Map<String, Object> contextToSet) throws SBonitaReadException {
        for (SLeftOperand leftOperand : sLeftOperand) {
            loadLeftOperandInContext(leftOperand, expressionContext, contextToSet);
        }
    }

}
