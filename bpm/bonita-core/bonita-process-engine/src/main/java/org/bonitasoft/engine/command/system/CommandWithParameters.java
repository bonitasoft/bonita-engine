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
package org.bonitasoft.engine.command.system;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public abstract class CommandWithParameters extends TenantCommand {

    @SuppressWarnings("unchecked")
    protected <T> T getParameter(final Map<String, Serializable> parameters, final String parameterName, final String message)
            throws SCommandParameterizationException {
        try {
            return (T) parameters.get(parameterName);
        } catch (final Exception e) {
            throw new SCommandParameterizationException(message);
        }
    }

    protected <T> T getParameter(final Map<String, Serializable> parameters, final String parameterName) throws SCommandParameterizationException {
        return getParameter(parameters, parameterName, "An error occurred while parsing " + parameterName);
    }

    protected Long getLongMandadoryParameter(final Map<String, Serializable> parameters, final String field) throws SCommandParameterizationException {
        final String message = "Parameters map must contain an entry " + field + " with a long value.";
        final Long mandatoryParameter = getMandatoryParameter(parameters, field, message);
        if (mandatoryParameter == 0L) {
            throw new SCommandParameterizationException(message);
        }
        return mandatoryParameter;
    }

    protected Integer getIntegerMandadoryParameter(final Map<String, Serializable> parameters, final String field) throws SCommandParameterizationException {
        final String message = "Parameters map must contain an entry " + field + " with a int value.";
        return getMandatoryParameter(parameters, field, message);
    }

    protected String getStringMandadoryParameter(final Map<String, Serializable> parameters, final String field) throws SCommandParameterizationException {
        final String message = "Parameters map must contain an entry " + field + " with a String value.";
        return getMandatoryParameter(parameters, field, message);
    }

    protected <T> T getMandatoryParameter(final Map<String, Serializable> parameters, final String field, final String message)
            throws SCommandParameterizationException {
        final T value = getParameter(parameters, field, message);
        if (value == null) {
            throw new SCommandParameterizationException(message);
        }
        return value;
    }

}
