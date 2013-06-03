/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthieu Chaffotte
 */
public class BonitaRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -5413586694735909486L;

    public BonitaRuntimeException(final String message) {
        super(message);
    }

    public BonitaRuntimeException(final String message, final Throwable cause) {
        super(message);
        this.mergeStacks(cause);
    }

    public BonitaRuntimeException(final Throwable cause) {
        super(cause.getMessage());
        this.mergeStacks(cause);
    }

    /**
     * @param cause
     */
    private void mergeStacks(final Throwable cause) {
        final StackTraceElement[] currentStack = this.getStackTrace();
        final List<StackTraceElement[]> causesStacks = new ArrayList<StackTraceElement[]>();
        final List<Throwable> exceptions = new ArrayList<Throwable>();
        Throwable subCause = cause;
        int causeslength = 0;
        do {
            final StackTraceElement[] stackTrace = subCause.getStackTrace();
            causesStacks.add(stackTrace);
            exceptions.add(subCause);
            causeslength += stackTrace.length + 1;
        } while ((subCause = subCause.getCause()) != null);
        final StackTraceElement[] mergedStackTrace = new StackTraceElement[currentStack.length + causeslength + 1];
        System.arraycopy(currentStack, 0, mergedStackTrace, 0, currentStack.length);
        mergedStackTrace[currentStack.length] = new StackTraceElement("══════════════════════════", "<server stack trace>", "", -3);
        int current = currentStack.length + 1;
        int i = 0;
        for (final StackTraceElement[] stackTraceElements : causesStacks) {
            System.arraycopy(stackTraceElements, 0, mergedStackTrace, current, stackTraceElements.length);
            current += stackTraceElements.length;
            mergedStackTrace[current] = new StackTraceElement("", "Caused By:", exceptions.get(i).getMessage(), -3);
            current += 1;
            i++;
        }
        this.setStackTrace(mergedStackTrace);
    }
}
