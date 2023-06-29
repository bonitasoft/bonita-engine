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
package org.bonitasoft.engine.bpm.process.impl.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.junit.Test;

public class DesignProcessDefinitionImplTest {

    private static final String ACTOR_NAME = "member";

    /*
     * The process design is used inside the "process more details" > "Scripts" page to list the groovy expression that
     * can edited. So we need to ensure that the JSON serialization works
     */
    @Test
    public void should_not_throw_exception_when_serializing_process_definition_design() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("descProcess",
                "1.0");
        processBuilder.addDescription("processDescription");
        processBuilder.addActor(ACTOR_NAME).addDescription("actorDescription");
        processBuilder.addBooleanData("booleanProcessData", null).addDescription("descBooleanProcessData");
        processBuilder.addStartEvent("start");
        processBuilder.addGateway("gateway", GatewayType.PARALLEL).addDescription("descGateway");
        processBuilder.addUserTask("userTask", ACTOR_NAME).addDescription("descUserTask")
                .addBooleanData("booleanUserTaskData", null)
                .addDescription("descBooleanUserTaskData");
        processBuilder.addTransition("start", "gateway");
        processBuilder.addTransition("gateway", "userTask");
        om.writeValueAsString(processBuilder);
    }

}
