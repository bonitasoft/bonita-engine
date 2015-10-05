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
package org.bonitasoft.engine.api.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.impl.transaction.process.DeleteProcess;
import org.bonitasoft.engine.api.impl.transaction.process.DisableProcess;
import org.bonitasoft.engine.bpm.parameter.ParameterCriterion;
import org.bonitasoft.engine.bpm.parameter.ParameterInstance;
import org.bonitasoft.engine.bpm.parameter.impl.ParameterImpl;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Matthieu Chaffotte
 */
// Uncomment the "implements" when this delegate implements all the methods.
public class ProcessManagementAPIImplDelegate /* implements ProcessManagementAPI */{

    private static PlatformServiceAccessor getPlatformServiceAccessor() {
        try {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SProcessDefinition getServerProcessDefinition(final long processDefinitionId, final ProcessDefinitionService processDefinitionService)
            throws SProcessDefinitionNotFoundException, SBonitaReadException {
        return processDefinitionService.getProcessDefinition(processDefinitionId);
    }

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteProcessDefinition(final long processDefinitionId) throws SBonitaException, BonitaHomeNotSetException, IOException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();

        final DeleteProcess deleteProcess = instantiateDeleteProcessTransactionContent(processDefinitionId);
        deleteProcess.execute();

        BonitaHomeServer.getInstance().getProcessManager().deleteProcess(tenantAccessor.getTenantId(), processDefinitionId);

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "The user <" + SessionInfos.getUserNameFromSession() + "> has deleted process with id = <"
                    + processDefinitionId + ">");
        }
    }

    protected DeleteProcess instantiateDeleteProcessTransactionContent(final long processId) {
        return new DeleteProcess(getTenantAccessor(), processId);
    }

    public void disableProcess(final long processId) throws SProcessDefinitionNotFoundException, SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final PlatformServiceAccessor platformServiceAccessor = getPlatformServiceAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final EventInstanceService eventInstanceService = tenantAccessor.getEventInstanceService();
        final SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
        final TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        final DisableProcess disableProcess = new DisableProcess(processDefinitionService, processId, eventInstanceService, schedulerService, logger,
                SessionInfos.getUserNameFromSession());
        disableProcess.execute();
    }

    public void purgeClassLoader(final long processDefinitionId) throws ProcessDefinitionNotFoundException, UpdateException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            final SProcessDefinitionDeployInfo processDeploymentInfo = processDefinitionService.getProcessDeploymentInfo(processDefinitionId);
            if (!ActivationState.DISABLED.name().equals(processDeploymentInfo.getActivationState())) {
                throw new UpdateException("Purge can only be done on a disabled process");
            }
            final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
            final long numberOfProcessInstances = processInstanceService.getNumberOfProcessInstances(processDefinitionId);
            if (numberOfProcessInstances != 0) {
                throw new UpdateException("Purge can only be done on a disabled process with no running instances");
            }
            tenantAccessor.getClassLoaderService().removeLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    public List<ParameterInstance> getParameterInstances(final long processDefinitionId, final int startIndex, final int maxResults,
            final ParameterCriterion sort) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            OrderBy order;
            switch (sort) {
                case NAME_DESC:
                    order = OrderBy.NAME_DESC;
                    break;
                default:
                    order = OrderBy.NAME_ASC;
                    break;
            }

            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(processDefinitionId, processDefinitionService);
            if (sProcessDefinition.getParameters().isEmpty()) {
                return Collections.emptyList();
            }
            final List<SParameter> parameters = parameterService.get(processDefinitionId, startIndex, maxResults, order);
            final List<ParameterInstance> paramterInstances = new ArrayList<ParameterInstance>();
            for (int i = 0; i < parameters.size(); i++) {
                final SParameter parameter = parameters.get(i);
                final String name = parameter.getName();
                final String value = parameter.getValue();
                final SParameterDefinition parameterDefinition = sProcessDefinition.getParameter(name);
                final String description = parameterDefinition.getDescription();
                final String type = parameterDefinition.getType();
                paramterInstances.add(new ParameterImpl(name, description, value, type));
            }
            return paramterInstances;
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    public int getNumberOfParameterInstances(final long processDefinitionId) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(processDefinitionId, processDefinitionService);
            return sProcessDefinition.getParameters().size();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    public ParameterInstance getParameterInstance(final long processDefinitionId, final String parameterName) throws NotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ParameterService parameterService = tenantAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(processDefinitionId, processDefinitionService);
            final SParameter parameter = parameterService.get(processDefinitionId, parameterName);
            final String name = parameter.getName();
            final String value = parameter.getValue();
            final SParameterDefinition parameterDefinition = sProcessDefinition.getParameter(name);
            final String description = parameterDefinition.getDescription();
            final String type = parameterDefinition.getType();
            return new ParameterImpl(name, description, value, type);
        } catch (final SParameterProcessNotFoundException e) {
            throw new NotFoundException("the parameter with name " + parameterName + " and process with id " + processDefinitionId + " was not found.");
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

}
