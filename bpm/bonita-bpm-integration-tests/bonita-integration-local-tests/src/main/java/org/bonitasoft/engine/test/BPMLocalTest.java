package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.archive.SATransitionInstance;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BPMLocalTest extends CommonAPILocalTest {

    protected static final String JOHN_USERNAME = "john";

    protected static final String JOHN_PASSWORD = "bpm";

    private User john;

    @After
    public void afterTest() throws Exception {
        VariableStorage.clearAll();
        deleteUser(JOHN_USERNAME);
        logout();
        cleanSession();

    }

    @Before
    public void beforeTest() throws Exception {
        login();
        john = createUser(JOHN_USERNAME, JOHN_PASSWORD);
        logout();
        loginWith(JOHN_USERNAME, JOHN_PASSWORD);
        setSessionInfo(getSession());
    }

    @Test(expected = InvalidSessionException.class)
    public void useAFakeSessionId() throws BonitaException {
        final APISession session = APITestUtil.loginDefaultTenant();
        final FakeSession fakeSession = new FakeSession(session);
        fakeSession.setId(fakeSession.getId() + 1);

        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(fakeSession);
        identityAPI.getGroup(12);
    }

    @Cover(classes = { TransitionService.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Transition", "Activity" }, jira = "ENGINE-528")
    @Test
    public void checkTransitionWhenNextFlowNodeIsActivity() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransitionService transitionInstanceService = tenantAccessor.getTransitionInstanceService();
        final TransactionService transactionService = tenantAccessor.getTransactionService();
        final ProcessDefinitionBuilder processDef = new ProcessDefinitionBuilder().createNewInstance("processToTestTransitions", "1.0");
        processDef.addStartEvent("start");
        processDef.addUserTask("step1", "delivery");
        processDef.addAutomaticTask("step2");
        processDef.addEndEvent("end");
        processDef.addTransition("start", "step1");
        processDef.addTransition("step1", "step2");
        processDef.addTransition("step2", "end");
        processDef.addActor("delivery");

        // Execute process
        final ProcessDefinition definition = deployAndEnableWithActor(processDef.done(), "delivery", john);
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());

        // Execute step1
        final ActivityInstance waitForUserTask = waitForUserTask("step1", processInstance);
        assignAndExecuteStep(waitForUserTask, john.getId());
        waitForProcessToFinish(processInstance);

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        // Check
        final TransactionContentWithResult<List<SATransitionInstance>> searchArchivedTransitions = new TransactionContentWithResult<List<SATransitionInstance>>() {

            private List<SATransitionInstance> searchArchivedTransitions;

            @Override
            public void execute() throws SBonitaException {
                final OrderByOption orderByOption = new OrderByOption(SATransitionInstance.class, "id", OrderByType.ASC);
                final QueryOptions searchOptions = new QueryOptions(0, 10, Collections.singletonList(orderByOption));
                searchArchivedTransitions = transitionInstanceService.searchArchivedTransitionInstances(searchOptions);
            }

            @Override
            public List<SATransitionInstance> getResult() {
                return searchArchivedTransitions;
            }
        };
        executeInTransaction(transactionService, searchArchivedTransitions);
        final List<SATransitionInstance> result = searchArchivedTransitions.getResult();
        assertEquals(3, result.size());
        assertTrue(result.get(2).getId() > result.get(0).getId());
        assertEquals(result.get(2).getId(), result.get(0).getId() + 2);

        disableAndDeleteProcess(definition);
    }

    @Test
    public void checkProcessCommentAreArchived() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final SCommentService commentService = tenantAccessor.getCommentService();
        final TransactionService transactionService = tenantAccessor.getTransactionService();
        final ProcessDefinitionBuilder processDef = new ProcessDefinitionBuilder().createNewInstance("processToTestComment", "1.0");
        processDef.addStartEvent("start");
        processDef.addUserTask("step1", "delivery");
        processDef.addEndEvent("end");
        processDef.addTransition("start", "step1");
        processDef.addTransition("step1", "end");
        processDef.addActor("delivery");
        final ProcessDefinition definition = deployAndEnableWithActor(processDef.done(), "delivery", john);
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final TransactionContentWithResult<Long> getNumberOfComment = new TransactionContentWithResult<Long>() {

            private long numberOfTransitionInstances;

            @Override
            public void execute() throws SBonitaException {
                numberOfTransitionInstances = commentService.getNumberOfComments(QueryOptions.defaultQueryOptions());
            }

            @Override
            public Long getResult() {
                return numberOfTransitionInstances;
            }
        };
        final TransactionContentWithResult<Long> getNumberOfArchivedComment = new TransactionContentWithResult<Long>() {

            private long numberOfTransitionInstances;

            @Override
            public void execute() throws SBonitaException {
                numberOfTransitionInstances = commentService.getNumberOfArchivedComments(QueryOptions.defaultQueryOptions());
            }

            @Override
            public Long getResult() {
                return numberOfTransitionInstances;
            }
        };
        executeInTransaction(transactionService, getNumberOfComment);
        executeInTransaction(transactionService, getNumberOfArchivedComment);
        assertEquals(0, (long) getNumberOfComment.getResult());
        final long numberOfInitialArchivedComments = getNumberOfArchivedComment.getResult();
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final ActivityInstance waitForUserTask = waitForUserTask("step1", processInstance);
        getProcessAPI().addComment(processInstance.getId(), "kikoo lol");
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        executeInTransaction(transactionService, getNumberOfComment);
        executeInTransaction(transactionService, getNumberOfArchivedComment);
        assertEquals(1, (long) getNumberOfComment.getResult());
        assertEquals(numberOfInitialArchivedComments, (long) getNumberOfArchivedComment.getResult());
        getProcessAPI().assignUserTask(waitForUserTask.getId(), john.getId());
        getProcessAPI().executeFlowNode(waitForUserTask.getId());
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        executeInTransaction(transactionService, getNumberOfComment);
        executeInTransaction(transactionService, getNumberOfArchivedComment);
        assertEquals(2, (long) getNumberOfComment.getResult());// claim add a comment...
        assertEquals(numberOfInitialArchivedComments, (long) getNumberOfArchivedComment.getResult());
        waitForProcessToFinish(processInstance);
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        executeInTransaction(transactionService, getNumberOfComment);
        executeInTransaction(transactionService, getNumberOfArchivedComment);
        assertEquals(0, (long) getNumberOfComment.getResult());
        assertEquals(numberOfInitialArchivedComments + 2, (long) getNumberOfArchivedComment.getResult());
        disableAndDeleteProcess(definition);
    }

    private static void executeInTransaction(TransactionService transactionService, TransactionContent tc) throws SBonitaException {
        transactionService.begin();
        tc.execute();
        transactionService.complete();
    }

    @Test
    public void checkPendingMappingAreDeleted() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final TransactionService transactionService = tenantAccessor.getTransactionService();
        final ProcessDefinitionBuilder processDef = new ProcessDefinitionBuilder().createNewInstance("processToTestComment", "1.0");
        processDef.addShortTextData("kikoo", new ExpressionBuilder().createConstantStringExpression("lol"));
        processDef.addStartEvent("start");
        processDef.addUserTask("step1", "delivery").addShortTextData("kikoo2", new ExpressionBuilder().createConstantStringExpression("lol"));
        processDef.addUserTask("step2", "delivery");
        processDef.addEndEvent("end");
        processDef.addTransition("start", "step1");
        processDef.addTransition("step1", "step2");
        processDef.addTransition("step2", "end");
        processDef.addActor("delivery");
        final ProcessDefinition definition = deployAndEnableWithActor(processDef.done(), "delivery", john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final ActivityInstance waitForUserTask = waitForUserTask("step1", processInstance);
        final long taskId = waitForUserTask.getId();
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        final TransactionContentWithResult<List<SPendingActivityMapping>> getPendingMappings = new TransactionContentWithResult<List<SPendingActivityMapping>>() {

            private List<SPendingActivityMapping> mappings;

            @Override
            public void execute() throws SBonitaException {
                mappings = activityInstanceService.getPendingMappings(taskId, QueryOptions.defaultQueryOptions());
            }

            @Override
            public List<SPendingActivityMapping> getResult() {
                return mappings;
            }

        };
        executeInTransaction(transactionService, getPendingMappings);
        List<SPendingActivityMapping> mappings = getPendingMappings.getResult();
        assertEquals(1, mappings.size());
        assignAndExecuteStep(waitForUserTask, john.getId());
        waitForUserTask("step2", processInstance);
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        executeInTransaction(transactionService, getPendingMappings);
        mappings = getPendingMappings.getResult();
        assertEquals(0, mappings.size());
        disableAndDeleteProcess(definition);
    }

    @Test
    public void checkDependenciesAreDeletedWhenProcessIsDeleted() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final TransactionService transactionService = tenantAccessor.getTransactionService();
        final ProcessDefinitionBuilder processDef = new ProcessDefinitionBuilder().createNewInstance("processToTestTransitions", "1.0");
        processDef.addStartEvent("start");
        processDef.addUserTask("step1", "delivery");
        processDef.addEndEvent("end");
        processDef.addTransition("start", "step1");
        processDef.addTransition("step1", "end");
        processDef.addActor("delivery");
        final byte[] content = new byte[] { 1, 2, 3, 4, 5, 6, 7 };
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDef.done())
                .addClasspathResource(new BarResource("myDep", content)).done();
        final ProcessDefinition definition = deployAndEnableWithActor(businessArchive, "delivery", john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final ActivityInstance waitForUserTask = waitForUserTask("step1", processInstance);
        transactionService.begin();
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        List<Long> dependencyIds = dependencyService.getDependencyIds(definition.getId(), "process", QueryOptions.defaultQueryOptions());
        transactionService.complete();
        assertEquals(1, dependencyIds.size());
        transactionService.begin();
        final SDependency dependency = dependencyService.getDependency(dependencyIds.get(0));
        transactionService.complete();
        assertTrue(dependency.getName().endsWith("myDep"));
        assertTrue(Arrays.equals(content, dependency.getValue()));

        assignAndExecuteStep(waitForUserTask, john.getId());
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(definition);
        transactionService.begin();
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        dependencyIds = dependencyService.getDependencyIds(definition.getId(), "process", QueryOptions.defaultQueryOptions());
        transactionService.complete();
        assertEquals(0, dependencyIds.size());
    }

    @Test
    public void checkMoreThan20DependenciesAreDeletedWhenProcessIsDeleted() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final TransactionService transactionService = tenantAccessor.getTransactionService();
        final ProcessDefinitionBuilder processDef = new ProcessDefinitionBuilder().createNewInstance("processToTestTransitions", "1.0");
        processDef.addStartEvent("start");
        processDef.addUserTask("step1", "delivery");
        processDef.addEndEvent("end");
        processDef.addTransition("start", "step1");
        processDef.addTransition("step1", "end");
        processDef.addActor("delivery");
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDef.done());
        for (int i = 0; i < 25; i++) {
            final byte[] content = new byte[] { 1, 2, 3, 4, 5, 6, 7, (byte) (i >>> 24), (byte) (i >> 16 & 0xff), (byte) (i >> 8 & 0xff), (byte) (i & 0xff) };
            businessArchiveBuilder.addClasspathResource(new BarResource("myDep" + i, content));
        }
        final ProcessDefinition definition = deployAndEnableWithActor(businessArchiveBuilder.done(), "delivery", john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final ActivityInstance waitForUserTask = waitForUserTask("step1", processInstance);
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        transactionService.begin();
        List<Long> dependencyIds = dependencyService.getDependencyIds(definition.getId(), "process", QueryOptions.allResultsQueryOptions());
        transactionService.complete();
        assertEquals(25, dependencyIds.size());
        transactionService.begin();
        final SDependency dependency = dependencyService.getDependency(dependencyIds.get(0));
        assertNotNull(dependency);
        transactionService.complete();

        assignAndExecuteStep(waitForUserTask, john.getId());
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(definition);
        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        transactionService.begin();
        dependencyIds = dependencyService.getDependencyIds(definition.getId(), "process", QueryOptions.defaultQueryOptions());
        transactionService.complete();
        assertEquals(0, dependencyIds.size());
    }

    @Test
    public void deletingProcessDeletesActors() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionService transactionService = tenantAccessor.getTransactionService();
        final String userTaskName = "actNaturally";
        final ProcessDefinition definition = deployAndEnableProcessWithOneHumanTask("deletingProcessDeletesActors", "CandidateForOscarReward", userTaskName);

        final ProcessInstance processInstanceId = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(userTaskName, processInstanceId);

        disableAndDeleteProcess(definition);

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        transactionService.begin();
        final List<SActor> actors = getTenantAccessor().getActorMappingService().getActors(definition.getId());
        transactionService.complete();

        // Check there is no actor left:
        assertEquals(0, actors.size());
    }

    @Test
    public void deletingProcessDeletesActorMappings() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final TransactionService transactionService = tenantAccessor.getTransactionService();
        final String userTaskName = "actNaturally";
        final ProcessDefinition definition = deployAndEnableProcessWithOneHumanTask("deletingProcessDeletesActorMappings", "CandidateForOscarReward",
                userTaskName);

        final ProcessInstance processInstanceId = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(userTaskName, processInstanceId);

        disableAndDeleteProcess(definition);

        setSessionInfo(getSession()); // the session was cleaned by api call. This must be improved
        transactionService.begin();
        final List<SActorMember> actorMembers = getTenantAccessor().getActorMappingService().getActorMembersOfUser(john.getId());
        transactionService.complete();

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
        return deployAndEnableWithActor(processBuilder.done(), actorName, john);
    }

}
