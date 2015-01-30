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
package org.bonitasoft.engine.bpm.flownode;

import org.bonitasoft.engine.exception.NotFoundException;

/**
 * Thrown when it's not possible to find the {@link TimerEventTriggerInstance}.
 * 
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public class TimerEventTriggerInstanceNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the specified detail cause.
     * 
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is permitted, and indicates that the
     *            cause is nonexistent or unknown.)
     */
    public TimerEventTriggerInstanceNotFoundException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param timerEventTriggerInstanceId
     *            The identifier of the timer trigger instance who doesn't find
     */
    public TimerEventTriggerInstanceNotFoundException(final long timerEventTriggerInstanceId) {
        super("Can't find the TimerEventInstance with id=" + timerEventTriggerInstanceId);
    }
}
