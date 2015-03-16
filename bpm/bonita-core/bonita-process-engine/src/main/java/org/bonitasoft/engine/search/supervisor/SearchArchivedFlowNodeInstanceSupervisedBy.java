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
package org.bonitasoft.engine.search.supervisor;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Celine Souchet
 */
public class SearchArchivedFlowNodeInstanceSupervisedBy extends AbstractSearchEntity<ArchivedFlowNodeInstance, SAFlowNodeInstance> {

    private final FlowNodeInstanceService flowNodeInstanceService;

    private final FlowNodeStateManager flowNodeStateManager;

    private final Long supervisorId;

    public SearchArchivedFlowNodeInstanceSupervisedBy(final Long supervisorId, final FlowNodeInstanceService flowNodeInstanceService,
            final FlowNodeStateManager flowNodeStateManager, final SearchEntityDescriptor searchDescriptor, final SearchOptions searchOptions) {
        super(searchDescriptor, searchOptions);
        this.supervisorId = supervisorId;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.flowNodeStateManager = flowNodeStateManager;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return flowNodeInstanceService.getNumberOfArchivedFlowNodeInstancesSupervisedBy(supervisorId, SAFlowNodeInstance.class, searchOptions);
    }

    @Override
    public List<SAFlowNodeInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return flowNodeInstanceService.searchArchivedFlowNodeInstancesSupervisedBy(supervisorId, SAFlowNodeInstance.class, searchOptions);
    }

    @Override
    public List<ArchivedFlowNodeInstance> convertToClientObjects(final List<SAFlowNodeInstance> serverObjects) {
        return ModelConvertor.toArchivedFlowNodeInstances(serverObjects, flowNodeStateManager);
    }

}
