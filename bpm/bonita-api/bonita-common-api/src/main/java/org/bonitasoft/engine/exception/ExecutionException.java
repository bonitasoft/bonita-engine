/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.exception;

/**
 * Thrown when a problem occurs:
 * <ul>
 * <li>while executing a BPM entity (activity, process, event, ...)</li>
 * <li>while executing an administration operation (startPlatform, activateTenant, etc.)</li>
 * <li>...</li>
 * </ul>
 * 
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class ExecutionException extends BonitaException {

    private static final long serialVersionUID = -424782295937066031L;

    public ExecutionException(final Throwable cause) {
        super(cause);
    }

    public ExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ExecutionException(final String message) {
        super(message);
    }

}
