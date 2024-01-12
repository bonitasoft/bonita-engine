/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.bpm.connector;

import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.web.rest.model.bpm.connector.ConnectorInstanceItem;
import org.bonitasoft.web.rest.server.framework.api.EnumConverter;

/**
 * Convenient object to convert ATTRIBUTE_VALUE from {@link ConnectorInstanceItem} into {@link ConnectorState}
 *
 * @author Vincent Elcrin
 */
public class ConnectorInstanceStateConverter implements EnumConverter<ConnectorState> {

    public final ConnectorState convert(final String attributeStateValue) {
        if (ConnectorInstanceItem.VALUE_STATE_DONE.equals(attributeStateValue)) {
            return ConnectorState.DONE;
        } else if (ConnectorInstanceItem.VALUE_STATE_FAILED.equals(attributeStateValue)) {
            return ConnectorState.FAILED;
        } else if (ConnectorInstanceItem.VALUE_STATE_SKIPPED.equals(attributeStateValue)) {
            return ConnectorState.SKIPPED;
        } else if (ConnectorInstanceItem.VALUE_STATE_TO_BE_EXECUTED.equals(attributeStateValue)) {
            return ConnectorState.TO_BE_EXECUTED;
        } else if (ConnectorInstanceItem.VALUE_STATE_TO_RE_EXECUTE.equals(attributeStateValue)) {
            return ConnectorState.TO_RE_EXECUTE;
        } else {
            throw new RuntimeException("Can't convert following state into engine state <" + attributeStateValue + ">");
        }
    }

    @Override
    public String convert(ConnectorState enumValue) {
        switch (enumValue) {
            case DONE:
                return ConnectorInstanceItem.VALUE_STATE_DONE;
            case FAILED:
                return ConnectorInstanceItem.VALUE_STATE_FAILED;
            case SKIPPED:
                return ConnectorInstanceItem.VALUE_STATE_SKIPPED;
            case TO_BE_EXECUTED:
                return ConnectorInstanceItem.VALUE_STATE_TO_BE_EXECUTED;
            case TO_RE_EXECUTE:
                return ConnectorInstanceItem.VALUE_STATE_TO_RE_EXECUTE;
            default:
                throw new RuntimeException("Can't convert <" + enumValue + ">. Flow node type not supported.");
        }

    }
}
