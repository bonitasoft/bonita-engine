/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.platform;

import static org.bonitasoft.engine.matchers.ListElementMatcher.nameAre;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class RestartHandlersTests extends CommonAPITest {

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    /*
     * This test is not synchronized correctly so there is the test below: we have more elements so some are restarted using restart handlers
     */
    @Test
    @Cover(classes = {}, concept = BPMNConcept.ACTIVITIES, jira = "ENGINE-469", keywords = { "node", "restart", "transition", "flownode" }, story = "elements must be restarted when they were not completed when the node was shut down")
    public void restartElements() throws Exception {
        final User user = createUser("john", "bpm");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithTransition", "1.0");
        builder.addActor("actor");
        builder.addUserTask("step1", "actor");
        builder.addUserTask("step2", "actor");
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), "actor", user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, user.getId());
        Thread.sleep(50);// wait that notify is executed at least
        logout();
        final PlatformSession loginPlatform = APITestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        platformAPI.stopNode();
        platformAPI.startNode();
        APITestUtil.logoutPlatform(loginPlatform);
        login();
        waitForUserTask("step2", processInstance);
        disableAndDeleteProcess(processDefinition.getId());
        deleteUser(user);
    }

    @Test
    @Cover(classes = {}, concept = BPMNConcept.ACTIVITIES, jira = "ENGINE-469", keywords = { "node", "restart", "transition", "flownode" }, story = "elements must be restarted when they were not completed when the node was shut down")
    public void restartALotOfElements() throws Exception {
        final User user = createUser("john", "bpm");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithTransition", "1.0");
        builder.addActor("actor");
        builder.addUserTask("step1", "actor");
        final ArrayList<String> names = new ArrayList<String>(28);
        for (int i = 2; i < 30; i++) {
            final String activityName = "step" + i;
            builder.addAutomaticTask(activityName);
            // if (i > 6) {
            // addUserTask.addOperation(new OperationBuilder().createSetDataOperation("data",
            // new ExpressionBuilder().createGroovyScriptExpression("script", "Thread.sleep(5);return 10;", "java.lang.Integer")));
            // addUserTask.addIntegerData("data", new ExpressionBuilder().createConstantIntegerExpression(0));
            // }
            builder.addTransition("step1", "step" + i);
        }
        for (int i = 2; i < 30; i++) {
            final String activityName = "ustep" + i;
            names.add(activityName);
            builder.addUserTask(activityName, "actor");
            builder.addTransition("step" + i, "ustep" + i);
        }
        Collections.sort(names);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), "actor", user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, user.getId());
        logout();
        final PlatformSession loginPlatform = APITestUtil.loginPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        Thread.sleep(50);
        platformAPI.stopNode();
        Thread.sleep(50);
        platformAPI.startNode();
        APITestUtil.logoutPlatform(loginPlatform);
        login();
        // check all are not already pending
        assertTrue(getProcessAPI().getNumberOfPendingHumanTaskInstances(user.getId()) < names.size() - 5);
        final WaitUntil waitUntil = new WaitUntil(100, 5000, false) {

            @Override
            protected boolean check() {
                final List<HumanTaskInstance> pendingHumanTaskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 40,
                        ActivityInstanceCriterion.NAME_ASC);
                return nameAre(names.toArray(new String[names.size()])).matches(pendingHumanTaskInstances);
            }
        };
        assertTrue(waitUntil.waitUntil());
        disableAndDeleteProcess(processDefinition.getId());
        deleteUser(user);
    }

}
