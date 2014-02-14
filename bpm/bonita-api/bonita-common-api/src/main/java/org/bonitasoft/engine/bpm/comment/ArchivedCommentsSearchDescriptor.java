/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.comment;

/**
 * The fields on which a search can be made for the archived comments.
 * 
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class ArchivedCommentsSearchDescriptor {

    /**
     * The identifier of the process instance associated to the comment
     */
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";

    /**
     * The identifier of the user that posted the comment
     */
    public static final String POSTED_BY_ID = "userId";

    /**
     * The identifier of the archived comment
     */
    public static final String ID = "id";

    /**
     * The username of the user that posted the comment
     */
    public static final String USER_NAME = "userName";

    /**
     * The content of the comment
     */
    public static final String CONTENT = "content";

    /**
     * The date to which the comment was posted.
     */
    public static final String POSTDATE = "postdate";

    /**
     * The date to which the comment was archived.
     */
    public static final String ARCHIVE_DATE = "archiveDate";

    /**
     * The identifier of the comment (not archived)
     */
    public static final String SOURCE_OBJECT_ID = "sourceObjectId";
}
