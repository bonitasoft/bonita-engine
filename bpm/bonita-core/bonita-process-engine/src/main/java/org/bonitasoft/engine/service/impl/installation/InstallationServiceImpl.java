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
package org.bonitasoft.engine.service.impl.installation;

import static org.bonitasoft.engine.commons.ExceptionUtils.printLightWeightStacktrace;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDisablementException;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.parameter.ParameterService;
import org.bonitasoft.engine.service.InstallationFailedException;
import org.bonitasoft.engine.service.InstallationService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InstallationServiceImpl implements InstallationService {

    private final ProcessDefinitionService processDefinitionService;
    private final ParameterService parameterService;
    private final BusinessArchiveArtifactsManager businessArchiveArtifactsManager;
    private final EventsHandler eventsHandler;

    public InstallationServiceImpl(ProcessDefinitionService processDefinitionService, ParameterService parameterService,
            BusinessArchiveArtifactsManager dependencyResolver, EventsHandler eventsHandler) {
        this.processDefinitionService = processDefinitionService;
        this.parameterService = parameterService;
        this.businessArchiveArtifactsManager = dependencyResolver;
        this.eventsHandler = eventsHandler;
    }

    @Override
    public void install(byte[] binaries, byte[] configuration) throws InstallationFailedException {
        if (binaries != null) {
            throw new IllegalStateException("binaries archive is not yet implemented");
        }
        if (configuration != null) {
            installConfiguration(configuration);
        }
    }

    private void installConfiguration(byte[] configuration) throws InstallationFailedException {
        try (ConfigurationArchive confArchive = new ConfigurationArchive(configuration)) {
            for (ProcessConfiguration processConfiguration : confArchive.getProcessConfigurations()) {
                String processName = processConfiguration.getName();
                String processVersion = processConfiguration.getVersion();
                try {
                    long pDefId = processDefinitionService.getProcessDefinitionId(processName, processVersion);
                    parameterService.merge(pDefId, processConfiguration.getParameters());
                    final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(pDefId);
                    final List<Problem> problems = businessArchiveArtifactsManager
                            .getProcessResolutionProblems(processDefinition);
                    final SProcessDefinitionDeployInfo pInfo = processDefinitionService
                            .getProcessDeploymentInfo(pDefId);
                    if (problems.isEmpty()) {
                        businessArchiveArtifactsManager.changeResolutionStatus(pDefId, processDefinitionService,
                                true);
                        if (ActivationState.DISABLED.name().equals(pInfo.getActivationState())) {
                            log.info("Configuration of process {}-{} is now complete. Enabling it.",
                                    processName, processVersion);
                            enableProcess(pDefId, processName, processVersion);
                        }
                    } else {
                        businessArchiveArtifactsManager.changeResolutionStatus(pDefId, processDefinitionService,
                                false);
                        if (ActivationState.ENABLED.name().equals(pInfo.getActivationState())) {
                            disableProcess(pDefId, processName, processVersion);
                        }
                    }
                } catch (SProcessDefinitionNotFoundException e) {
                    // Process configuration may be present in bconf file for non deployed processes filtered by a deploy.json file at deploy time (see la-deployer)
                    // don't rethrow ex to avoid breaking ongoing configuration deployment.
                    log.warn("Configuration parameter found in BCONF file for non existing process " + processName + "-"
                            + processVersion + ". Skipping those parameters.");
                }
            }
        } catch (Exception e) {
            log.error("Failed to apply configuration.", e);
            throw new InstallationFailedException("Failed to apply configuration.", e);
        }
    }

    private void enableProcess(long processDefinitionId, String processName, String processVersion) {
        try {
            processDefinitionService.enableProcess(processDefinitionId, false);
            handleStartEvents(processDefinitionId);
        } catch (SBonitaException e) {
            // It's not mandatory to enable the process here, simply log the action
            log.warn("Failed to enable the process " + processName + " in version " + processVersion
                    + " after deploying the configuration",
                    printLightWeightStacktrace(e));
        }
    }

    private void handleStartEvents(long processDefinitionId) throws SBonitaException {
        final SProcessDefinition sProcessDefinition = processDefinitionService
                .getProcessDefinition(processDefinitionId);
        final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
        for (final SStartEventDefinition sStartEventDefinition : processContainer.getStartEvents()) {
            eventsHandler.handleCatchEvent(sProcessDefinition, sStartEventDefinition, null);
        }
    }

    private void disableProcess(long processDefinitionId, String processName, String processVersion) {
        try {
            processDefinitionService.disableProcess(processDefinitionId, false);
        } catch (SProcessDefinitionNotFoundException | SProcessDisablementException e) {
            // It's not mandatory to disable the process here, simply log the action
            log.warn("Failed to disable the process " + processName + " in version " + processVersion
                    + " after deploying the configuration",
                    printLightWeightStacktrace(e));
        }
    }
}
