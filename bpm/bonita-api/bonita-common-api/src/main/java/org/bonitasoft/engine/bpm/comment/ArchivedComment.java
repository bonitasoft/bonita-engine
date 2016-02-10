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

import java.util.Date;

import org.bonitasoft.engine.bpm.ArchivedElement;
import org.bonitasoft.engine.bpm.BaseElement;
import org.bonitasoft.engine.bpm.NamedElement;

/**
 * The archived comment associated to a process instance
 * 
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ArchivedComment extends NamedElement, BaseElement, ArchivedElement {

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
    Date getPostDate();

    /**
     * @return The content of the comment
     */
    String getContent();

}
