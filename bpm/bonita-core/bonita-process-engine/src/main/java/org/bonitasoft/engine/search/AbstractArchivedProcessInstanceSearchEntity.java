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
package org.bonitasoft.engine.search;

import java.util.List;

import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public abstract class AbstractArchivedProcessInstanceSearchEntity extends AbstractSearchEntity<ArchivedProcessInstance, SAProcessInstance> {

    private SProcessDefinition sProcessDefinition;

    private ProcessDefinitionService processDefinitionService;

    public AbstractArchivedProcessInstanceSearchEntity(final SearchEntityDescriptor searchDescriptor, final SearchOptions options,
            final SProcessDefinition sProcessDefinition) {
        super(searchDescriptor, options);
        this.sProcessDefinition = sProcessDefinition;
    }

    public AbstractArchivedProcessInstanceSearchEntity(final SearchEntityDescriptor searchDescriptor, final SearchOptions options,
            final ProcessDefinitionService processDefinitionService) {
        super(searchDescriptor, options);
        this.processDefinitionService = processDefinitionService;
    }

    @Override
    public List<ArchivedProcessInstance> convertToClientObjects(final List<SAProcessInstance> serverObjects) {
        if (sProcessDefinition != null) {
            return ModelConvertor.toArchivedProcessInstances(serverObjects, sProcessDefinition);
        }
        return ModelConvertor.toArchivedProcessInstances(serverObjects, processDefinitionService);
    }

}
