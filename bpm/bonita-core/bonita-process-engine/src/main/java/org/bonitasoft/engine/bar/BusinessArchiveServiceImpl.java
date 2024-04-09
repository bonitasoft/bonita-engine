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
package org.bonitasoft.engine.bar;

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;
import static org.bonitasoft.engine.form.FormMappingTarget.LEGACY;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SV6FormsDeployException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SDeletingEnabledProcessException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDeletionException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;

/**
 * @author Baptiste Mesta
 */
@Slf4j
public class BusinessArchiveServiceImpl implements BusinessArchiveService {

    private final ProcessDefinitionService processDefinitionService;
    private final DependencyService dependencyService;
    private final BusinessArchiveArtifactsManager businessArchiveArtifactsManager;
    private final ClassLoaderService classLoaderService;

    public BusinessArchiveServiceImpl(ProcessDefinitionService processDefinitionService,
            DependencyService dependencyService,
            BusinessArchiveArtifactsManager businessArchiveArtifactsManager, ClassLoaderService classLoaderService) {
        this.processDefinitionService = processDefinitionService;
        this.dependencyService = dependencyService;
        this.businessArchiveArtifactsManager = businessArchiveArtifactsManager;
        this.classLoaderService = classLoaderService;
    }

    @Override
    public SProcessDefinition deploy(BusinessArchive businessArchive)
            throws SObjectCreationException, SAlreadyExistsException {

        final DesignProcessDefinition designProcessDefinition = businessArchive.getProcessDefinition();
        SProcessDefinition sProcessDefinition;
        try {
            checkIfExists(designProcessDefinition);
            checkNoV6Forms(businessArchive);
            sProcessDefinition = processDefinitionService.store(designProcessDefinition);

            final boolean isResolved = businessArchiveArtifactsManager.resolveDependencies(businessArchive,
                    sProcessDefinition);
            if (isResolved) {
                processDefinitionService.resolveProcess(sProcessDefinition.getId());
            }
            classLoaderService.refreshClassLoaderAfterUpdate(identifier(ScopeType.PROCESS, sProcessDefinition.getId()));
        } catch (SV6FormsDeployException | SAlreadyExistsException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SObjectCreationException(e);
        }
        info(sProcessDefinition);
        return sProcessDefinition;
    }

    private void checkNoV6Forms(BusinessArchive businessArchive) throws SV6FormsDeployException {

        List<FormMappingDefinition> formMappings = businessArchive.getFormMappingModel().getFormMappings();
        for (FormMappingDefinition formMapping : formMappings) {
            if (formMapping.getTarget() == LEGACY) {
                throw new SV6FormsDeployException("The process contains v6 forms");
            }
        }
    }

    void checkIfExists(DesignProcessDefinition designProcessDefinition)
            throws SBonitaReadException, SAlreadyExistsException {
        try {
            processDefinitionService.getProcessDefinitionId(designProcessDefinition.getName(),
                    designProcessDefinition.getVersion());
            throw new SAlreadyExistsException("The process " + designProcessDefinition.getName() + " in version "
                    + designProcessDefinition.getVersion()
                    + " already exists.");
        } catch (final SProcessDefinitionNotFoundException e) {
            // ok
        }
    }

    void info(SProcessDefinition sProcessDefinition) {
        log.info("The user <{}> has installed process <{}> in version <{}> with id <{}>",
                SessionInfos.getUserNameFromSession(), sProcessDefinition.getName(),
                sProcessDefinition.getVersion(), sProcessDefinition.getId());
    }

    @Override
    public BusinessArchive export(long processDefinitionId)
            throws SBonitaException, InvalidBusinessArchiveFormatException {
        final DesignProcessDefinition designProcessDefinition = processDefinitionService
                .getDesignProcessDefinition(processDefinitionId);

        return businessArchiveArtifactsManager.exportBusinessArchive(processDefinitionId, designProcessDefinition);
    }

    @Override
    public void delete(long processDefinitionId)
            throws SProcessDefinitionNotFoundException, SObjectModificationException {
        try {
            final SProcessDefinition processDefinition = processDefinitionService
                    .getProcessDefinition(processDefinitionId);
            businessArchiveArtifactsManager.deleteDependencies(processDefinition);
            processDefinitionService.delete(processDefinition.getId());
            classLoaderService.removeLocalClassloader(identifier(ScopeType.PROCESS, processDefinition.getId()));
        } catch (SBonitaReadException | SProcessDeletionException | SDeletingEnabledProcessException
                | SRecorderException | SClassLoaderException e) {
            throw new SObjectModificationException(
                    "Unable to delete the process definition <" + processDefinitionId + ">", e);
        }
    }
}
