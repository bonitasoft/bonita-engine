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
package org.bonitasoft.engine.search.flownode;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchArchivedFlowNodeInstanceDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Emmanuel Duchastenier
 */
public class SearchArchivedFlowNodeInstances extends AbstractSearchEntity<ArchivedFlowNodeInstance, SAFlowNodeInstance> {

    private final FlowNodeInstanceService flowNodeInstanceService;

    private final FlowNodeStateManager flowNodeStateManager;

    public SearchArchivedFlowNodeInstances(final FlowNodeInstanceService flowNodeInstanceService, final FlowNodeStateManager flowNodeStateManager,
            final SearchArchivedFlowNodeInstanceDescriptor searchDescriptor, final SearchOptions searchOptions) {
        super(searchDescriptor, searchOptions);
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.flowNodeStateManager = flowNodeStateManager;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return flowNodeInstanceService.getNumberOfArchivedFlowNodeInstances(SAFlowNodeInstance.class, searchOptions);
    }

    @Override
    public List<SAFlowNodeInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return flowNodeInstanceService.searchArchivedFlowNodeInstances(SAFlowNodeInstance.class, searchOptions);
    }

    @Override
    public List<ArchivedFlowNodeInstance> convertToClientObjects(final List<SAFlowNodeInstance> serverObjects) {
        return ModelConvertor.toArchivedFlowNodeInstances(serverObjects, flowNodeStateManager);
    }

}
