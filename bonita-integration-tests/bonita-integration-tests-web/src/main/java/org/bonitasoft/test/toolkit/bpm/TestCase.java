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
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.test.toolkit.exception.NextActivityIsNotAllowedStateException;
import org.bonitasoft.test.toolkit.exception.NoActivityLeftException;
import org.bonitasoft.test.toolkit.exception.TestToolkitException;
import org.bonitasoft.test.toolkit.organization.TestToolkitCtx;
import org.bonitasoft.test.toolkit.organization.TestUser;

/**
 * @author Vincent Elcrin
 */
public class TestCase {

    private final ProcessInstance processInstance;

    public final static int GET_NEXT_NB_ATTEMPT = 30;

    public final static int SLEEP_TIME_MS = 100;

    public final static String READY_STATE = "started";

    public TestCase(final ProcessInstance instance) {
        processInstance = instance;
    }

    /**
     * Wait until the process return the state in parameter
     */
    public void waitProcessState(final APISession apiSession, final String state) {
        final ProcessAPI processAPI = TestProcess.getProcessAPI(apiSession);
        ProcessInstance instance = null;
        for (int i = 0; i < GET_NEXT_NB_ATTEMPT; i++) {
            try {
                instance = processAPI.getProcessInstance(processInstance.getId());
                if (instance != null && state.equals(instance.getState())) {
                    break;
                }
                Thread.sleep(SLEEP_TIME_MS);
            } catch (final Exception e) {
                throw new TestToolkitException("Can't get process instance <" + processInstance.getId() + ">.", e);
            }
        }
        if (instance == null || !state.equals(instance.getState())) {
            throw new TestToolkitException(
                    "Instance <" + processInstance.getId() + "> has not reached the expected state <" + state + ">.");
        }
    }

    /**
     * Search and get next human task
     */
    public TestHumanTask getNextHumanTask(final APISession apiSession) {
        final ProcessAPI processAPI = TestProcess.getProcessAPI(apiSession);
        final SearchOptionsBuilder searchOptBuilder = new SearchOptionsBuilder(0, 1);
        searchOptBuilder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE);

        /*
         * Get next workable human task. (e.g. not in initialization state)
         */
        HumanTaskInstance humanTask = null;
        SearchResult<HumanTaskInstance> result = null;
        for (int i = 0; i < GET_NEXT_NB_ATTEMPT; i++) {
            try {
                result = processAPI.searchHumanTaskInstances(searchOptBuilder.done());
                if (!result.getResult().isEmpty()) {
                    humanTask = result.getResult().get(0);
                    break;
                }
                Thread.sleep(SLEEP_TIME_MS);
            } catch (final InvalidSessionException e) {
                throw new TestToolkitException("Can't search human task instances. Invalid session", e);
            } catch (final SearchException e) {
                throw new TestToolkitException("Can't search human task instances", e);
            } catch (final InterruptedException e) {
                throw new TestToolkitException("Interrupted during searching process", e);
            }
        }

        if (humanTask != null) {
            return new TestHumanTask(humanTask);
        } else {
            if (result.getResult().size() > 0) {
                throw new NextActivityIsNotAllowedStateException(result.getResult().get(0));
            } else {
                throw new NoActivityLeftException();
            }
        }
    }

    public TestHumanTask getNextHumanTask() {
        return getNextHumanTask(TestToolkitCtx.getInstance().getInitiator().getSession());
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    private ArchivedProcessInstance getArchive(final APISession apiSession) {
        final ProcessAPI processAPI = TestProcess.getProcessAPI(apiSession);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 1);
        searchOptionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, getId());
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.ARCHIVE_DATE, Order.DESC);
        SearchResult<ArchivedProcessInstance> searchArchivedProcessInstances;
        try {
            searchArchivedProcessInstances = processAPI
                    .searchArchivedProcessInstancesInAllStates(searchOptionsBuilder.done());
        } catch (final SearchException se) {
            throw new TestToolkitException("Can't get process instance archived for <" + getId() + ">", se);
        }
        if (searchArchivedProcessInstances != null && searchArchivedProcessInstances.getCount() > 0) {
            return searchArchivedProcessInstances.getResult().get(0);
        } else {
            throw new TestToolkitException("Can't get process instance archived for <" + getId() + ">");
        }
    }

    public ArchivedProcessInstance getArchive(final TestUser initiator) {
        return getArchive(initiator.getSession());
    }

    public ArchivedProcessInstance getArchive() {
        return getArchive(TestToolkitCtx.getInstance().getInitiator());
    }

    public long getId() {
        return processInstance.getId();
    }

    // ///////////////////////////////////////////////////////////////////
    // / Execution
    // ///////////////////////////////////////////////////////////////////

    public void execute(final APISession apiSession) {
        try {
            final TestHumanTask nextActivityInstance = getNextHumanTask(apiSession);
            if (nextActivityInstance != null) {
                nextActivityInstance.execute(apiSession);
            }
        } catch (final NoActivityLeftException e) {
            // there were no activity in the process
        }

    }

    public void execute(final TestUser user) {
        execute(user.getSession());
    }

    public void execute() {
        execute(TestToolkitCtx.getInstance().getInitiator());
    }

    // ///////////////////////////////////////////////////////////////////
    // / Comments
    // ///////////////////////////////////////////////////////////////////

    private void addComment(final APISession apiSession, final String content) {
        final ProcessAPI processAPI = TestProcess.getProcessAPI(apiSession);
        try {
            processAPI.addProcessComment(processInstance.getId(), content);
        } catch (final Exception e) {
            throw new TestToolkitException("Can't add comment to <" + processInstance.getId() + ">", e);
        }
    }

    private void addComment(final TestUser initiator, final String content) {
        addComment(initiator.getSession(), content);
    }

    public void addComments(final TestUser initiator, final int nbOfComments, final String content) {
        for (int i = 0; i < nbOfComments; i++) {
            addComment(initiator, content + i);
        }
    }

    public void addComment(final String content) {
        addComment(TestToolkitCtx.getInstance().getInitiator(), content);
    }
}
