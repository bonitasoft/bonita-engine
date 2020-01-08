/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractArchivedHumanTaskInstanceSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

public class SearchArchivedHumanTasksSupervisedBy extends AbstractArchivedHumanTaskInstanceSearchEntity {

    private final ActivityInstanceService activityInstanceService;

    private final Long supervisorId;

    public SearchArchivedHumanTasksSupervisedBy(final Long supervisorId,
            final ActivityInstanceService activityInstanceService,
            final FlowNodeStateManager flowNodeStateManager, final SearchEntityDescriptor searchDescriptor,
            final SearchOptions searchOptions) {
        super(searchDescriptor, searchOptions, flowNodeStateManager);
        this.supervisorId = supervisorId;
        this.activityInstanceService = activityInstanceService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return activityInstanceService.getNumberOfArchivedHumanTasksSupervisedBy(supervisorId, searchOptions);
    }

    @Override
    public List<SAHumanTaskInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return activityInstanceService.searchArchivedHumanTasksSupervisedBy(supervisorId, searchOptions);
    }

}
