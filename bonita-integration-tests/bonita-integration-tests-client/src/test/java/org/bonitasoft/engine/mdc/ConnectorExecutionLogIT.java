/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.mdc;

import static org.bonitasoft.engine.test.BuildTestUtil.generateConnectorImplementation;

import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.DoNothingConnector;
import org.bonitasoft.engine.connectors.FailingConnector;
import org.bonitasoft.engine.execution.ProcessExecutorImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.slf4j.LoggerFactory;

public class ConnectorExecutionLogIT extends CommonAPIIT {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @After
    public void afterTest() throws Exception {
        logout();
    }

    @Before
    public void beforeTest() throws Exception {
        loginWithTechnicalUser();
    }

    @Test
    public void executeConnectorAndInspectLogs() throws Exception {
        AutomaticTaskDefinitionBuilder automaticTask = new ProcessDefinitionBuilder()
                .createNewInstance("processWithConnectors", " 1.0")
                .addAutomaticTask("step");
        automaticTask.addConnector("enter1", "connectorDef1", "1.0", ConnectorEvent.ON_ENTER);

        BusinessArchive bar = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(automaticTask
                        .getProcess())
                .addConnectorImplementation(
                        generateConnectorImplementation("connectorDef1", "1.0", DoNothingConnector.class))
                .done();
        ProcessDefinition processDefinition = getProcessAPI().deploy(bar);
        getProcessAPI().enableProcess(processDefinition.getId());

        final Logger exeLogger = (Logger) LoggerFactory.getLogger(ProcessExecutorImpl.class);
        Level oldLogLevel = exeLogger.getLevel();
        try {
            exeLogger.setLevel(Level.DEBUG);
            // when
            systemOutRule.clearLog();
            ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

            waitForProcessToFinish(processInstance);
            // then we must have context for exception, even if it was logged outside of the initial throwing scope
            checkLogEntryContains(Map.of(MDCConstants.PROCESS_INSTANCE_ID, Long.toString(processInstance.getId()),
                    MDCConstants.PROCESS_DEFINITION_ID, Long.toString(processDefinition.getId()),
                    MDCConstants.ROOT_PROCESS_INSTANCE_ID, Long.toString(processInstance.getRootProcessInstanceId())));

        } finally {
            disableAndDeleteProcess(processDefinition);
            // restore old log level
            exeLogger.setLevel(oldLogLevel);
        }

    }

    @Test
    public void executeFailedConnectorAndInspectLogs() throws Exception {
        AutomaticTaskDefinitionBuilder automaticTask = new ProcessDefinitionBuilder()
                .createNewInstance("processWithFailedConnectors", " 1.0")
                .addAutomaticTask("step");
        automaticTask.addConnector("enter1", "connectorDef1", "1.0", ConnectorEvent.ON_ENTER);
        automaticTask.addConnector("enter2", "failingConnector", "1.0", ConnectorEvent.ON_ENTER);

        BusinessArchive bar = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(automaticTask
                        .getProcess())
                .addConnectorImplementation(
                        generateConnectorImplementation("connectorDef1", "1.0", DoNothingConnector.class))
                .addConnectorImplementation(
                        generateConnectorImplementation("failingConnector", "1.0", FailingConnector.class))
                .done();
        ProcessDefinition processDefinition = getProcessAPI().deploy(bar);
        getProcessAPI().enableProcess(processDefinition.getId());

        try {
            // when
            systemOutRule.clearLog();
            ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

            FlowNodeInstance failedTask = waitForFlowNodeInFailedState(processInstance, "step");
            // then we must have context for exception, even if it was logged outside of the initial throwing scope
            checkLogEntryContains(Map.of(MDCConstants.PROCESS_INSTANCE_ID, Long.toString(processInstance.getId()),
                    MDCConstants.PROCESS_DEFINITION_ID, Long.toString(processDefinition.getId()),
                    MDCConstants.ROOT_PROCESS_INSTANCE_ID, Long.toString(processInstance.getRootProcessInstanceId()),
                    MDCConstants.FLOW_NODE_INSTANCE_ID, Long.toString(failedTask.getId())));

        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    /**
     * Check there is a line in the log which contain context variables
     *
     * @param context context variables to check
     */
    private void checkLogEntryContains(Map<String, String> context) {
        var log = systemOutRule.getLogWithNormalizedLineSeparator();
        LogITUtil.checkLogEntryContains(log, context);
    }

}
