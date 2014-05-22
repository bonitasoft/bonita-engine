/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
 ** 
 * @since 6.2
 */
package org.bonitasoft.engine.operation;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
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

/**
 * @author Baptiste Mesta
 * 
 */
public class TransientDataLeftOperandHandler implements LeftOperandHandler {

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
    public void update(final SLeftOperand sLeftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        SDataInstance dataInstance;
        try {
            dataInstance = retrieve(sLeftOperand, containerId, containerType);
            EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
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
        } catch (SDataInstanceException e) {
            throw new SOperationExecutionException("Unable to update the transient data", e);
        } catch (SBonitaReadException e) {
            throw new SOperationExecutionException("Unable to update the transient data", e);
        }
    }

    @Override
    public Object retrieve(final SLeftOperand sLeftOperand, final SExpressionContext expressionContext) throws SBonitaReadException {
        Long containerId = expressionContext.getContainerId();
        String containerType = expressionContext.getContainerType();
        return retrieve(sLeftOperand, containerId, containerType);
    }

    private SDataInstance retrieve(final SLeftOperand sLeftOperand, final Long containerId, final String containerType)
            throws SBonitaReadException {
        try {
            // if not found reevaluate
            return transientDataService.getDataInstance(sLeftOperand.getName(), containerId, containerType);
        } catch (SDataInstanceNotFoundException e) {
            try {
                logger.log(getClass(), TechnicalLogSeverity.WARNING, "The value of the transient data " + sLeftOperand.getName() + "  " + containerId + " "
                        + containerType);
                reevaluateTransientData(sLeftOperand.getName(), containerId, containerType,
                        flownodeInstanceService, processDefinitionService,
                        bpmInstancesCreator);
                return transientDataService.getDataInstance(sLeftOperand.getName(), containerId, containerType);
            } catch (SDataInstanceException e1) {
                throw new SBonitaReadException("Unable to read the transient data", e);
            }
        } catch (SDataInstanceException e) {
            throw new SBonitaReadException("Unable to read the transient data", e);
        }
    }

    /**
     * @param name
     * @param containerTypeœ
     * @param expressionContext
     */
    public static void reevaluateTransientData(final String name, final long containerId,
            final String containerType, final FlowNodeInstanceService flowbodeInstanceService, final ProcessDefinitionService processDefinitionService,
            final BPMInstancesCreator bpmInstancesCreator) throws SBonitaReadException {

        try {
            SFlowNodeInstance flowNodeInstance = flowbodeInstanceService.getFlowNodeInstance(containerId);
            long flowNodeDefinitionId = flowNodeInstance.getFlowNodeDefinitionId();
            long processDefinitionId = flowNodeInstance.getProcessDefinitionId();
            SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            SActivityDefinition flowNode = (SActivityDefinition) processDefinition.getProcessContainer().getFlowNode(flowNodeDefinitionId);
            List<SDataDefinition> sDataDefinitions = flowNode.getSDataDefinitions();
            SDataDefinition theTransientData = null;
            for (SDataDefinition sDataDefinition : sDataDefinitions) {
                if (sDataDefinition.getName().equals(name)) {
                    theTransientData = sDataDefinition;
                    break;
                }
            }
            if (theTransientData == null) {
                throw new SBonitaReadException(
                        "Transient data was not found and we were unable to reevaluate it because it was not found in the definition, name=<" + name
                                + "> process definition=<"
                                + processDefinition.getName() + "," + processDefinition.getVersion() + "> flow node=<" + flowNode.getName() + ">");
            }
            bpmInstancesCreator.createDataInstances(Arrays.asList(theTransientData), containerId, DataInstanceContainer.ACTIVITY_INSTANCE,
                    new SExpressionContext(containerId, containerType, processDefinitionId));
        } catch (SDataInstanceException e) {
            throwBonitaReadException(name, e);
        } catch (SExpressionException e) {
            throwBonitaReadException(name, e);
        } catch (SFlowNodeNotFoundException e) {
            throwBonitaReadException(name, e);
        } catch (SFlowNodeReadException e) {
            throwBonitaReadException(name, e);
        } catch (SProcessDefinitionReadException e) {
            throwBonitaReadException(name, e);
        } catch (SProcessDefinitionNotFoundException e) {
            throwBonitaReadException(name, e);
        }
    }

    private static void throwBonitaReadException(final String name, final Exception e) throws SBonitaReadException {
        throw new SBonitaReadException("Transient data was not found and we were unable to reevaluate it, name=<" + name + ">", e);
    }
}
