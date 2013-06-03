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
package org.bonitasoft.engine.exceptions;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Baptiste Mesta
 * @since 6.0
 */
public interface ExceptionsManager {

    /**
     * Get all possible causes for specified exception
     * 
     * @param exceptionId
     *            Identifier of exception
     * @return a list of causes
     */
    List<String> getPossibleCauses(String exceptionId);

    /**
     * @param exceptionId
     *            The exceptionId of the causes
     * @param parameters
     *            Parameters that will be injected in the causes
     * @return causes of the exception
     */
    List<String> getPossibleCauses(String exceptionId, Object... parameters);

    /**
     * @param e
     *            The exception on which we want causes
     * @return a list of causes of the exception
     */
    List<String> getPossibleCauses(SBonitaException e);

}
