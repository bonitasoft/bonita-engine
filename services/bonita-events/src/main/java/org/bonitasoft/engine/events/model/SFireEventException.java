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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Christophe Havard
 * @author Celine Souchet
 */
public class SFireEventException extends SBonitaException {

    private static final long serialVersionUID = 752699780388742999L;

    private List<Exception> handlerExceptions;

    public SFireEventException(final String message) {
        super(message);
    }

    public void addHandlerException(final Exception e) {
        if (handlerExceptions == null) {
            handlerExceptions = new ArrayList<Exception>();
        }
        handlerExceptions.add(e);
    }

    public List<Exception> getHandlerExceptions() {
        if (handlerExceptions != null) {
            return handlerExceptions;
        }
        return Collections.emptyList();
    }

}
