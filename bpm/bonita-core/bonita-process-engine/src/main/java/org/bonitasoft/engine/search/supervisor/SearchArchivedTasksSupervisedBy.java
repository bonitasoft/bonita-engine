package org.bonitasoft.engine.search.supervisor;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.search.AbstractArchivedHumanTaskInstanceSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

public class SearchArchivedTasksSupervisedBy extends AbstractArchivedHumanTaskInstanceSearchEntity {

    private final ActivityInstanceService activityInstanceService;

    private final Long supervisorId;

    public SearchArchivedTasksSupervisedBy(final Long supervisorId, final ActivityInstanceService activityInstanceService,
            final FlowNodeStateManager flowNodeStateManager, final SearchEntityDescriptor searchDescriptor, final SearchOptions searchOptions) {
        super(searchDescriptor, searchOptions, flowNodeStateManager);
        this.supervisorId = supervisorId;
        this.activityInstanceService = activityInstanceService;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaSearchException {
        return activityInstanceService.getNumberOfArchivedTasksSupervisedBy(supervisorId, searchOptions);
    }

    @Override
    public List<SAHumanTaskInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaSearchException {
        return activityInstanceService.searchArchivedTasksSupervisedBy(supervisorId, searchOptions);
    }

}
