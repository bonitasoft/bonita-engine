package org.bonitasoft.engine.test.check;

import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * 
 * @deprecated use {@link APITestUtil} .waitFor...
 */
@Deprecated
public final class CheckNbPendingTaskOf extends WaitUntil {

    private final int nbActivities;

    private final User user;

    private final ProcessAPI processAPI;

    private List<HumanTaskInstance> pendingHumanTaskInstances;

    private final ActivityInstanceCriterion orderBy;

    public CheckNbPendingTaskOf(final ProcessAPI processAPI, final int repeatEach, final int timeout, final boolean throwExceptions, final int nbActivities,
            final User user) {
        this(processAPI, repeatEach, timeout, throwExceptions, nbActivities, user, ActivityInstanceCriterion.NAME_ASC);
    }

    public CheckNbPendingTaskOf(final ProcessAPI processAPI, final int repeatEach, final int timeout, final boolean throwExceptions, final int nbActivities,
            final User user, final ActivityInstanceCriterion orderBy) {
        super(repeatEach, timeout, throwExceptions);
        this.nbActivities = nbActivities;
        this.user = user;
        this.processAPI = processAPI;
        this.orderBy = orderBy;
    }

    @Override
    protected boolean check() {
        pendingHumanTaskInstances = processAPI.getPendingHumanTaskInstances(user.getId(), 0, Math.max(nbActivities, 20), orderBy);
        return pendingHumanTaskInstances.size() == nbActivities;
    }

    public List<HumanTaskInstance> getPendingHumanTaskInstances() {
        return pendingHumanTaskInstances;
    }
}
