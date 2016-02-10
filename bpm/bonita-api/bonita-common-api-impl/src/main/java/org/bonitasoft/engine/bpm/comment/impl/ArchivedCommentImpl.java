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
package org.bonitasoft.engine.bpm.comment.impl;

import java.util.Date;

import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Zhang Bole
 */
public class ArchivedCommentImpl extends NamedElementImpl implements ArchivedComment {

    private static final long serialVersionUID = -6573747806944970703L;

    private long id;

    private Date archiveDate;

    private long processInstanceId;

    private long sourceObjectId;

    private Long userId;

    private Date postDate;

    private String content;

    public ArchivedCommentImpl(final String name) {
        super(name);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(final Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(final long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    @Override
    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(final Date postDate) {
        this.postDate = postDate;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }
}
