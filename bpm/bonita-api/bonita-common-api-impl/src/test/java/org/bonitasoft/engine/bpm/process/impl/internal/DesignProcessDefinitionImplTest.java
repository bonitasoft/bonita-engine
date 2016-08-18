package org.bonitasoft.engine.bpm.process.impl.internal;

import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DesignProcessDefinitionImplTest {

    private static final String ACTOR_NAME = "member";

    /*
     * The process design is used inside the "process more details" > "Scripts" page to list the groovy expression that
     * can edited. So we need to ensure that the JSON serialization works
     */
    @Test
    public void should_not_throw_exception_when_serializing_process_definition_design() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("descProcess", "1.0");
        processBuilder.addDescription("processDescription");
        processBuilder.addActor(ACTOR_NAME).addDescription("actorDescription");
        processBuilder.addBooleanData("booleanProcessData", null).addDescription("descBooleanProcessData");
        processBuilder.addStartEvent("start");
        processBuilder.addGateway("gateway", GatewayType.PARALLEL).addDescription("descGateway");
        processBuilder.addUserTask("userTask", ACTOR_NAME).addDescription("descUserTask").addBooleanData("booleanUserTaskData", null)
                .addDescription("descBooleanUserTaskData");
        processBuilder.addTransition("start", "gateway");
        processBuilder.addTransition("gateway", "userTask");
        om.writeValueAsString(processBuilder);
    }

}
