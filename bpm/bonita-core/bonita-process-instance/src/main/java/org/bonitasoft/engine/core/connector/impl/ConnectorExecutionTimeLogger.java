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
package org.bonitasoft.engine.core.connector.impl;

import java.util.Map;
import java.util.stream.Collectors;

import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * Log connector execution time when the connector took more than the configured threshold
 */
public class ConnectorExecutionTimeLogger {

    private final TechnicalLogger logger;
    private Long warnWhenLongerThanMillis;

    /**
     * @param technicalLoggerService the technical logger
     * @param warnWhenLongerThanMillis the duration in milli seconds above which the time taken will be logged as a
     *        warning
     */
    public ConnectorExecutionTimeLogger(TechnicalLoggerService technicalLoggerService,
            Long warnWhenLongerThanMillis) {
        logger = technicalLoggerService.asLogger(ConnectorExecutionTimeLogger.class);
        this.warnWhenLongerThanMillis = warnWhenLongerThanMillis;
    }

    public void log(long processDefinitionId, SConnectorInstance sConnectorInstance, Connector connector,
            Map<String, Object> inputParameters, long executionTimeMillis) {
        if (executionTimeMillis < warnWhenLongerThanMillis) {
            return;
        }
        logger.warn(
                "Connector {} with id {} with class {} of process definition {} on element {} took {} ms.",
                sConnectorInstance.getName(), sConnectorInstance.getId(), connector.getClass().getName(),
                processDefinitionId, printContext(sConnectorInstance), executionTimeMillis);
        if (logger.isDebugEnabled()) {
            logger.debug("Input parameters of the connector with id {}: {}", sConnectorInstance.getId(),
                    print(inputParameters));
        }
    }

    private String print(Map<String, Object> inputParameters) {
        return inputParameters.entrySet().stream()
                .map((e -> e.getKey() + ": [" + e.getValue().toString().replaceAll("([\\r\\n])", " ") + "]"))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private String printContext(SConnectorInstance sConnectorInstance) {
        return sConnectorInstance.getContainerType() + " with id " + sConnectorInstance.getContainerId();
    }
}
