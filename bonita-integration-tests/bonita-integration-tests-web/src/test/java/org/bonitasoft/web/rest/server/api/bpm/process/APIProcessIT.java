/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Test;

public class APIProcessIT extends AbstractConsoleTest {

    private APIProcess apiProcess;

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Override
    public void consoleTestSetUp() throws Exception {
        apiProcess = new APIProcess();
        apiProcess.setCaller(getAPICaller(TestUserFactory.getJohnCarpenter().getSession(), "API/bpm/process"));

    }

    /**
     * Add a process uploaded
     *
     * @throws Exception
     */
    @Test
    public void testAddProcessItem() throws Exception {
        final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(TestUserFactory.getJohnCarpenter().getSession());
        final List<ProcessDeploymentInfo> before = processAPI.getProcessDeploymentInfos(0, 10,
                ProcessDeploymentInfoCriterion.DEFAULT);

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("Test process", "1.0").done())
                .done();
        final String fileKey = writeBarToUploads("addProcessTest", businessArchive);

        // use api to deploy process uploaded
        final ProcessItem item = new ProcessItem();
        item.setAttribute("fileupload", fileKey);
        apiProcess.add(item);

        // check the process has been correctly uploaded
        String assertMessage = "Can't add a ProcessItem to APIProcess. ";
        int actualSize = -1;
        final List<ProcessDeploymentInfo> processDeploymentInfos = processAPI.getProcessDeploymentInfos(0, 10,
                ProcessDeploymentInfoCriterion.DEFAULT);
        if (processDeploymentInfos != null) {
            actualSize = processDeploymentInfos.size();
            for (ProcessDeploymentInfo processDeploymentInfo : processDeploymentInfos) {
                assertMessage += "\nprocessDeploymentInfo=" + processDeploymentInfo;
            }
        } else {
            assertMessage += "processDeploymentInfos is null.";
        }
        assertEquals(assertMessage, 1, actualSize - before.size());
    }

    /**
     * Update state of an enabled process to disabled
     *
     * @throws Exception
     */
    @Test
    public void testUpdateProcessItem() throws Exception {
        final APIID processDefinitionId = APIID.makeAPIID(TestProcessFactory.getDefaultHumanTaskProcess()
                .addActor(getInitiator())
                .enable()
                .getId());

        // assert process is well enabled
        final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(TestUserFactory.getJohnCarpenter().getSession());
        final String expectedState = processAPI.getProcessDeploymentInfos(0, 1, ProcessDeploymentInfoCriterion.DEFAULT)
                .get(0).getActivationState().name();
        assertEquals("Process should start enabled", ProcessItem.VALUE_ACTIVATION_STATE_ENABLED, expectedState);

        // use process api to update the state
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(ProcessItem.ATTRIBUTE_ACTIVATION_STATE, ProcessItem.VALUE_ACTIVATION_STATE_DISABLED);
        final ProcessItem processItem = apiProcess.update(processDefinitionId, attributes);

        // check the process is disabled (resolved)
        assertEquals(
                "Can't update a processItem with APIProcess <" + processItem.getActivationState() + " - "
                        + ProcessItem.VALUE_ACTIVATION_STATE_DISABLED
                        + ">",
                processItem.getActivationState(), ProcessItem.VALUE_ACTIVATION_STATE_DISABLED);
    }

    /**
     * Get a process
     *
     * @throws Exception
     */
    @Test
    public void testGetProcessItem() throws Exception {
        final APIID processDefinitionId = APIID.makeAPIID(TestProcessFactory.getDefaultHumanTaskProcess()
                .addActor(getInitiator())
                .getId());

        final ArrayList<String> deploys = new ArrayList<>();
        final ArrayList<String> counters = new ArrayList<>();

        assertEquals("Can't get a processItem with APIProcess",
                apiProcess.runGet(processDefinitionId, deploys, counters).getName(),
                TestProcessFactory.getDefaultHumanTaskProcess().getProcessDefinition().getName());
        assertEquals("Can't get a processItem with APIProcess",
                apiProcess.runGet(processDefinitionId, deploys, counters).getDescription(),
                TestProcessFactory.getDefaultHumanTaskProcess().getProcessDefinition().getDescription());
    }

    /**
     * Search process by its id
     *
     * @throws Exception
     */
    @Test
    public void testSearchProcessItemForUser() throws Exception {
        final APIID processDefinitionId = APIID.makeAPIID(TestProcessFactory.getDefaultHumanTaskProcess()
                .addActor(getInitiator())
                .enable()
                .getId());

        // Set the filters
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ProcessItem.FILTER_USER_ID, String.valueOf(TestUserFactory.getJohnCarpenter().getId()));

        // Search the ProcessItem
        final ArrayList<String> deploys = new ArrayList<>();
        final ArrayList<String> counters = new ArrayList<>();
        final ProcessItem processItem = apiProcess
                .runSearch(0, 10, null, ProcessItem.ATTRIBUTE_DISPLAY_NAME + " ASC", filters, deploys, counters)
                .getResults().get(0);
        assertEquals(
                "Can't search a processItem with APIProcess <" + processDefinitionId + " - "
                        + processItem.getId().toLong() + ">",
                processDefinitionId,
                processItem.getId().toLong());
    }

    /*
     * Create a temporary file, contain a businessArchive
     * @return File key from temporary content
     * @param String
     * prefix path for the temporary file
     * @param BusinessArchive
     * businessArchive write in the temporary file
     */
    private static String writeBarToUploads(final String barName, final BusinessArchive businessArchive) {
        String fileKey = null;
        try {
            File tempFile = File.createTempFile(barName, ".bar",
                    WebBonitaConstantsUtils.getTenantInstance().getTempFolder());
            tempFile.delete();
            BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive, tempFile);

            // write into database
            fileKey = PlatformAPIAccessor.getTemporaryContentAPI()
                    .storeTempFile(new FileContent("thisismynewfile.doc", new FileInputStream(tempFile),
                            "application/octet-stream"));

        } catch (final IOException | BonitaHomeNotSetException | ServerAPIException | UnknownAPITypeException e) {
            e.printStackTrace();
        }
        return fileKey;
    }

    /**
     * Get the latest process version
     *
     * @throws Exception
     */
    @Test
    public void testGetLastProcessVersion() throws Exception {
        // create 3 version of a process
        final TestProcess p1 = new TestProcess(
                TestProcessFactory.getDefaultProcessDefinitionBuilder("multipleVersionsProcess", "aVersion"));
        TestProcessFactory.getInstance().add(p1);
        final TestProcess p2 = new TestProcess(
                TestProcessFactory.getDefaultProcessDefinitionBuilder("multipleVersionsProcess", "aVersion2"));
        TestProcessFactory.getInstance().add(p2);
        final TestProcess p3 = new TestProcess(
                TestProcessFactory.getDefaultProcessDefinitionBuilder("multipleVersionsProcess", "anOtherVersion"));
        TestProcessFactory.getInstance().add(p3);

        // map actor John Carpenter on the created processes, then set enable
        p1.addActor(TestUserFactory.getJohnCarpenter()).enable();
        p2.addActor(TestUserFactory.getJohnCarpenter()).enable();
        p3.addActor(TestUserFactory.getJohnCarpenter()).enable();

        // Set the filters
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ProcessItem.FILTER_USER_ID, String.valueOf(TestUserFactory.getJohnCarpenter().getId()));
        filters.put(ProcessItem.ATTRIBUTE_DISPLAY_NAME, "multipleVersionsProcess");

        // search the last version of a process
        final List<ProcessItem> resultList = apiProcess
                .runSearch(0, 1, null, ProcessItem.ATTRIBUTE_DEPLOYMENT_DATE + " DESC", filters, null, null)
                .getResults();

        // get the first element
        final ProcessItem searchedProcessItem = resultList.get(0);
        assertEquals("multipleVersionsProcess", searchedProcessItem.getDisplayName());
        assertEquals("anOtherVersion", searchedProcessItem.getVersion());

        //Because TestProcessFactory is based on names, at least 2 out of the three above processes should be cleaned manually.
        // This could be improved later in TestProcessFactory
        TestProcessFactory.getInstance().delete(p1);
        TestProcessFactory.getInstance().delete(p2);
        TestProcessFactory.getInstance().delete(p3);
    }

}
