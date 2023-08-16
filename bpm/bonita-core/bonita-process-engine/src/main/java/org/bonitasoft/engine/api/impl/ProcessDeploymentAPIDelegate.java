/**
 * Copyright (C) 2023 Bonitasoft S.A.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.transaction.process.EnableProcess;
import org.bonitasoft.engine.bar.BusinessArchiveService;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.*;
import org.bonitasoft.engine.commons.exceptions.SAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SV6FormsDeployException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchProcessDefinitionsDescriptor;
import org.bonitasoft.engine.search.process.SearchProcessDeploymentInfos;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Emmanuel Duchastenier
 */
public class ProcessDeploymentAPIDelegate {

    private static final ProcessDeploymentAPIDelegate instance = new ProcessDeploymentAPIDelegate();

    private ProcessDeploymentAPIDelegate() {
    }

    public static ProcessDeploymentAPIDelegate getInstance() {
        return instance;
    }

    protected TenantServiceAccessor getTenantServiceAccessor() {
        return APIUtils.getTenantAccessor();
    }

    public ProcessDefinition deploy(final BusinessArchive businessArchive)
            throws ProcessDeployException, AlreadyExistsException {
        validateBusinessArchive(businessArchive);
        final BusinessArchiveService businessArchiveService = getTenantServiceAccessor().getBusinessArchiveService();
        try {
            return ModelConvertor.toProcessDefinition(businessArchiveService.deploy(businessArchive));
        } catch (SV6FormsDeployException e) {
            throw new V6FormDeployException(e);
        } catch (SObjectCreationException e) {
            throw new ProcessDeployException(e);
        } catch (SAlreadyExistsException e) {
            throw new AlreadyExistsException(e.getMessage());
        }
    }

    public ProcessDefinition deployAndEnableProcess(final BusinessArchive businessArchive)
            throws ProcessDeployException, ProcessEnablementException, AlreadyExistsException {
        final ProcessDefinition processDefinition = deploy(businessArchive);
        try {
            enableProcess(processDefinition.getId());
        } catch (final ProcessDefinitionNotFoundException e) {
            throw new ProcessEnablementException(e.getMessage());
        }
        return processDefinition;
    }

    void validateBusinessArchive(BusinessArchive businessArchive) throws ProcessDeployException {
        for (Map.Entry<String, byte[]> resource : businessArchive.getResources().entrySet()) {
            final byte[] resourceContent = resource.getValue();
            if (resourceContent == null || resourceContent.length == 0) {
                throw new ProcessDeployException(
                        "The BAR file you are trying to deploy contains an empty file: " + resource.getKey()
                                + ". The process cannot be deployed. Fix it or remove it from the BAR.");
            }
        }
    }

    public void enableProcess(final long processDefinitionId)
            throws ProcessDefinitionNotFoundException, ProcessEnablementException {
        final ProcessDefinitionService processDefinitionService = getTenantServiceAccessor()
                .getProcessDefinitionService();
        final EventsHandler eventsHandler = getTenantServiceAccessor().getEventsHandler();
        try {
            new EnableProcess(processDefinitionService, processDefinitionId,
                    eventsHandler, SessionInfos.getUserNameFromSession()).execute();
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final Exception e) {
            throw new ProcessEnablementException(e);
        }
    }

    public long getProcessDefinitionId(final String name, final String version)
            throws ProcessDefinitionNotFoundException {
        try {
            return getTenantServiceAccessor().getProcessDefinitionService().getProcessDefinitionId(name, version);
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    public List<Problem> getProcessResolutionProblems(final long processDefinitionId)
            throws ProcessDefinitionNotFoundException {
        final TenantServiceAccessor tenantAccessor = getTenantServiceAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            return tenantAccessor.getBusinessArchiveArtifactsManager().getProcessResolutionProblems(processDefinition);
        } catch (final SProcessDefinitionNotFoundException | SBonitaReadException e) {
            throw new ProcessDefinitionNotFoundException(e);
        }
    }

    public Map<Long, ProcessDeploymentInfo> getProcessDeploymentInfosFromIds(final List<Long> processDefinitionIds) {
        final TenantServiceAccessor tenantAccessor = getTenantServiceAccessor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        try {
            final List<SProcessDefinitionDeployInfo> processDefinitionDeployInfos = processDefinitionService
                    .getProcessDeploymentInfos(processDefinitionIds);
            final List<ProcessDeploymentInfo> processDeploymentInfos = ModelConvertor
                    .toProcessDeploymentInfo(processDefinitionDeployInfos);
            final Map<Long, ProcessDeploymentInfo> mProcessDefinitions = new HashMap<>();
            for (final ProcessDeploymentInfo p : processDeploymentInfos) {
                mProcessDefinitions.put(p.getProcessId(), p);
            }
            return mProcessDefinitions;
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    public ProcessDeploymentInfo getProcessDeploymentInfo(final long processDefinitionId)
            throws ProcessDefinitionNotFoundException {
        final ProcessDefinitionService processDefinitionService = getTenantServiceAccessor()
                .getProcessDefinitionService();
        try {
            return ModelConvertor
                    .toProcessDeploymentInfo(processDefinitionService.getProcessDeploymentInfo(processDefinitionId));
        } catch (final SProcessDefinitionNotFoundException e) {
            throw new ProcessDefinitionNotFoundException(e);
        } catch (final SBonitaReadException e) {
            throw new RetrieveException(e);
        }
    }

    public SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfos(final SearchOptions searchOptions)
            throws SearchException {
        final TenantServiceAccessor tenantAccessor = getTenantServiceAccessor();

        final SearchEntitiesDescriptor searchEntitiesDescriptor = tenantAccessor.getSearchEntitiesDescriptor();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SearchProcessDefinitionsDescriptor searchDescriptor = searchEntitiesDescriptor
                .getSearchProcessDefinitionsDescriptor();
        final SearchProcessDeploymentInfos transactionSearch = new SearchProcessDeploymentInfos(
                processDefinitionService, searchDescriptor, searchOptions);
        try {
            transactionSearch.execute();
        } catch (final SBonitaException e) {
            throw new SearchException("Can't get processDefinition's executing searchProcessDefinitions()", e);
        }
        return transactionSearch.getResult();
    }

}
