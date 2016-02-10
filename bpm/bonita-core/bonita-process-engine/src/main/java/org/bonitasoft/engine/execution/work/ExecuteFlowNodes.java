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
package org.bonitasoft.engine.execution.work;

import java.util.Iterator;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.GatewayInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * Restart flownodes that needs to be restarted in a single transaction, with a maximum of {@value #MAX_FLOWNODES_TO_RESTART_PER_TRANSACTION}.
 *
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class ExecuteFlowNodes implements Callable<Object> {

    private static final int MAX_FLOWNODES_TO_RESTART_PER_TRANSACTION = 20;

    private final WorkService workService;

    private final TechnicalLoggerService logger;

    private final ActivityInstanceService activityInstanceService;

    private final GatewayInstanceService gatewayInstanceService;

    private final ProcessDefinitionService processDefinitionService;

    private final Iterator<Long> iterator;

    public ExecuteFlowNodes(final TenantServiceAccessor tenantServiceAccessor, final Iterator<Long> iterator) {
        workService = tenantServiceAccessor.getWorkService();
        logger = tenantServiceAccessor.getTechnicalLoggerService();
        activityInstanceService = tenantServiceAccessor.getActivityInstanceService();
        gatewayInstanceService = tenantServiceAccessor.getGatewayInstanceService();
        processDefinitionService = tenantServiceAccessor.getProcessDefinitionService();
        this.iterator = iterator;
    }

    @Override
    public Object call() throws Exception {
        try {
            for (int i = 0; i < MAX_FLOWNODES_TO_RESTART_PER_TRANSACTION && iterator.hasNext(); i++) {
                SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(iterator.next());
                if (flowNodeInstance.isTerminal()) {
                    createNotifyChildFinishedWork(workService, logger, flowNodeInstance);
                } else {
                    if (shouldExecuteFlownode(flowNodeInstance)) {
                        createExecuteFlowNodeWork(workService, logger, flowNodeInstance);
                    } else {
                        if (logger.isLoggable(RestartFlowNodesHandler.class, TechnicalLogSeverity.INFO)) {
                            logger.log(RestartFlowNodesHandler.class, TechnicalLogSeverity.INFO, "Flownode with name = <" + flowNodeInstance.getName()
                                    + ">, and id = <" + flowNodeInstance.getId() + "> in state = <" + flowNodeInstance.getStateName()
                                    + "> does not fullfill the restart conditions.");
                        }
                    }
                }
            }
            return null;
        } catch (final SWorkRegisterException e) {
            throw new RestartException("Unable to restart flowNodes: can't register work", e);
        } catch (final SBonitaException e) {
            throw new RestartException("Unable to restart flowNodes: can't read flow nodes", e);
        }
    }

    void createExecuteFlowNodeWork(final WorkService workService, final TechnicalLoggerService logger, final SFlowNodeInstance sFlowNodeInstance)
            throws SWorkRegisterException {
        logInfo(logger, "Restarting flow node (Execute ...) with name = <" + sFlowNodeInstance.getName() + ">, and id = <" + sFlowNodeInstance.getId()
                + "> in state = <" + sFlowNodeInstance.getStateName() + ">");
        // ExecuteFlowNodeWork and ExecuteConnectorOfActivityWork
        workService.registerWork(WorkFactory.createExecuteFlowNodeWork(sFlowNodeInstance.getProcessDefinitionId(),
                sFlowNodeInstance.getParentProcessInstanceId(), sFlowNodeInstance.getId(), null, null));
    }

    void createNotifyChildFinishedWork(final WorkService workService, final TechnicalLoggerService logger, final SFlowNodeInstance sFlowNodeInstance)
            throws SWorkRegisterException {
        logInfo(logger, "Restarting flow node (Notify finished...) with name = <" + sFlowNodeInstance.getName() + ">, and id = <" + sFlowNodeInstance.getId()
                + " in state = <" + sFlowNodeInstance.getStateName() + ">");
        // NotifyChildFinishedWork, if it is terminal it means the notify was not called yet
        workService.registerWork(WorkFactory.createNotifyChildFinishedWork(sFlowNodeInstance.getProcessDefinitionId(), sFlowNodeInstance
                .getParentProcessInstanceId(), sFlowNodeInstance.getId(), sFlowNodeInstance.getParentContainerId(), sFlowNodeInstance.getParentContainerType()
                .name()));
    }

    private void logInfo(final TechnicalLoggerService logger, final String message) {
        final boolean isInfo = logger.isLoggable(RestartFlowNodesHandler.class, TechnicalLogSeverity.INFO);
        if (isInfo) {
            logger.log(RestartFlowNodesHandler.class, TechnicalLogSeverity.INFO, message);
        }
    }

    /**
     * Determines if the found flownode should be relaunched at restart or not. For now, only Gateways must not always be restarted under certain conditions.
     *
     * @param sFlowNodeInstance
     *            the flownode to check
     * @return true if the flownode should be relaunched because it has not finished its work in progress, false otherwise.
     * @throws SBonitaException
     *             in case of error.
     */
    protected boolean shouldExecuteFlownode(final SFlowNodeInstance sFlowNodeInstance) throws SBonitaException {
        try {
            final boolean isGateway = SFlowNodeType.GATEWAY.equals(sFlowNodeInstance.getType());
            if (isGateway) {
                SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(sFlowNodeInstance.getProcessDefinitionId());
                return gatewayInstanceService.checkMergingCondition(processDefinition, (SGatewayInstance) sFlowNodeInstance);
            }
            return true;
        } catch (final SBonitaException e) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.ERROR)) {
                logger.log(this.getClass(), TechnicalLogSeverity.ERROR, e);
            }
            throw e;
        }
    }

}
