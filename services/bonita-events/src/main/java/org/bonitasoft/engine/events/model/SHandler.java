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
package org.bonitasoft.engine.events.model;

import java.io.Serializable;

/**
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 */
public interface SHandler<T extends SEvent> extends Serializable {

    /**
     * Performs the action corresponding to the given Event
     */
    void execute(T event) throws SHandlerExecutionException;

    /**
     * Precise if the current Handler is interested by the given event.
     * If so, it could run the execute(SEvent e) method.
     */
    boolean isInterested(T event);

    /**
     * Returns a unique identifier for each instance of the Handler
     */
    String getIdentifier();
}
