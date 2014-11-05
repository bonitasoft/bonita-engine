/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
     * @deprecated As of 6.1 use {@link APISession#getTenantId()} instead
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
