/**
 * Copyright (C) 2024 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.commons.exceptions;

public interface ScopedException {

    String UNKNOWN_SCOPE = "UNKNOWN";
    String GENERAL_INFORMATION = "General information";
    String OPERATION = "Operation";
    String EVENT = "Event";
    String ITERATION = "Iteration";
    String CONNECTOR = "Connector";
    String DATA = "Data initialization";
    String ACTOR_MAPPING = "Actor mapping";
    String OUTGOING_TRANSITION = "Outgoing transition";

    /**
     * Describe the scope of the exception with a human-readable category.
     *
     * @return The scope of the exception
     */
    default String getScope() {
        return UNKNOWN_SCOPE;
    }

}
