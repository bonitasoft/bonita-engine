/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.bpm.flownode.ArchivedFlowElementInstance;
import org.bonitasoft.engine.core.process.instance.api.FlowElementInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowElementInstance;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchArchivedFlowElementInstanceDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Emmanuel Duchastenier
 */
public class SearchArchivedFlowElementInstances extends AbstractSearchEntity<ArchivedFlowElementInstance, SAFlowElementInstance> {

    private final FlowElementInstanceService flowElementInstanceService;

    public SearchArchivedFlowElementInstances(final FlowElementInstanceService flowElementInstanceService,
            final SearchArchivedFlowElementInstanceDescriptor searchDescriptor, final SearchOptions searchOptions) {
        super(searchDescriptor, searchOptions);
        this.flowElementInstanceService = flowElementInstanceService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return flowElementInstanceService.getNumberOfFlowElementInstances(SAFlowElementInstance.class, searchOptions);
    }

    @Override
    public List<SAFlowElementInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return flowElementInstanceService.searchArchivedFlowElementInstances(SAFlowElementInstance.class, searchOptions);
    }

    @Override
    public List<ArchivedFlowElementInstance> convertToClientObjects(final List<SAFlowElementInstance> serverObjects) {
        return ModelConvertor.toArchivedFlowElementInstances(serverObjects);
    }

}
