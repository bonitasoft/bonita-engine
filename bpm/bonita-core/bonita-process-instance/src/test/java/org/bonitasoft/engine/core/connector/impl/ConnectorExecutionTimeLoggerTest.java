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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class ConnectorExecutionTimeLoggerTest {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    private static final long MAX_DURATION_IN_MILLIS_THRESHOLD = 1000L;
    private ConnectorExecutionTimeLogger connectorExecutionTimeLogger = new ConnectorExecutionTimeLogger(
            new TechnicalLoggerSLF4JImpl(), MAX_DURATION_IN_MILLIS_THRESHOLD);

    @Test
    public void should_log_a_warning_when_connector_takes_more_time_than_the_defined_threshold() {
        systemOutRule.clearLog();
        HashMap<String, Object> inputParameters = new HashMap<>();
        inputParameters.put("serviceUrl", "http://some.external.service/1234?call=true");
        inputParameters.put("groovyScript", "import something\n" +
                "\n" +
                "Thread.sleep(10000)");
        SConnectorInstance sConnectorInstance = new SConnectorInstance("myConnector", 5555L,
                SConnectorInstance.FLOWNODE_TYPE, "theConnectorId", "1.0.0", ConnectorEvent.ON_ENTER);
        sConnectorInstance.setId(333L);

        connectorExecutionTimeLogger.log(123L,
                sConnectorInstance,
                new ConnectorServiceImplTest.MyTestConnector(),
                inputParameters,
                1500);

        assertThat(systemOutRule.getLog()).contains(
                "Connector myConnector with id 333 with class org.bonitasoft.engine.core.connector.impl.ConnectorServiceImplTest$MyTestConnector"
                        +
                        " of process definition 123 on element flowNode with id 5555 took 1500 ms.");
        assertThat(systemOutRule.getLog()).contains(
                "Input parameters of the connector with id 333: {groovyScript: [import something  Thread.sleep(10000)], serviceUrl: [http://some.external.service/1234?call=true]}");
    }

    @Test
    public void should_not_log_when_connector_takes_less_than_the_defined_threashold() {
        systemOutRule.clearLog();
        SConnectorInstance sConnectorInstance = new SConnectorInstance("myConnector", 5555L,
                SConnectorInstance.FLOWNODE_TYPE, "theConnectorId", "1.0.0", ConnectorEvent.ON_ENTER);

        connectorExecutionTimeLogger.log(123L,
                sConnectorInstance,
                new ConnectorServiceImplTest.MyTestConnector(),
                Collections.emptyMap(),
                100);

        assertThat(systemOutRule.getLog()).isEmpty();
    }

}
