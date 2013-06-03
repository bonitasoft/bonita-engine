/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.api.impl.transaction.comment;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.SComment;

/**
 * @author Hongwen Zang
 */
public class AddComment implements TransactionContent {

    private final SCommentService CommentService;

    private final long processInstanceId;

    private final String comment;

    private SComment sComment;

    public AddComment(SCommentService commentService, long processInstanceId, String comment) {
        super();
        CommentService = commentService;
        this.processInstanceId = processInstanceId;
        this.comment = comment;
    }

    @Override
    public void execute() throws SBonitaException {
        sComment = CommentService.addComment(processInstanceId, comment);
    }

    public SComment getResult() {
        return sComment;
    }
}
