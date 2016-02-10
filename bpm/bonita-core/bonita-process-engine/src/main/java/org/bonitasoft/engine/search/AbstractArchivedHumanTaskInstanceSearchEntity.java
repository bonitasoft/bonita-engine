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

import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Baptiste Mesta
 */
public abstract class AbstractArchivedHumanTaskInstanceSearchEntity extends AbstractSearchEntity<ArchivedHumanTaskInstance, SAHumanTaskInstance> {

    private final FlowNodeStateManager flowNodeStateManager;

    public AbstractArchivedHumanTaskInstanceSearchEntity(final SearchEntityDescriptor searchDescriptor, final SearchOptions options,
            final FlowNodeStateManager flowNodeStateManager) {
        super(searchDescriptor, options);
        this.flowNodeStateManager = flowNodeStateManager;
    }

    @Override
    public List<ArchivedHumanTaskInstance> convertToClientObjects(final List<SAHumanTaskInstance> serverObjects) {
        return ModelConvertor.toArchivedHumanTaskInstances(serverObjects, flowNodeStateManager);
    }

}
