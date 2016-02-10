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
package org.bonitasoft.engine.api.impl.resolver;

import java.util.List;

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
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface BusinessArchiveArtifactManager {

    /**
     * deploy a dedicated part of the process
     * e.g. load connectors
     * Must throw an exception is something is not resolved in the process
     *
     * @param businessArchive
     *        the business archive containing the dependency
     * @param processDefinition
     *        the process definition
     * @return true if the process is resolved for this deployer, false otherwise
     * @throws BonitaException
     */
    boolean deploy(BusinessArchive businessArchive, SProcessDefinition processDefinition) throws BonitaException, SBonitaException;

    /**
     * @param processDefinition
     *        the process definition
     * @return
     *         a list of resolution problems or an empty list is there is no issue for this artifact
     */
    List<Problem> checkResolution(final SProcessDefinition processDefinition);

    void delete(final SProcessDefinition processDefinition) throws SObjectModificationException, SBonitaReadException, SRecorderException;

    void exportToBusinessArchive(long processDefinitionId, BusinessArchiveBuilder businessArchiveBuilder) throws SBonitaException;
}
