package org.bonitasoft.engine.test.check;

import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Elias Ricken de Medeiros
 */
public final class CheckNbAssignedTaskOf extends WaitUntil {

    private final int nbActivities;

    private final User user;

    private final ProcessAPI processAPI;

    private List<HumanTaskInstance> assignedHumanTaskInstances;

    public CheckNbAssignedTaskOf(final ProcessAPI processAPI, final int repeatEach, final int timeout, final boolean throwExceptions, final int nbActivities,
            final User user) {
        super(repeatEach, timeout, throwExceptions);
        this.nbActivities = nbActivities;
        this.user = user;
        this.processAPI = processAPI;
    }

    @Override
    protected boolean check() {
        assignedHumanTaskInstances = processAPI.getAssignedHumanTaskInstances(user.getId(), 0, Math.max(nbActivities, 20), ActivityInstanceCriterion.NAME_ASC);
        return assignedHumanTaskInstances.size() == nbActivities;
    }

    /**
     * @return the pendingHumanTaskInstances
     */
    public List<HumanTaskInstance> getAssingnedHumanTaskInstances() {
        return assignedHumanTaskInstances;
    }
}
