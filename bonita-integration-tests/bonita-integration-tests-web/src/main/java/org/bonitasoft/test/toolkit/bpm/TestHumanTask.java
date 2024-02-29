/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.test.toolkit.bpm;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.test.toolkit.exception.TestToolkitException;
import org.bonitasoft.test.toolkit.organization.TestToolkitCtx;
import org.bonitasoft.test.toolkit.organization.TestUser;

/**
 * @author Vincent Elcrin
 */
public class TestHumanTask extends AbstractManualTask {

    private HumanTaskInstance humanTaskInstance;

    private final static int GET_NEXT_NB_ATTEMPT = 30;

    private final static int SLEEP_TIME_MS = 100;

    /**
     * Default Constructor.
     */
    public TestHumanTask(final ActivityInstance activityInstance) {
        assert activityInstance instanceof HumanTaskInstance;
        humanTaskInstance = (HumanTaskInstance) activityInstance;
    }

    public HumanTaskInstance getHumanTaskInstance() {
        return humanTaskInstance;
    }

    /**
     * @return the processInstance
     */
    private HumanTaskInstance fetchHumanTaskInstance(final APISession apiSession) {
        try {
            return TestProcess.getProcessAPI(apiSession).getHumanTaskInstance(getId());
        } catch (final Exception e) {
            throw new TestToolkitException("Can't get humanTask instance for <" + getId() + ">", e);
        }
    }

    public void refreshHumanTaskInstanceInstance() {
        humanTaskInstance = fetchHumanTaskInstance(TestToolkitCtx.getInstance().getInitiator().getSession());
    }

    public DataInstance getDataInstance(final String dataName) {
        try {
            return TestProcess.getProcessAPI(TestToolkitCtx.getInstance().getInitiator().getSession())
                    .getActivityDataInstance(dataName, humanTaskInstance.getId());
        } catch (final DataNotFoundException e) {
            throw new TestToolkitException("Unable to find dataInstance " + dataName, e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.test.AbstractManualTask#getId()
     */
    @Override
    public long getId() {
        return humanTaskInstance.getId();
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.test.toolkit.bpm.AbstractManualTask#getDescription()
     */
    @Override
    public String getDescription() {
        return humanTaskInstance.getDescription();
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.test.toolkit.bpm.AbstractManualTask#getName()
     */
    @Override
    public String getName() {
        return humanTaskInstance.getName();
    }

    // /////////////////////////////////////////////////////////////////////////////
    // / Assign
    // /////////////////////////////////////////////////////////////////////////////

    private TestHumanTask assignTo(final APISession apiSession, final TestUser user) {
        final ProcessAPI processAPI = TestProcess.getProcessAPI(apiSession);
        try {
            processAPI.assignUserTask(humanTaskInstance.getId(), user.getId());
        } catch (final Exception e) {
            throw new TestToolkitException("Can't assign user", e);
        }
        return this;
    }

    public TestHumanTask assignTo(final TestUser initiator, final TestUser user) {
        return assignTo(initiator.getSession(), user);
    }

    public TestHumanTask assignTo(final TestUser user) {
        return assignTo(TestToolkitCtx.getInstance().getInitiator(), user);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // / Execute
    // ////////////////////////////////////////////////////////////////////////////

    public void execute(final APISession apiSession) {
        final ProcessAPI processAPI = TestProcess.getProcessAPI(apiSession);
        try {
            processAPI.executeFlowNode(humanTaskInstance.getId());
        } catch (final Exception e) {
            throw new TestToolkitException("Can't execute activity <" + humanTaskInstance.getId() + ">.", e);
        }
    }

    public void archive(final APISession apiSession) {
        try {
            execute(apiSession);
        } catch (final TestToolkitException e) {
            if (!(e.getCause() instanceof ActivityExecutionException)) {
                throw e;
            }
        }
    }

    public void archive(final TestUser initiator) {
        archive(initiator.getSession());
    }

    public void archive() {
        archive(TestToolkitCtx.getInstance().getInitiator());
    }

    // /////////////////////////////////////////////////////////////////////////////////
    // Convenient method
    // /////////////////////////////////////////////////////////////////////////////////

    public void waitState(final String state) {
        for (int i = 0; i < GET_NEXT_NB_ATTEMPT; i++) {
            try {
                Thread.sleep(SLEEP_TIME_MS);
            } catch (final InterruptedException e) {
                throw new TestToolkitException(
                        "Problem while waiting for state <" + state + "> for human task <" + getId() + ">. Interrupted",
                        e);
            }
            refreshHumanTaskInstanceInstance();
            if (getHumanTaskInstance() != null && state.equals(getHumanTaskInstance().getState())) {
                break;
            }
        }
        if (getHumanTaskInstance() == null || !state.equals(getHumanTaskInstance().getState())) {
            throw new TestToolkitException(
                    "Expected state <" + state + "> has not been reached for human task<" + getId() + ">.");
        }
    }

}
