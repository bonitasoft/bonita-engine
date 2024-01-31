/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.command.system;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.RuntimeCommand;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @deprecated since 9.0.0, use {@link RuntimeCommand} instead
 */
@Deprecated(forRemoval = true, since = "9.0.0")
public abstract class CommandWithParameters extends TenantCommand {

    @Deprecated
    protected Long getLongMandadoryParameter(final Map<String, Serializable> parameters, final String field)
            throws SCommandParameterizationException {
        return getLongMandatoryParameter(parameters, field);
    }

    @Deprecated
    protected Integer getIntegerMandadoryParameter(final Map<String, Serializable> parameters, final String field)
            throws SCommandParameterizationException {
        return getIntegerMandatoryParameter(parameters, field);
    }

    @Deprecated
    protected String getStringMandadoryParameter(final Map<String, Serializable> parameters, final String field)
            throws SCommandParameterizationException {
        return getStringMandatoryParameter(parameters, field);
    }

}
