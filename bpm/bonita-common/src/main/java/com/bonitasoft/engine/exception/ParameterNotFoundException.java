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
package com.bonitasoft.engine.exception;

import java.text.MessageFormat;

import org.bonitasoft.engine.exception.BonitaException;

/**
 * @author Matthieu Chaffotte
 */
public class ParameterNotFoundException extends BonitaException {

    private static final long serialVersionUID = -5548436489951596184L;

    public ParameterNotFoundException(final Throwable cause) {
        super(cause);
    }

    public ParameterNotFoundException(final long processDefinitionId, final String parameterName) {
        super(MessageFormat.format("the parameter with name {0} and process with id {1}", parameterName, processDefinitionId));
    }

}
