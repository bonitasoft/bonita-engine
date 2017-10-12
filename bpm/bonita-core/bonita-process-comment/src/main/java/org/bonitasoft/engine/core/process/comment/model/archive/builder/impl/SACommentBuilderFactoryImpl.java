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
package org.bonitasoft.engine.core.process.comment.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilder;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilderFactory;

/**
 * @author Zhang Bole
 * @author Hongwen Zang
 */
public class SACommentBuilderFactoryImpl implements SACommentBuilderFactory {

    private static final String ID_KEY = "id";

    private static final String TENANTID_KEY = "tenantId";

    private static final String USERID_KEY = "userId";

    private static final String PROCESSINSTANCEID_KEY = "processInstanceId";

    private static final String POSTDATE_KEY = "postDate";

    private static final String CONTENT_KEY = "content";

    private static final String ARCHIVEDATE_KEY = "archiveDate";

    private static final String SOURCEOBJECTID_KEY = "sourceObjectId";

    @Override
    public SACommentBuilder createNewInstance(final SComment sComment) {
        return new SACommentBuilderImpl(sComment);
    }

    @Override
    public String getIdKey() {
        return ID_KEY;
    }

    @Override
    public String getTenantIdKey() {
        return TENANTID_KEY;
    }

    @Override
    public String getUserIdKey() {
        return USERID_KEY;
    }

    @Override
    public String getProcessInstanceIdKey() {
        return PROCESSINSTANCEID_KEY;
    }

    @Override
    public String getPostDateKey() {
        return POSTDATE_KEY;
    }

    @Override
    public String getContentKey() {
        return CONTENT_KEY;
    }

    @Override
    public String getArchiveDate() {
        return ARCHIVEDATE_KEY;
    }

    @Override
    public String getSourceObjectId() {
        return SOURCEOBJECTID_KEY;
    }
}
