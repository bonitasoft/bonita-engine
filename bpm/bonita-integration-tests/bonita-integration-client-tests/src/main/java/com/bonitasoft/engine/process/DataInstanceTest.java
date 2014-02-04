package com.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;

/**
 * 
 * @author Baptiste Mesta
 * 
 */
public class DataInstanceTest extends CommonAPISPTest {

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @Test
    public void should_process_have_no_data_mapping_when_finished() throws Exception {
        ProcessDefinition processDefinition = deployAndEnableProcess(new ProcessDefinitionBuilder().createNewInstance("Arrival of a new employee", "1.0")
                .addShortTextData("name", new ExpressionBuilder().createConstantStringExpression("John"))
                .addStartEvent("start")
                .addAutomaticTask("step1")
                .addIntermediateThrowEvent("signal1").addSignalEventTrigger("mySignal1")
                .addIntermediateCatchEvent("signal2").addSignalEventTrigger("mySignal2")
                .addEndEvent("end")
                .addTransition("start", "step1")
                .addTransition("step1", "signal1")
                .addTransition("signal1", "signal2")
                .addTransition("signal2", "end").getProcess());

        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        // no better solutions to check if there is still some visibility mapping
        String count = getReportingAPI().selectList("SELECT count(*) FROM data_mapping").split("\n")[1];
        assertTrue("no visibility mapping during execution of the process", Integer.valueOf(count) > 0);

        waitForFlowNodeInState(processInstance, "signal2", "waiting", false);
        getProcessAPI().sendSignal("mySignal2");
        waitForProcessToFinish(processInstance);

        count = getReportingAPI().selectList("SELECT count(*) FROM data_mapping").split("\n")[1];
        assertEquals("there should not be any visibility mapping at this point, in db: " + getReportingAPI().selectList("SELECT * FROM data_mapping"),
                new Integer(0), Integer.valueOf(count));

        disableAndDeleteProcess(processDefinition);
    }
}
