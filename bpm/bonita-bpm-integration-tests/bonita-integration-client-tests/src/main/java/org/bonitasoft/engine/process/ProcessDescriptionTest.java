package org.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcessDescriptionTest extends CommonAPITest {

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
         loginOnDefaultTenantWithDefaultTechnicalLogger();
    }

    @Test
    public void testAllInstanceDescriptions() throws Exception {
        final User user = createUser("Jani", "kuulu");
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

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(getBusinessArchive(processBuilder), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals("processDescription", processInstance.getDescription());

        final DataInstance processDataInstance = getProcessAPI().getProcessDataInstance("booleanProcessData", processInstance.getId());
        assertEquals("descBooleanProcessData", processDataInstance.getDescription());

        final ActivityInstance userTask = waitForUserTask("userTask", processInstance);
        final DataInstance activityDataInstance = getProcessAPI().getActivityDataInstance("booleanUserTaskData", userTask.getId());
        assertEquals("descBooleanUserTaskData", activityDataInstance.getDescription());

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    private BusinessArchive getBusinessArchive(final ProcessDefinitionBuilder builder) throws Exception {
        final File barFile = File.createTempFile("businessArchive", ".bar");
        barFile.delete();
        final DesignProcessDefinition process = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(process).done();
        BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, barFile);
        final InputStream inputStream = new FileInputStream(barFile);
        try {
            return BusinessArchiveFactory.readBusinessArchive(inputStream);
        } finally {
            inputStream.close();
        }
    }

}
