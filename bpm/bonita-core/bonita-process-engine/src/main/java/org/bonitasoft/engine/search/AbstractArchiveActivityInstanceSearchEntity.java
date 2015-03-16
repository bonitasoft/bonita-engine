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

import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SASendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAUserTaskInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.search.descriptor.SearchArchivedActivityInstanceDescriptor;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public abstract class AbstractArchiveActivityInstanceSearchEntity extends AbstractSearchEntity<ArchivedActivityInstance, SAActivityInstance> {

    private final FlowNodeStateManager flowNodeStateManager;

    private final Class<? extends PersistentObject> entityClass;

    public AbstractArchiveActivityInstanceSearchEntity(final SearchArchivedActivityInstanceDescriptor searchDescriptor, final SearchOptions searchOptions,
            final FlowNodeStateManager flowNodeStateManager) {
        super(searchDescriptor, searchOptions);
        this.flowNodeStateManager = flowNodeStateManager;
        entityClass = getEntityClass(searchOptions);
    }

    @Override
    public List<ArchivedActivityInstance> convertToClientObjects(final List<SAActivityInstance> serverObjects) {
        return ModelConvertor.toArchivedActivityInstances(serverObjects, flowNodeStateManager);
    }

    protected Class<? extends PersistentObject> getEntityClass(final SearchOptions searchOptions) {
        Class<? extends PersistentObject> entityClass = SAActivityInstance.class;
        final SearchFilter searchFilter = getSearchFilter(searchOptions, ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE);
        if (searchFilter != null) {
            final FlowNodeType activityType = (FlowNodeType) searchFilter.getValue();
            if (activityType != null) {
                switch (activityType) {
                    case AUTOMATIC_TASK:
                        entityClass = SAAutomaticTaskInstance.class;
                        break;
                    case MANUAL_TASK:
                        entityClass = SAManualTaskInstance.class;
                        break;
                    case USER_TASK:
                        entityClass = SAUserTaskInstance.class;
                        break;
                    case HUMAN_TASK:
                        entityClass = SAHumanTaskInstance.class;
                        break;
                    case RECEIVE_TASK:
                        entityClass = SAReceiveTaskInstance.class;
                        break;
                    case SEND_TASK:
                        entityClass = SASendTaskInstance.class;
                        break;
                    default:
                        entityClass = SAActivityInstance.class;
                        break;
                }
                searchOptions.getFilters().remove(searchFilter);
            }
        }
        return entityClass;
    }

    protected Class<? extends PersistentObject> getEntityClass() {
        return entityClass;
    }
}
