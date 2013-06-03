/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class BonitaCausePrinter extends Exception {

    private static final long serialVersionUID = 5902890485495844428L;

    private final BonitaException exception;

    private final int index;

    public BonitaCausePrinter(final BonitaException exception) {
        this.exception = exception;
        index = 0;
    }

    public BonitaCausePrinter(final BonitaCausePrinter exception) {
        this.exception = exception.exception;
        index = exception.index + 1;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return exception.getStackTrace(index);
    }

    @Override
    public Throwable getCause() {
        final int[] causesPositions = exception.getCausesPositions();
        if (causesPositions != null && index < causesPositions.length - 1) {
            return new BonitaCausePrinter(this);
        } else {
            return null;
        }
    }

}
