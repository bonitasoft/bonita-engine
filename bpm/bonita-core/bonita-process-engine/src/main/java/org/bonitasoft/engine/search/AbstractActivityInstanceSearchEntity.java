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

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Yanyan Liu
 */
public abstract class AbstractActivityInstanceSearchEntity extends AbstractSearchEntity<ActivityInstance, SActivityInstance> {

    private final FlowNodeStateManager flowNodeStateManager;

    public AbstractActivityInstanceSearchEntity(final SearchEntityDescriptor searchDescriptor, final SearchOptions options,
            final FlowNodeStateManager flowNodeStateManager) {
        super(searchDescriptor, options);
        this.flowNodeStateManager = flowNodeStateManager;
    }

    @Override
    public List<ActivityInstance> convertToClientObjects(final List<SActivityInstance> serverObjects) {
        // invoke this method to get to different typed client object according to the server type
        return ModelConvertor.toActivityInstances(serverObjects, flowNodeStateManager);
    }

}
