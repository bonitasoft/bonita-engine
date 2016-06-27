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
package org.bonitasoft.engine.api.impl.transaction.process;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessEnablementException;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.resources.SBARResource;
import org.bonitasoft.platform.configuration.ConfigurationService;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public final class EnableProcess implements TransactionContent {

    public static final String RESOURCES_FORMS_SECURITY_CONFIG_PROPERTIES = "forms/security-config.properties";

    private final ProcessDefinitionService processDefinitionService;

    private final ConfigurationService configurationService;
    private final ProcessResourcesService processResourcesService;
    private final long processId;

    private final EventsHandler eventsHandler;

    private final TechnicalLoggerService logger;

    private final String userName;
    private final long tenantId;

    public EnableProcess(final ProcessDefinitionService processDefinitionService, ConfigurationService configurationService,
            ProcessResourcesService processResourcesService, final long processId,
            final EventsHandler eventsHandler,
            final TechnicalLoggerService logger, final String userName, long tenantId) {
        this.processDefinitionService = processDefinitionService;
        this.configurationService = configurationService;
        this.processResourcesService = processResourcesService;
        this.processId = processId;
        this.eventsHandler = eventsHandler;
        this.logger = logger;
        this.userName = userName;
        this.tenantId = tenantId;
    }

    @Override
    public void execute() throws SBonitaException {
        final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processId);
        handleAutoLoginConfiguration(sProcessDefinition);
        handleStartEvents(sProcessDefinition);
        processDefinitionService.enableProcessDeploymentInfo(processId);

        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "The user <" + userName + "> has enabled process <" + sProcessDefinition.getName()
                    + "> in version <" + sProcessDefinition.getVersion() + "> with id <" + sProcessDefinition.getId() + ">");
        }
    }

    private void handleAutoLoginConfiguration(SProcessDefinition sProcessDefinition) throws SBonitaReadException {
        AutoLoginConfigurationHelper autoLoginConfigurationHelper = new AutoLoginConfigurationHelper(configurationService, tenantId, sProcessDefinition);
        try {
            final SBARResource sbarResource = processResourcesService.get(processId, BARResourceType.EXTERNAL,
                    RESOURCES_FORMS_SECURITY_CONFIG_PROPERTIES);
            if (sbarResource != null) {
                autoLoginConfigurationHelper.enableAutoLogin(getPropertiesFromBarResource(sbarResource));
            }
        } catch (IOException | SProcessEnablementException e) {
            throw new SBonitaReadException(e);
        }

    }

    private Properties getPropertiesFromBarResource(SBARResource sbarResource) throws IOException {
        Properties properties = new Properties();
        final byte[] content = sbarResource.getContent();
        properties.load(new ByteArrayInputStream(content));
        return properties;
    }

    private void handleStartEvents(final SProcessDefinition sProcessDefinition) throws SBonitaException {
        final SFlowElementContainerDefinition processContainer = sProcessDefinition.getProcessContainer();
        for (final SStartEventDefinition sStartEventDefinition : processContainer.getStartEvents()) {
            eventsHandler.handleCatchEvent(sProcessDefinition, sStartEventDefinition, null);
        }
    }

}
