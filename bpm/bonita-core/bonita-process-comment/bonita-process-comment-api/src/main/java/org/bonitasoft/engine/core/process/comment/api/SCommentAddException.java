/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
