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
package org.bonitasoft.engine.bpm.comment;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * The comment associated to a process instance
 *
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public interface Comment extends BonitaObject {

    /**
     * @return The identifier of the tenant
     * @deprecated As of 6.1 use {@link org.bonitasoft.engine.session.APISession#getTenantId()} instead
     */
    @Deprecated
    long getTenantId();

    /**
     * @return The identifier of the comment
     */
    long getId();

    /**
     * @return The identifier of the user that posted the comment
     */
    Long getUserId();

    /**
     * @return The identifier of the process instance associated to the comment
     */
    long getProcessInstanceId();

    /**
     * @return The date to which the comment was posted.
     */
    long getPostDate();

    /**
     * @return The content of the comment
     */
    String getContent();

}
