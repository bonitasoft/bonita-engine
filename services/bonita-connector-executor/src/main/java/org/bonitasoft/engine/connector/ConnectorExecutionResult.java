/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.connector;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectorExecutionResult {

    public static class ConnectorExecutionResultBuilder {

        private ConnectorExecutionResult connectorExecutionResult;

        ConnectorExecutionResultBuilder(Map<String, Object> outputs) {
            connectorExecutionResult = new ConnectorExecutionResult(outputs);
        }

        public ConnectorExecutionResult tookMillis(long executionTimeMillis) {
            connectorExecutionResult.executionTimeMillis = executionTimeMillis;
            return connectorExecutionResult;
        }

    }

    public static ConnectorExecutionResultBuilder result(Map<String, Object> outputs) {
        return new ConnectorExecutionResultBuilder(outputs);
    }

    private long executionTimeMillis;
    private Map<String, Object> outputs;

    private ConnectorExecutionResult(Map<String, Object> outputs) {
        this.outputs = outputs;
    }

}
