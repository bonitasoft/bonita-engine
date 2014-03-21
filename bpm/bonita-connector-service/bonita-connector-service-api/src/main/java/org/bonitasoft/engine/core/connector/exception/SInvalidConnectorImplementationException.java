/**
 * Copyright (C) 2011, 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.connector.exception;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;

/**
 * @author Feng Hui
 * @author Celine Souchet
 */
public class SInvalidConnectorImplementationException extends SBonitaException {

    private static final long serialVersionUID = -3113075377405323282L;

    public SInvalidConnectorImplementationException(final String message, final SConnectorImplementationDescriptor connectorImplementationDescriptor) {
        this(message);
        setConnectorDefinitionImplementationClassNameOnContext(connectorImplementationDescriptor.getImplementationClassName());
        setConnectorDefinitionIdOnContext(connectorImplementationDescriptor.getDefinitionId());
        setConnectorDefinitionVersionOnContext(connectorImplementationDescriptor.getDefinitionVersion());
    }

    public SInvalidConnectorImplementationException(final String message) {
        super(message);
    }

    public SInvalidConnectorImplementationException(final Throwable cause) {
        super(cause);
    }

    public SInvalidConnectorImplementationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
