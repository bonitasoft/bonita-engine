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

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SReceiveTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSendTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SSubProcessActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.service.ModelConvertor;

/**
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public abstract class AbstractActivityInstanceSearchEntity extends AbstractSearchEntity<ActivityInstance, SActivityInstance> {

    private final FlowNodeStateManager flowNodeStateManager;

    private final Class<? extends PersistentObject> entityClass;

    public AbstractActivityInstanceSearchEntity(final SearchEntityDescriptor searchDescriptor, final SearchOptions options,
            final FlowNodeStateManager flowNodeStateManager) throws SBonitaReadException {
        super(searchDescriptor, options);
        this.flowNodeStateManager = flowNodeStateManager;
        entityClass = getEntityClass(options);
    }

    @Override
    public List<ActivityInstance> convertToClientObjects(final List<SActivityInstance> serverObjects) {
        // invoke this method to get to different typed client object according to the server type
        return ModelConvertor.toActivityInstances(serverObjects, flowNodeStateManager);
    }

    protected Class<? extends PersistentObject> getEntityClass(final SearchOptions searchOptions) throws SBonitaReadException {
        Class<? extends PersistentObject> entityClass = SActivityInstance.class;
        final SearchFilter searchFilter = getSearchFilter(searchOptions, ActivityInstanceSearchDescriptor.ACTIVITY_TYPE);
        if (searchFilter != null) {
            final FlowNodeType activityType = (FlowNodeType) searchFilter.getValue();
            if (activityType != null) {
                switch (activityType) {
                    case AUTOMATIC_TASK:
                        entityClass = SAutomaticTaskInstance.class;
                        break;
                    case MANUAL_TASK:
                        entityClass = SManualTaskInstance.class;
                        break;
                    case USER_TASK:
                        entityClass = SUserTaskInstance.class;
                        break;
                    case HUMAN_TASK:
                        entityClass = SHumanTaskInstance.class;
                        break;
                    case RECEIVE_TASK:
                        entityClass = SReceiveTaskInstance.class;
                        break;
                    case SEND_TASK:
                        entityClass = SSendTaskInstance.class;
                        break;
                    case CALL_ACTIVITY:
                        entityClass = SCallActivityInstance.class;
                        break;
                    case MULTI_INSTANCE_ACTIVITY:
                        entityClass = SMultiInstanceActivityInstance.class;
                        break;
                    case SUB_PROCESS:
                        entityClass = SSubProcessActivityInstance.class;
                        break;
                    case LOOP_ACTIVITY:
                        entityClass = SLoopActivityInstance.class;
                        break;
                    default:
                        throw new SBonitaReadException("You're searching for a " + activityType.name() + " which is not an activity type, using the ActivityInstanceSearch feature. Ensure you are using the correct search method for your needs.");
                }
                searchOptions.getFilters().remove(searchFilter);
            }
        }
        return entityClass;
    }

    @Override
    protected void validateQuery(final SearchOptions searchOptions) throws SBonitaReadException {
        validateActivityTypeFilterUnicity(searchOptions);
    }

    private void validateActivityTypeFilterUnicity(SearchOptions searchOptions) throws SBonitaReadException {
        for (final SearchFilter searchFilter : searchOptions.getFilters()) {
            if (ActivityInstanceSearchDescriptor.ACTIVITY_TYPE.equals(searchFilter.getField())) {
                throw new SBonitaReadException("Invalid query, filtering several times on 'ActivityInstanceSearchDescriptor.ACTIVITY_TYPE' is not supported.");
            }
        }
    }

    protected Class<? extends PersistentObject> getEntityClass() {
        return entityClass;
    }

}
