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

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.model.SComment;

/**
 * @author Zhang Bole
 */
public class GetComments implements TransactionContent {

    private final SCommentService CommentService;

    private final long processInstanceId;

    private List<SComment> sComment;

    public GetComments(SCommentService commentService, long processInstanceId) {
        super();
        CommentService = commentService;
        this.processInstanceId = processInstanceId;
    }

    @Override
    public void execute() throws SBonitaException {
        sComment = CommentService.getComments(processInstanceId);
    }

    public List<SComment> getResult() {
        return sComment;
    }
}
