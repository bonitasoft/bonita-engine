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
package org.bonitasoft.engine.core.connector;

import java.util.Collections;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bonitasoft.engine.connector.Connector;

/**
 * Contains the instantiated connector and its result
 * It is used to give make the connector service return both the instantiated connector and its result
 * in order to be able to call disconnect on it in the execute operation method
 *
 * @author Baptiste Mesta
 */
@Data
@AllArgsConstructor
public class ConnectorResult {

    private Connector connector;
    private Map<String, Object> result;

    public Map<String, Object> getResult() {
        if (result == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(result);
    }

}
