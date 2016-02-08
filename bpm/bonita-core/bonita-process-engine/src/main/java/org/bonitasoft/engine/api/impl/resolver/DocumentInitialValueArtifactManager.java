/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.api.impl.resolver;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.SRecorderException;

/**
 * @author Baptiste Mesta
 */
public class DocumentInitialValueArtifactManager extends BARResourceArtifactManager {

    public static final String FOLDER = "documents";

    public DocumentInitialValueArtifactManager(ProcessResourcesService processResourcesService) {
        super(processResourcesService);
    }

    @Override
    public boolean deploy(BusinessArchive businessArchive, SProcessDefinition processDefinition) throws BonitaException, SBonitaException {
        saveResources(businessArchive, processDefinition, FOLDER, BARResourceType.DOCUMENT);
        return true;
    }

    @Override
    public List<Problem> checkResolution(SProcessDefinition processDefinition) {
        return Collections.emptyList();
    }

    @Override
    public void delete(SProcessDefinition processDefinition) throws SObjectModificationException, SBonitaReadException, SRecorderException {
        processResourcesService.removeAll(processDefinition.getId(),BARResourceType.DOCUMENT);
    }

    @Override
    public void exportToBusinessArchive(long processDefinitionId, BusinessArchiveBuilder businessArchiveBuilder) throws SBonitaException {
        exportResourcesToBusinessArchive(processDefinitionId, businessArchiveBuilder, BARResourceType.DOCUMENT);
    }

    @Override
    void addToBusinessArchive(BusinessArchiveBuilder businessArchiveBuilder, BarResource resource) {
        businessArchiveBuilder.addDocumentResource(resource);
    }
}
