package org.bonitasoft.engine.process.comment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.ArchivedCommentsSearchDescriptor;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CommentTest extends CommonAPITest {

    protected User user;

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser(USERNAME, PASSWORD);
    }

    @After
    public void after() throws Exception {
        deleteUser(user);
        logoutOnTenant();
    }

    @Test
    public void addComment() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final String commentContent = "abc";
        final Comment comment = getProcessAPI().addProcessComment(pi0.getId(), commentContent);
        assertNotNull(comment);
        assertEquals(commentContent, comment.getContent());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getComments() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        // get comments before add new.
        final List<Comment> comments1 = getProcessAPI().searchComments(
                new SearchOptionsBuilder(0, 100).filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, pi0.getId()).done()).getResult();
        // add comments
        final String commentContent = "abc";
        getProcessAPI().addProcessComment(pi0.getId(), commentContent);
        getProcessAPI().addProcessComment(pi0.getId(), commentContent);
        getProcessAPI().addProcessComment(pi0.getId(), commentContent);
        // get comments again.
        final List<Comment> comments2 = getProcessAPI().searchComments(
                new SearchOptionsBuilder(0, 100).filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, pi0.getId()).done()).getResult();
        assertEquals(comments1.size() + 3, comments2.size());
        assertEquals(commentContent, comments2.get(1).getContent());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void allCommentsAreArchived() throws Exception {
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("allCommentsAreArchived", "0.2");
        processBuilder.addActor(ACTOR_NAME);
        final String activityName = "maTache";
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask(activityName, ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance pi = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < 21; i++) {
            getProcessAPI().addProcessComment(pi.getId(), "myComment_" + i);
        }
        waitForUserTaskAndExecuteIt(activityName, pi, user);
        waitForProcessToFinish(pi);
        final SearchResult<ArchivedComment> searchArchivedComments = getProcessAPI().searchArchivedComments(
                new SearchOptionsBuilder(0, 30).filter(ArchivedCommentsSearchDescriptor.PROCESS_INSTANCE_ID, pi.getId()).done());
        assertTrue("At least 21 comments should have been retrieved (+ possible automatic comments)", searchArchivedComments.getCount() >= 21);

        disableAndDeleteProcess(processDefinition);
    }
}
