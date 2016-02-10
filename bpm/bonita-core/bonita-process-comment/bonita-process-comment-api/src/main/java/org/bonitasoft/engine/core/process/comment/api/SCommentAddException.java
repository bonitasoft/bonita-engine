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
package org.bonitasoft.engine.core.process.comment.api;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Hongwen Zang
 * @author Celine Souchet
 * 
 */
public class SCommentAddException extends SBonitaException {

    private static final long serialVersionUID = 6387069363737827464L;

    public SCommentAddException(String message, Exception cause) {
        super(message, cause);
    }

    public SCommentAddException(String message) {
        super(message);
    }

    public SCommentAddException(Exception cause) {
        super(cause);
    }

    public SCommentAddException(long processInstanceId, final String commentType, final Exception e) {
        super("Can't create a " + commentType + " comment on the process instance.", e);
        setProcessInstanceIdOnContext(processInstanceId);
    }

}
