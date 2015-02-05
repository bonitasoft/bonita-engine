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
package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class CommentServiceTest extends CommonBPMServicesTest {

    private static TransactionService transactionService;

    private static SCommentService commentService;

    public CommentServiceTest() {
        transactionService = getServicesBuilder().getTransactionService();
        commentService = getServicesBuilder().getCommentService();
    }

    @Test
    public void testCreateAndRetrieveShortComment() throws Exception {
        createAndRetrieveComment("I have to comment this decision because ...");
    }

    @Test
    public void testCreateAndRetrieveLongComment() throws Exception {
        // longer than 50 characters:
        createAndRetrieveComment("I have to comment this decision because I really think the reasons you invoke to justify it may not be relevant");
    }

    private void createAndRetrieveComment(final String commentContent) throws Exception {
        final long processInstanceId = 123456L;

        transactionService.begin();
        final SComment comment = commentService.addComment(processInstanceId, commentContent);
        transactionService.complete();
        assertNotNull(comment);

        transactionService.begin();
        final List<SComment> comments = commentService.getComments(processInstanceId, new QueryOptions(0, 50, SComment.class, "id", OrderByType.ASC));
        transactionService.complete();
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(commentContent, comments.get(0).getContent());

        transactionService.begin();
        commentService.delete(comment);
        transactionService.complete();
    }

    // @Test(expected = SCategoryAlreadyExistsException.class)
    public void testCreateCategoryWithSCategoryAlreadyExistsException() {
        // final BusinessTransaction tx = transactionService.createTransaction();
        // final String name = "categoryTestExceptionName";
        // final String description = "test create category with SCategoryAlreadyExistsException";
        // tx.begin();
        // final SCategory category = categoryService.createCategory(name, description);
        // assertNotNull(category);
        // assertEquals(name, category.getName());
        // try {
        // categoryService.createCategory(name, description);
        // } finally {
        // categoryService.deleteCategory(category.getId());
        // tx.complete();
        // }
    }

}
