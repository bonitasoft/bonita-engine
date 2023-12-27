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
package org.bonitasoft.engine.api.impl;

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;
import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.impl.transaction.process.DisableProcess;
import org.bonitasoft.engine.bpm.parameter.ParameterCriterion;
import org.bonitasoft.engine.bpm.parameter.ParameterInstance;
import org.bonitasoft.engine.bpm.parameter.impl.ParameterImpl;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SParameterDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @author Matthieu Chaffotte
 */
// Uncomment the "implements" when this delegate implements all the methods.
@Slf4j
public class ProcessManagementAPIImplDelegate /* implements ProcessManagementAPI */ {

    private static final ProcessManagementAPIImplDelegate instance = new ProcessManagementAPIImplDelegate();

    public static ProcessManagementAPIImplDelegate getInstance() {
        return instance;
    }

    protected ServiceAccessor getServiceAccessor() {
        try {
            return ServiceAccessorFactory.getInstance().createServiceAccessor();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static SProcessDefinition getServerProcessDefinition(final long processDefinitionId,
            final ProcessDefinitionService processDefinitionService)
            throws SProcessDefinitionNotFoundException, SBonitaReadException {
        return processDefinitionService.getProcessDefinition(processDefinitionId);
    }

    public void deleteProcessDefinition(final long processDefinitionId)
            throws SBonitaException, BonitaHomeNotSetException, IOException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        serviceAccessor.getBusinessArchiveService().delete(processDefinitionId);

        log.info("The user <" + SessionInfos.getUserNameFromSession() + "> has deleted process with id = <"
                + processDefinitionId + ">");
    }

    public void disableProcess(final long processId) throws SBonitaException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();
        final EventInstanceService eventInstanceService = serviceAccessor.getEventInstanceService();
        final SchedulerService schedulerService = serviceAccessor.getSchedulerService();

        final DisableProcess disableProcess = new DisableProcess(processDefinitionService, processId,
                eventInstanceService,
                schedulerService, SessionInfos.getUserNameFromSession());
        disableProcess.execute();
    }

    public void purgeClassLoader(final long processDefinitionId)
            throws ProcessDefinitionNotFoundException, UpdateException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();
        try {
            final SProcessDefinitionDeployInfo processDeploymentInfo = processDefinitionService
                    .getProcessDeploymentInfo(processDefinitionId);
            if (!ActivationState.DISABLED.name().equals(processDeploymentInfo.getActivationState())) {
                throw new UpdateException("Purge can only be done on a disabled process");
            }
            final ProcessInstanceService processInstanceService = serviceAccessor.getProcessInstanceService();
            final long numberOfProcessInstances = processInstanceService
                    .getNumberOfProcessInstances(processDefinitionId);
            if (numberOfProcessInstances != 0) {
                throw new UpdateException("Purge can only be done on a disabled process with no running instances");
            }
            serviceAccessor.getClassLoaderService().removeLocalClassloader(identifier(PROCESS, processDefinitionId));
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        } catch (SClassLoaderException e) {
            throw new UpdateException(e);
        }
    }

    public List<ParameterInstance> getParameterInstances(final long processDefinitionId, final int startIndex,
            final int maxResults, final ParameterCriterion sort) {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final ParameterService parameterService = serviceAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();
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

            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(processDefinitionId,
                    processDefinitionService);
            if (sProcessDefinition.getParameters().isEmpty()) {
                return Collections.emptyList();
            }
            final List<SParameter> parameters = parameterService.get(processDefinitionId, startIndex, maxResults,
                    order);
            final List<ParameterInstance> parameterInstances = new ArrayList<>();
            for (final SParameter parameter : parameters) {
                final String name = parameter.getName();
                final String value = parameter.getValue();
                final SParameterDefinition parameterDefinition = sProcessDefinition.getParameter(name);
                final String description = parameterDefinition.getDescription();
                final String type = parameterDefinition.getType();
                parameterInstances.add(new ParameterImpl(name, description, value, type));
            }
            return parameterInstances;
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    public int getNumberOfParameterInstances(final long processDefinitionId) {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(processDefinitionId,
                    processDefinitionService);
            return sProcessDefinition.getParameters().size();
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    public ParameterInstance getParameterInstance(final long processDefinitionId, final String parameterName)
            throws NotFoundException {
        final ServiceAccessor serviceAccessor = getServiceAccessor();
        final ParameterService parameterService = serviceAccessor.getParameterService();
        final ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();
        try {
            final SProcessDefinition sProcessDefinition = getServerProcessDefinition(processDefinitionId,
                    processDefinitionService);
            final SParameter parameter = parameterService.get(processDefinitionId, parameterName);
            if (parameter == null) {
                throw new NotFoundException("the parameter with name " + parameterName + " and process with id "
                        + processDefinitionId + " was not found.");
            }
            final String name = parameter.getName();
            final String value = parameter.getValue();
            final SParameterDefinition parameterDefinition = sProcessDefinition.getParameter(name);
            final String description = parameterDefinition.getDescription();
            final String type = parameterDefinition.getType();
            return new ParameterImpl(name, description, value, type);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

}
