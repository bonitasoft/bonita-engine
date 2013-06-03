/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.exception;

/**
 * @author Celine Souchet
 */
public class NotSerializableException extends BonitaException {

    private static final long serialVersionUID = -5541516162117265707L;

    public NotSerializableException(final String connectorDefinitionId, final Throwable e) {
        super("Connector " + connectorDefinitionId
                + " executed successfully but output cannot be read correctly. See Bonita Engine log file for technical details.", e);
    }

    public NotSerializableException(final String connectorDefinitionId, final String connectorDefinitionVersion, final String key, final Object value) {
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
