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
package org.bonitasoft.engine.search;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.search.descriptor.SearchArchivedActivityInstanceDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Yanyan Liu
 */
public abstract class AbstractArchiveActivityInstanceSearchEntity extends AbstractSearchEntity<ArchivedActivityInstance, SAActivityInstance> {

    private final FlowNodeStateManager flowNodeStateManager;

    public AbstractArchiveActivityInstanceSearchEntity(final SearchArchivedActivityInstanceDescriptor searchDescriptor, final SearchOptions searchOptions,
            final FlowNodeStateManager flowNodeStateManager) {
        super(searchDescriptor, searchOptions);
        this.flowNodeStateManager = flowNodeStateManager;
    }

    @Override
    public List<ArchivedActivityInstance> convertToClientObjects(final List<SAActivityInstance> serverObjects) {
        return ModelConvertor.toArchivedActivityInstances(serverObjects, flowNodeStateManager);
    }

}
