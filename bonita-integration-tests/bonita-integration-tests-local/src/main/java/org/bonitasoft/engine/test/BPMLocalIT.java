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
package org.bonitasoft.engine.test;

import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.LoginAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.platform.Platform;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(BonitaTestRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class BPMLocalIT extends CommonAPILocalIT {

    public static Semaphore semaphore1 = new Semaphore(1);

    public static Semaphore semaphore2 = new Semaphore(1);

    private User john;

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();

        john = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        setSessionInfo(getSession());
    }

    @After
    public void after() throws Exception {
        VariableStorage.clearAll();
        deleteUser(USERNAME);
        logoutOnTenant();
        cleanSession();
    }

    @Test(expected = InvalidSessionException.class)
    public void useAFakeSessionId() throws BonitaException {
        final LoginAPI loginAPI = getLoginAPI();
        final APISession session = loginAPI.login(DEFAULT_TECHNICAL_LOGGER_USERNAME, DEFAULT_TECHNICAL_LOGGER_PASSWORD);
        final FakeSession fakeSession = new FakeSession(session);
        fakeSession.setId(fakeSession.getId() + 1);

        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(fakeSession);
        identityAPI.getGroup(12);
    }

    @Test
    public void checkProcessCommentAreArchived() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();
        final ProcessDefinitionBuilder processDef = new ProcessDefinitionBuilder().createNewInstance("processToTestComment", "1.0");
        processDef.addStartEvent("start");
        processDef.addUserTask("step1", ACTOR_NAME);
        processDef.addEndEvent("end");
        processDef.addTransition("start", "step1");
        processDef.addTransition("step1", "end");
        processDef.addActor(ACTOR_NAME);
        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDef.done(), ACTOR_NAME, john);
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final Callable<Long> getNumberOfComments = new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                return commentService.getNumberOfComments(QueryOptions.countQueryOptions());
            }
        };
        final Callable<Long> getNumberOfArchivedComments = new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                return commentService.getNumberOfArchivedComments(QueryOptions.countQueryOptions());
            }
        };
        assertEquals(0, (long) transactionService.executeInTransaction(getNumberOfComments));
        final long numberOfInitialArchivedComments = transactionService.executeInTransaction(getNumberOfArchivedComments);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final Long step1Id = waitForUserTask(processInstance, "step1");
        getProcessAPI().addProcessComment(processInstance.getId(), "kikoo lol");
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        assertEquals(1, (long) transactionService.executeInTransaction(getNumberOfComments));
        assertEquals(numberOfInitialArchivedComments, (long) transactionService.executeInTransaction(getNumberOfArchivedComments));
        assignAndExecuteStep(step1Id, john);

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        assertEquals(2, (long) transactionService.executeInTransaction(getNumberOfComments));// claim add a comment...
        assertEquals(numberOfInitialArchivedComments, (long) transactionService.executeInTransaction(getNumberOfArchivedComments));
        waitForProcessToFinish(processInstance);

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        assertEquals(0, (long) transactionService.executeInTransaction(getNumberOfComments));
        assertEquals(numberOfInitialArchivedComments + 2, (long) transactionService.executeInTransaction(getNumberOfArchivedComments));
        disableAndDeleteProcess(definition);
    }

    @Test
    public void checkPendingMappingAreDeleted() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();
        final ProcessDefinitionBuilder processDef = new ProcessDefinitionBuilder().createNewInstance("processToTestComment", "1.0");
        processDef.addShortTextData("kikoo", new ExpressionBuilder().createConstantStringExpression("lol"));
        processDef.addStartEvent("start");
        processDef.addUserTask("step1", ACTOR_NAME).addShortTextData("kikoo2", new ExpressionBuilder().createConstantStringExpression("lol"));
        processDef.addUserTask("step2", ACTOR_NAME);
        processDef.addEndEvent("end");
        processDef.addTransition("start", "step1");
        processDef.addTransition("step1", "step2");
        processDef.addTransition("step2", "end");
        processDef.addActor(ACTOR_NAME);
        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDef.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final Long step1Id = waitForUserTask(processInstance, "step1");
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final Callable<List<SPendingActivityMapping>> getPendingMappings = new Callable<List<SPendingActivityMapping>>() {

            @Override
            public List<SPendingActivityMapping> call() throws Exception {
                final QueryOptions queryOptions = new QueryOptions(0, 100, SPendingActivityMapping.class, "id", OrderByType.ASC);
                return activityInstanceService.getPendingMappings(step1Id, queryOptions);
            }
        };
        List<SPendingActivityMapping> mappings = transactionService.executeInTransaction(getPendingMappings);
        assertEquals(1, mappings.size());
        assignAndExecuteStep(step1Id, john.getId());
        waitForUserTask(processInstance, "step2");
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        mappings = transactionService.executeInTransaction(getPendingMappings);
        assertEquals(0, mappings.size());
        disableAndDeleteProcess(definition);
    }

    @Test
    public void checkDependenciesAreDeletedWhenProcessIsDeleted() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();
        final ProcessDefinitionBuilder processDef = new ProcessDefinitionBuilder().createNewInstance("processToTestTransitions", "1.0");
        processDef.addStartEvent("start");
        processDef.addUserTask("step1", ACTOR_NAME);
        processDef.addEndEvent("end");
        processDef.addTransition("start", "step1");
        processDef.addTransition("step1", "end");
        processDef.addActor(ACTOR_NAME);
        final byte[] content = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDef.done())
                .addClasspathResource(new BarResource("myDep", content)).done();
        final ProcessDefinition definition = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final Long step1Id = waitForUserTask(processInstance, "step1");
        List<Long> dependencyIds = transactionService.executeInTransaction(new GetDependenciesIds(getSession(), definition.getId(), dependencyService, 0,
                100));
        assertEquals(1, dependencyIds.size());
        final SDependency dependency = transactionService.executeInTransaction(new GetSDependency(dependencyIds.get(0), dependencyService));
        assertTrue(dependency.getName().endsWith("myDep"));
        assertTrue(Arrays.equals(content, dependency.getValue()));

        assignAndExecuteStep(step1Id, john);
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(definition);

        dependencyIds = transactionService.executeInTransaction(new GetDependenciesIds(getSession(), definition.getId(), dependencyService, 0, 100));
        assertEquals(0, dependencyIds.size());
    }

    @Test
    public void checkMoreThan20DependenciesAreDeletedWhenProcessIsDeleted() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();
        final ProcessDefinitionBuilder processDef = new ProcessDefinitionBuilder().createNewInstance("processToTestTransitions", "1.0");
        processDef.addStartEvent("start").addUserTask("step1", ACTOR_NAME).addEndEvent("end");
        processDef.addTransition("start", "step1").addTransition("step1", "end");
        processDef.addActor(ACTOR_NAME);
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDef.done());
        for (int i = 0; i < 25; i++) {
            final byte[] content = new byte[] { 1, 2, 3, 4, 5, 6, 7, (byte) (i >>> 24), (byte) (i >> 16 & 0xff), (byte) (i >> 8 & 0xff), (byte) (i & 0xff) };
            businessArchiveBuilder.addClasspathResource(new BarResource("myDep" + i, content));
        }
        final ProcessDefinition definition = deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final Long step1Id = waitForUserTask(processInstance, "step1");
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        List<Long> dependencyIds = transactionService.executeInTransaction(new GetDependenciesIds(getSession(), definition.getId(), dependencyService,
                0, 100));
        assertEquals(25, dependencyIds.size());
        final SDependency dependency = transactionService.executeInTransaction(new GetSDependency(dependencyIds.get(0), dependencyService));
        assertNotNull(dependency);

        assignAndExecuteStep(step1Id, john.getId());
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(definition);
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        dependencyIds = transactionService.executeInTransaction(new GetDependenciesIds(getSession(), definition.getId(), dependencyService, 0, 100));
        assertEquals(0, dependencyIds.size());
    }

    @Test
    public void deletingProcessDeletesActors() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();
        final String userTaskName = "actNaturally";
        final ProcessDefinition definition = deployAndEnableProcessWithOneHumanTask("deletingProcessDeletesActors", "CandidateForOscarReward", userTaskName);

        final ProcessInstance processInstanceId = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(processInstanceId, userTaskName);

        disableAndDeleteProcess(definition);

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final List<SActor> actors = transactionService.executeInTransaction(new Callable<List<SActor>>() {

            @Override
            public List<SActor> call() throws Exception {
                final QueryOptions queryOptions = new QueryOptions(0, 1, SActor.class, "id", OrderByType.ASC);
                return getTenantAccessor().getActorMappingService().getActors(definition.getId(), queryOptions);
            }
        });

        // Check there is no actor left:
        assertEquals(0, actors.size());
    }

    @Test
    public void deletingProcessDeletesActorMappings() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final UserTransactionService transactionService = tenantAccessor.getUserTransactionService();
        final String userTaskName = "actNaturally";
        final ProcessDefinition definition = deployAndEnableProcessWithOneHumanTask("deletingProcessDeletesActorMappings", "CandidateForOscarReward",
                userTaskName);

        final ProcessInstance processInstanceId = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(processInstanceId, userTaskName);

        disableAndDeleteProcess(definition);

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final List<SActorMember> actorMembers = transactionService.executeInTransaction(new Callable<List<SActorMember>>() {

            @Override
            public List<SActorMember> call() throws Exception {
                return getTenantAccessor().getActorMappingService().getActorMembersOfUser(john.getId(), 0, 1);
            }
        });

        // Check there is no actor left:
        assertEquals(0, actorMembers.size());
    }

    private ProcessDefinition deployAndEnableProcessWithOneHumanTask(final String processName, final String actorName, final String userTaskName)
            throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, "1.0");
        processBuilder.addActor(actorName).addDescription(actorName + " description");
        processBuilder.addStartEvent("startEvent");
        processBuilder.addUserTask(userTaskName, actorName);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition("startEvent", userTaskName);
        processBuilder.addTransition(userTaskName, "endEvent");
        return deployAndEnableProcessWithActor(processBuilder.done(), actorName, john);
    }

    @Test
    @Cover(classes = {}, concept = BPMNConcept.ACTIVITIES, jira = "ENGINE-469", keywords = { "node", "restart", "transition", "flownode" }, story = "elements must be restarted when they were not completed when the node was shut down")
    public void restartHandlerTests() throws Exception {
        /*
         * process with blocking connector
         */
        final ProcessDefinitionBuilder builder1 = new ProcessDefinitionBuilder().createNewInstance("p1", "1.0");
        builder1.addActor(ACTOR_NAME);
        builder1.addUserTask("step1", ACTOR_NAME);
        builder1.addAutomaticTask("step2").addConnector("myConnector", "blocking-connector", "1.0", ConnectorEvent.ON_ENTER);
        builder1.addTransition("step1", "step2");
        builder1.addUserTask("ustep2", ACTOR_NAME);
        builder1.addTransition("step2", "ustep2");
        final BusinessArchive businessArchive = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(builder1.done())
                .addConnectorImplementation(
                        new BarResource("blocking-connector.impl", BuildTestUtil.buildConnectorImplementationFile("blocking-connector", "1.0",
                                "blocking-connector-impl",
                                "1.0", BlockingConnector.class.getName()))).done();
        final ProcessDefinition p1 = deployAndEnableProcessWithActor(businessArchive, ACTOR_NAME, john);

        /*
         * process with blocking operation (executing work)
         */
        final ProcessDefinitionBuilder builder2 = new ProcessDefinitionBuilder().createNewInstance("p2", "1.0");
        final String blockingGroovyScript1 = "org.bonitasoft.engine.test.BPMLocalIT.tryAcquireSemaphore1();\nreturn \"done\";";
        builder2.addActor(ACTOR_NAME);
        builder2.addShortTextData("data", null);
        builder2.addUserTask("step1", ACTOR_NAME);
        builder2.addAutomaticTask("step2").addOperation(
                new OperationBuilder().createSetDataOperation("data",
                        new ExpressionBuilder().createGroovyScriptExpression("blockingGroovyScript1", blockingGroovyScript1, String.class.getName())));
        builder2.addTransition("step1", "step2");
        builder2.addUserTask("ustep2", ACTOR_NAME);
        builder2.addTransition("step2", "ustep2");

        final ProcessDefinition p2 = deployAndEnableProcessWithActor(builder2.done(), ACTOR_NAME, john);

        /*
         * process with blocking transition (notify work)
         */
        final ProcessDefinitionBuilder builder3 = new ProcessDefinitionBuilder().createNewInstance("p3", "1.0");
        final String blockingGroovyScript2 = "org.bonitasoft.engine.test.BPMLocalIT.tryAcquireSemaphore2();\nreturn true;";
        builder3.addActor(ACTOR_NAME);
        builder3.addUserTask("step1", ACTOR_NAME);
        builder3.addAutomaticTask("step2");
        builder3.addTransition("step1", "step2",
                new ExpressionBuilder().createGroovyScriptExpression("blockingGroovyScript2", blockingGroovyScript2, Boolean.class.getName()));
        builder3.addUserTask("ustep2", ACTOR_NAME);
        builder3.addTransition("step2", "ustep2");

        final ProcessDefinition p3 = deployAndEnableProcessWithActor(builder3.done(), ACTOR_NAME, john);

        // Block all 3 tasks
        BlockingConnector.semaphore.acquire();
        semaphore1.acquire();
        semaphore2.acquire();

        System.out.println("Start the process");
        final ProcessInstance pi1 = getProcessAPI().startProcess(p1.getId());
        final ProcessInstance pi2 = getProcessAPI().startProcess(p2.getId());
        final ProcessInstance pi3 = getProcessAPI().startProcess(p3.getId());
        waitForUserTaskAndExecuteIt(pi1, "step1", john);
        waitForUserTaskAndExecuteIt(pi2, "step1", john);
        waitForUserTaskAndExecuteIt(pi3, "step1", john);
        System.out.println("executed step1");
        logoutOnTenant();
        final PlatformSession loginPlatform = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        // stop node and in the same time release the semaphores to unlock works
        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("release semaphores");
                BlockingConnector.semaphore.release();
                semaphore1.release();
                semaphore2.release();
                System.out.println("released semaphores");
            }
        });
        System.out.println("stop node");
        thread.start();
        platformAPI.stopNode();
        System.out.println("node stopped");
        // release them (work will fail, node is stopped)
        thread.join(1000);
        Thread.sleep(50);
        System.out.println("start node");
        platformAPI.startNode();
        System.out.println("node started");
        logoutOnPlatform(loginPlatform);
        loginOnDefaultTenantWithDefaultTechnicalUser();

        //during stop node some flow node can be put in failed state
        retryFailedFlowNodes();

        // check we have all task ready
        waitForPendingTasks(john.getId(), 3);

        disableAndDeleteProcess(p1.getId());
        disableAndDeleteProcess(p2.getId());
        disableAndDeleteProcess(p3.getId());
    }

    private void retryFailedFlowNodes() throws SearchException, ActivityInstanceNotFoundException, ActivityExecutionException {
        List<FlowNodeInstance> failedFlowNodes = getFailedFlowNodes();
        for (FlowNodeInstance failedFlowNode : failedFlowNodes) {
            getProcessAPI().retryTask(failedFlowNode.getId());
        }
    }

    private List<FlowNodeInstance> getFailedFlowNodes() throws SearchException {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 3);
        builder.filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, ActivityStates.FAILED_STATE);
        SearchResult<FlowNodeInstance> searchResult = getProcessAPI().searchFlowNodeInstances(builder.done());
        return searchResult.getResult();
    }

    public static void tryAcquireSemaphore1() throws InterruptedException {
        System.out.println("tryAcquire semaphore1");
        semaphore1.tryAcquire(15, TimeUnit.SECONDS);
        semaphore1.release();
        System.out.println("release semaphore1");
    }

    public static void tryAcquireSemaphore2() throws InterruptedException {
        System.out.println("tryAcquire semaphore2");
        semaphore2.tryAcquire(15, TimeUnit.SECONDS);
        semaphore2.release();
        System.out.println("release semaphore2");
    }

    @Cover(classes = PlatformAPI.class, concept = BPMNConcept.NONE, keywords = { "Platform" }, story = "The platform version must be the same than the project version.", jira = "")
    @Test
    public void getPlatformVersion() throws BonitaException, IOException {
        logoutOnTenant();
        final PlatformSession platformSession = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        final Platform platform = platformAPI.getPlatform();
        logoutOnPlatform(platformSession);
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final String platformVersionToTest = getBonitaVersion();

        assertNotNull("can't find the platform", platform);
        assertEquals("platformAdmin", platform.getCreatedBy());
        assertEquals(platformVersionToTest, platform.getVersion());
        assertEquals(platformVersionToTest, platform.getInitialVersion());
    }

    public static String getBonitaVersion() throws IOException {
        String version = System.getProperty("bonita.version");// works in maven
        if (version == null) {
            // when running tests in eclipse get it from the pom.xml
            final File file = new File("pom.xml");
            final String pomContent = IOUtil.read(file);
            final Pattern pattern = Pattern.compile("<version>(.*)</version>");
            final Matcher matcher = pattern.matcher(pomContent);
            matcher.find();
            version = matcher.group(1);
        }
        return version;
    }

    private static class GetDependenciesIds implements Callable<List<Long>> {

        private final APISession session;

        private final long processDefinitionId;

        private final DependencyService dependencyService;

        private final int startIndex;

        private final int maxResult;

        public GetDependenciesIds(final APISession session, final long processDefinitionId, final DependencyService dependencyService,
                final int startIndex, final int maxResult) {
            this.session = session;
            this.processDefinitionId = processDefinitionId;
            this.dependencyService = dependencyService;
            this.startIndex = startIndex;
            this.maxResult = maxResult;
        }

        @Override
        public List<Long> call() throws Exception {
            setSessionInfo(session); // the session was cleaned by api call. This must be improved
            return dependencyService.getDependencyIds(processDefinitionId, ScopeType.PROCESS, startIndex, maxResult);
        }
    }

    private static class GetSDependency implements Callable<SDependency> {

        private final long dependencyId;

        private final DependencyService dependencyService;

        public GetSDependency(final long dependencyId, final DependencyService dependencyService) {
            this.dependencyId = dependencyId;
            this.dependencyService = dependencyService;
        }

        @Override
        public SDependency call() throws Exception {
            return dependencyService.getDependency(dependencyId);
        }
    }

}
