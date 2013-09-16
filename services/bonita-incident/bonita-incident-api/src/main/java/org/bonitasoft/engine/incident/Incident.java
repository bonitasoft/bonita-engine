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
package org.bonitasoft.engine.incident;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
public class Incident {

    private final String description;

    private final String recoveryProcedure;

    private final Exception cause;

    private final Exception exceptionWhenHandlingFailure;

    public Incident(final String description, final String recoveryProcedure, final Exception cause, final Exception exceptionWhenHandlingFailure) {
        super();
        this.description = description;
        this.recoveryProcedure = recoveryProcedure;
        this.cause = cause;
        this.exceptionWhenHandlingFailure = exceptionWhenHandlingFailure;
    }

    public String getDescription() {
        return description;
    }

    public String getRecoveryProcedure() {
        return recoveryProcedure;
    }

    public Exception getCause() {
        return cause;
    }

    public Exception getExceptionWhenHandlingFailure() {
        return exceptionWhenHandlingFailure;
    }

    @Override
    public String toString() {
        return "Incident [description=" + description + ", recoveryProcedure=" + recoveryProcedure + "]";
    }

}
