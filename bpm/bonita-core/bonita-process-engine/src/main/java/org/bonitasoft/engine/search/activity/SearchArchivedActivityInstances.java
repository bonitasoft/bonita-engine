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
package org.bonitasoft.engine.search.activity;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractArchiveActivityInstanceSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchArchivedActivityInstanceDescriptor;

/**
 * @author Yanyan Liu
 * @author Celine Souchet
 */
public class SearchArchivedActivityInstances extends AbstractArchiveActivityInstanceSearchEntity {

    private final ActivityInstanceService activityInstanceService;

    public SearchArchivedActivityInstances(final ActivityInstanceService activityInstanceService, final FlowNodeStateManager flowNodeStateManager,
            final SearchArchivedActivityInstanceDescriptor searchDescriptor, final SearchOptions searchOptions) {
        super(searchDescriptor, searchOptions, flowNodeStateManager);
        this.activityInstanceService = activityInstanceService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return activityInstanceService.getNumberOfArchivedActivityInstances(getEntityClass(), searchOptions);
    }

    @Override
    public List<SAActivityInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return activityInstanceService.searchArchivedActivityInstances(getEntityClass(), searchOptions);
    }

}
