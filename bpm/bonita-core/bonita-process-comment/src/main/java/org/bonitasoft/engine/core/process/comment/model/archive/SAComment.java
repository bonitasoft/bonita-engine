/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.comment.model.archive;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@Entity
@Table(name = "arch_process_comment")
@IdClass(PersistentObjectId.class)
public class SAComment implements ArchivedPersistentObject {

    public static final String ID_KEY = "id";
    public static final String USERID_KEY = "userId";
    public static final String PROCESSINSTANCEID_KEY = "processInstanceId";
    public static final String POSTDATE_KEY = "postDate";
    public static final String CONTENT_KEY = "content";
    public static final String ARCHIVEDATE_KEY = "archiveDate";
    public static final String SOURCEOBJECTID_KEY = "sourceObjectId";
    @Id
    private long id;
    @Id
    private long tenantId;
    private Long userId;
    private long processInstanceId;
    private long sourceObjectId;
    private long postDate;
    private long archiveDate;
    private String content;

    public SAComment(final SComment sComment) {
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
