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
package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;

/**
 * @author Baptiste Mesta
 */
public class SNotSerializableException extends SBonitaException {

    private static final long serialVersionUID = -229226043369898514L;

    public SNotSerializableException(final String connectorDefinitionId, final String connectorDefinitionVersion, final String key, final Object value) {
        super(createMessage(connectorDefinitionId, connectorDefinitionVersion, key, value));
    }

    private static String createMessage(final String connectorDefinitionId, final String connectorDefinitionVersion, final String key, final Object value) {
        final StringBuilder stringBuilder = new StringBuilder("the connector ");
        stringBuilder.append(connectorDefinitionId);
        stringBuilder.append(' ');
        stringBuilder.append(connectorDefinitionVersion);
        stringBuilder.append(" have an unserializable output and was called directly from the api. name=");
        stringBuilder.append(key);
        stringBuilder.append(" value=");
        stringBuilder.append(value.toString());
        return stringBuilder.toString();
    }

}
