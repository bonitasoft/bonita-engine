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
package org.bonitasoft.engine.process.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.ArchivedCommentsSearchDescriptor;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Test;

public class CommentIT extends TestWithUser {

    @Test
    public void should_be_able_to_add_search_and_archive_comments() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        String taskName = "userTask1";
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask(taskName, ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        // get comments before add new.
        final List<Comment> originalComments = getProcessAPI().searchComments(
                new SearchOptionsBuilder(0, 100).filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, pi0.getId()).done()).getResult();
        // add comments
        final String commentContent = "added comment 0";
        final Comment comment = getProcessAPI().addProcessComment(pi0.getId(), commentContent);
        assertNotNull(comment);
        assertEquals(commentContent, comment.getContent());
        getProcessAPI().addProcessComment(pi0.getId(), "added comment 1");
        Comment comment2 = getProcessAPI().addProcessComment(pi0.getId(), "added comment 2");
        // get comments again.
        final List<Comment> commentsAfterAdd = getProcessAPI().searchComments(
                new SearchOptionsBuilder(0, 100)
                        .filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, pi0.getId())
                        .sort(SearchCommentsDescriptor.POSTDATE, Order.ASC)
                        .done())
                .getResult();
        assertThat(commentsAfterAdd).as("should have 3 more comments").hasSize(originalComments.size() + 3);
        assertThat(commentsAfterAdd.get(1).getContent()).isEqualTo("added comment 1");

        // Archive comments
        waitForUserTaskAndExecuteIt(pi0, taskName, user);
        waitForProcessToFinish(pi0);

        final SearchResult<ArchivedComment> searchArchivedComments = getProcessAPI().searchArchivedComments(
                new SearchOptionsBuilder(0, 10).filter(ArchivedCommentsSearchDescriptor.PROCESS_INSTANCE_ID, pi0.getId()).done());
        assertThat(searchArchivedComments.getCount()).as("At least 3 comments should have been retrieved (+ possible automatic comments)")
                .isGreaterThanOrEqualTo(3);

        // ensure we can retrieve archived comments using the SOURCE_OBJECT_ID field
        final SearchResult<ArchivedComment> searchArchivedCommentsBySourceObjectId = getProcessAPI().searchArchivedComments(
                new SearchOptionsBuilder(0, 30).filter(ArchivedCommentsSearchDescriptor.SOURCE_OBJECT_ID, comment2.getId()).done());
        assertThat(searchArchivedCommentsBySourceObjectId.getCount()).isEqualTo(1);
        assertThat(searchArchivedCommentsBySourceObjectId.getResult().get(0).getContent()).isEqualTo("added comment 2");

        // ensure we can retrieve archived comments using the CONTENT field
        final SearchResult<ArchivedComment> searchArchivedCommentsByContent = getProcessAPI().searchArchivedComments(
                new SearchOptionsBuilder(0, 30).filter(ArchivedCommentsSearchDescriptor.CONTENT, "added comment 2").done());
        assertThat(searchArchivedCommentsByContent.getCount()).isEqualTo(1);
        assertThat(searchArchivedCommentsByContent.getResult().get(0).getSourceObjectId()).isEqualTo(comment2.getId());

        disableAndDeleteProcess(processDefinition);
    }

}
