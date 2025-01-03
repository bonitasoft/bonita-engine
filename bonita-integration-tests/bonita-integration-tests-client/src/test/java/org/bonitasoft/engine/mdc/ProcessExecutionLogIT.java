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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.impl.ProcessAPIImpl;
import org.bonitasoft.engine.api.impl.ProcessManagementAPIImplDelegate;
import org.bonitasoft.engine.api.impl.ProcessStarter;
import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.api.impl.transaction.process.DisableProcess;
import org.bonitasoft.engine.api.impl.transaction.process.EnableProcess;
import org.bonitasoft.engine.bar.BusinessArchiveServiceImpl;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.classloader.ClassLoaderServiceImpl;
import org.bonitasoft.engine.execution.ProcessExecutorImpl;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.slf4j.LoggerFactory;

public class ProcessExecutionLogIT extends CommonAPIIT {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @After
    public void afterTest() throws Exception {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @Test
    public void inspectTransactionIdInLogs() throws Exception {
        var txIdPattern = Pattern.compile(" " + MDCConstants.TRANSACTION_ID + "=([0-9a-f:]+)");
        Function<String, Optional<String>> getTxId = l -> {
            var matcher = txIdPattern.matcher(l);
            return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
        };

        BiConsumer<String, Map<String, Integer>> checkLogHasTxIds = (log, classPatternsWithSameTx) -> {
            var classesToFind = new HashMap<String, Integer>(classPatternsWithSameTx);
            var txIdFoundByClass = new HashMap<String, String>(classPatternsWithSameTx.size());
            var lines = log.split("\n\\|");
            for (var l : lines) {
                var classFound = classesToFind.keySet().stream().filter(c -> {
                    var linePattern = String.format("(?s).*(%s)\\|.*", c);
                    return l.matches(linePattern);
                }).findAny();
                classFound.ifPresent(c -> {
                    // should have a tx id
                    var txId = getTxId.apply(l);
                    assertThat(txId).as("Log line %s should contain %s", l, MDCConstants.TRANSACTION_ID).isPresent();

                    var toFind = classesToFind.get(c);
                    assertThat(toFind).as("Log line %s was found, but we expected only %d lines for class pattern %s",
                            l, classPatternsWithSameTx.get(c), c)
                            .isGreaterThan(0);
                    classesToFind.put(c, toFind - 1);

                    var previousTxId = txIdFoundByClass.put(c, txId.get());
                    if (previousTxId != null) {
                        assertThat(previousTxId)
                                .as("Transaction id should be consistent for class pattern %s. It has changed in line %s",
                                        c, l)
                                .isEqualTo(txId.get());
                    }
                });
                // there may be other log lines with or without transaction id...
            }

            assertThat(classesToFind).allSatisfy((c, i) -> assertThat(i)
                    .as("We expected %2$d log lines for class pattern %1$s but found only %3$d.",
                            c, classPatternsWithSameTx.get(c), classPatternsWithSameTx.get(c) - i)
                    .isEqualTo(0));
        };

        var designProcessDef = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(List.of("H1"),
                List.of(true));
        var user = createUser(USERNAME, PASSWORD);
        // when
        systemOutRule.clearLog();
        var processDef = deployAndEnableProcessWithActor(designProcessDef, BuildTestUtil.ACTOR_NAME, user);

        // then, check logs have transaction id for the given classes
        var businessArchivePattern = BusinessArchiveArtifactsManager.class.getSimpleName() + "|"
                + BusinessArchiveServiceImpl.class.getSimpleName();
        checkLogHasTxIds.accept(systemOutRule.getLogWithNormalizedLineSeparator(),
                Map.of(businessArchivePattern, 2,
                        ClassLoaderServiceImpl.class.getSimpleName(), 2,
                        EnableProcess.class.getSimpleName(), 1));

        long userId = user.getId();
        try {
            // when
            systemOutRule.clearLog();
            getProcessAPI().startProcess(userId, processDef.getId());

            // then consecutive logs contain the same transaction id or a unique new one
            checkLogHasTxIds.accept(systemOutRule.getLogWithNormalizedLineSeparator(),
                    Map.of(ProcessStarter.class.getSimpleName(), 1));
        } finally {
            // when
            systemOutRule.clearLog();
            disableAndDeleteProcess(processDef);
            // then there should be a transaction id once again
            checkLogHasTxIds.accept(systemOutRule.getLogWithNormalizedLineSeparator(),
                    Map.of(DisableProcess.class.getSimpleName(), 1,
                            ProcessManagementAPIImplDelegate.class.getSimpleName(), 1));
        }

    }

    @Test
    public void failProcessWithExceptionAndInspectLogs() throws Exception {
        var designProcessDef = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(List.of("S1", "H1"),
                List.of(false, true));
        ((DesignProcessDefinitionImpl) designProcessDef).setStringIndex(1, "i1",
                new ExpressionBuilder().createConstantStringExpression("a"));
        for (var act : designProcessDef.getFlowElementContainer().getActivities()) {
            switch (act.getName()) {
                case "S1":
                    // make S1 fail explicitly
                    var script = "throw new Exception(\"Expected fail=OK\")";
                    Expression lastingExpr = new ExpressionBuilder().createGroovyScriptExpression("script", script,
                            String.class.getName());
                    Operation operation = new OperationBuilder().createSetStringIndexOperation(1, lastingExpr);
                    act.getOperations().add(operation);
                    break;
                default:
                    break;
            }
        }
        var user = createUser(USERNAME, PASSWORD);
        var processDef = deployAndEnableProcessWithActor(designProcessDef, BuildTestUtil.ACTOR_NAME, user);
        long userId = user.getId();
        try {
            // when
            systemOutRule.clearLog();
            var processInstance = getProcessAPI().startProcess(userId, processDef.getId());
            var s1 = waitForFlowNodeInFailedState(processInstance, "S1");
            // then we must have context for exception, even if it was logged outside of the initial throwing scope
            checkLogEntryContains(Map.of(MDCConstants.PROCESS_INSTANCE_ID, Long.toString(processInstance.getId()),
                    MDCConstants.PROCESS_DEFINITION_ID, Long.toString(processDef.getId()),
                    MDCConstants.FLOW_NODE_INSTANCE_ID, Long.toString(s1.getId()),
                    "Expected fail", "OK"));

        } finally {
            disableAndDeleteProcess(processDef);
        }

    }

    @Test
    public void executeProcessAndInspectLogs() throws Exception {
        var designProcessDef = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(List.of("H1", "S2", "H3"),
                List.of(true, false, true));
        ((DesignProcessDefinitionImpl) designProcessDef).setStringIndex(1, "i1",
                new ExpressionBuilder().createConstantStringExpression("a"));
        for (var act : designProcessDef.getFlowElementContainer().getActivities()) {
            switch (act.getName()) {
                case "S2":
                    // make S2 last a bit
                    var script = """
                            Thread.sleep(500)
                            return 'b'
                            """;
                    Expression lastingExpr = new ExpressionBuilder().createGroovyScriptExpression("script", script,
                            String.class.getName());
                    Operation operation = new OperationBuilder().createSetStringIndexOperation(1, lastingExpr);
                    act.getOperations().add(operation);
                    break;
                default:
                    break;
            }
        }
        var user = createUser(USERNAME, PASSWORD);
        var processDef = deployAndEnableProcessWithActor(designProcessDef, BuildTestUtil.ACTOR_NAME, user);
        long userId = user.getId();
        long substituteId = getSession().getUserId();

        // set debug level to get execution logs on service tasks and process end
        final Logger exeLogger = (Logger) LoggerFactory.getLogger(ProcessExecutorImpl.class);
        Level oldExeLogLevel = exeLogger.getLevel();
        final Logger apiLogger = (Logger) LoggerFactory.getLogger(ProcessAPIImpl.class);
        Level oldApiLogLevel = apiLogger.getLevel();
        try {
            exeLogger.setLevel(Level.DEBUG);
            apiLogger.setLevel(Level.INFO);

            // when
            systemOutRule.clearLog();
            var processInstance = getProcessAPI().startProcess(userId, processDef.getId());
            // then
            checkLogEntryContains(Map.of(MDCConstants.PROCESS_INSTANCE_ID, Long.toString(processInstance.getId()),
                    MDCConstants.PROCESS_DEFINITION_ID, Long.toString(processDef.getId()),
                    MDCConstants.USER_ID, Long.toString(userId),
                    MDCConstants.SUBSTITUTE_USER_ID, Long.toString(substituteId)));

            // when
            var h1 = waitForUserTaskAndGetIt(processInstance, "H1");
            systemOutRule.clearLog();
            getProcessAPI().assignAndExecuteUserTask(userId, h1.getId(), Collections.emptyMap());
            // then
            checkLogEntryContains(Map.of(MDCConstants.PROCESS_INSTANCE_ID, Long.toString(processInstance.getId()),
                    MDCConstants.PROCESS_DEFINITION_ID, Long.toString(processDef.getId()),
                    MDCConstants.FLOW_NODE_INSTANCE_ID, Long.toString(h1.getId()),
                    MDCConstants.USER_ID, Long.toString(userId),
                    MDCConstants.SUBSTITUTE_USER_ID, Long.toString(substituteId)));

            // when
            systemOutRule.clearLog();
            // give S2 the required time to process
            var h3 = waitForUserTaskAndGetIt(processInstance, "H3");
            var s2Id = waitForFlowNodeInState(processInstance, "S2", TestStates.NORMAL_FINAL, false);
            // then
            checkLogEntryContains(Map.of(MDCConstants.PROCESS_INSTANCE_ID, Long.toString(processInstance.getId()),
                    MDCConstants.PROCESS_DEFINITION_ID, Long.toString(processDef.getId()),
                    MDCConstants.FLOW_NODE_INSTANCE_ID, Long.toString(s2Id)));

            //when
            systemOutRule.clearLog();
            getProcessAPI().assignAndExecuteUserTask(userId, h3.getId(), Collections.emptyMap());
            // then
            checkLogEntryContains(Map.of(MDCConstants.PROCESS_INSTANCE_ID, Long.toString(processInstance.getId()),
                    MDCConstants.PROCESS_DEFINITION_ID, Long.toString(processDef.getId()),
                    MDCConstants.FLOW_NODE_INSTANCE_ID, Long.toString(h3.getId()),
                    MDCConstants.USER_ID, Long.toString(userId),
                    MDCConstants.SUBSTITUTE_USER_ID, Long.toString(substituteId)));

            // when
            waitForProcessToFinish(processInstance);
            // then
            checkLogEntryContains(Map.of(MDCConstants.PROCESS_INSTANCE_ID, Long.toString(processInstance.getId()),
                    MDCConstants.PROCESS_DEFINITION_ID, Long.toString(processDef.getId())),
                    // distinguish process end from human task...
                    Collections.singletonList(MDCConstants.FLOW_NODE_INSTANCE_ID));

        } finally {
            disableAndDeleteProcess(processDef);
            // restore old log level
            exeLogger.setLevel(oldExeLogLevel);
            apiLogger.setLevel(oldApiLogLevel);
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

    /**
     * Check there is a line in the log which contain context variables
     *
     * @param context context variables to check
     * @param forbiddenKeys keys that must not appear in the context
     */
    private void checkLogEntryContains(Map<String, String> context, Collection<String> forbiddenKeys) {
        var log = systemOutRule.getLogWithNormalizedLineSeparator();
        LogITUtil.checkLogEntryContains(log, context, forbiddenKeys);
    }

}
