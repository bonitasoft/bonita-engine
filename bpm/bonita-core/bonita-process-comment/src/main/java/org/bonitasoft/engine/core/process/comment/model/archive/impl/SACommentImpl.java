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
package org.bonitasoft.engine.core.process.comment.model.archive.impl;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
@Data
@NoArgsConstructor
public class SACommentImpl implements SAComment {

    private long id;
    private long tenantId;
    private Long userId;
    private long processInstanceId;
    private long sourceObjectId;
    private long postDate;
    private long archiveDate;
    private String content;

    public SACommentImpl(final SComment sComment) {
        tenantId = sComment.getTenantId();
        content = sComment.getContent();
        postDate = sComment.getPostDate();
        sourceObjectId = sComment.getId();
        processInstanceId = sComment.getProcessInstanceId();
        userId = sComment.getUserId();
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SComment.class;
    }

}
