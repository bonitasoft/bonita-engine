/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.junit.Test;

public class ProcessDescriptionIT extends TestWithUser {

    @Test
    public void allInstanceDescriptions() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("descProcess", "1.0");
        processBuilder.addDescription("processDescription");
        processBuilder.addActor(ACTOR_NAME).addDescription("actorDescription");
        processBuilder.addBooleanData("booleanProcessData", null).addDescription("descBooleanProcessData");
        processBuilder.addStartEvent("start");
        processBuilder.addGateway("gateway", GatewayType.PARALLEL).addDescription("descGateway");
        processBuilder.addUserTask("userTask", ACTOR_NAME).addDescription("descUserTask").addBooleanData("booleanUserTaskData", null)
                .addDescription("descBooleanUserTaskData");
        processBuilder.addTransition("start", "gateway");
        processBuilder.addTransition("gateway", "userTask");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals("processDescription", processInstance.getDescription());

        final DataInstance processDataInstance = getProcessAPI().getProcessDataInstance("booleanProcessData", processInstance.getId());
        assertEquals("descBooleanProcessData", processDataInstance.getDescription());

        final long userTaskId = waitForUserTask(processInstance, "userTask");
        final DataInstance activityDataInstance = getProcessAPI().getActivityDataInstance("booleanUserTaskData", userTaskId);
        assertEquals("descBooleanUserTaskData", activityDataInstance.getDescription());

        disableAndDeleteProcess(processDefinition);
    }

}
