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
 */

package org.bonitasoft.engine.form;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.V6FormDeployException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.page.AuthorizationRuleConstants;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageURL;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Baptiste Mesta
 */
public class FormMappingIT extends TestWithUser {

    private final Map<String, Serializable> context = Collections.singletonMap(AuthorizationRuleConstants.IS_ADMIN, (Serializable) true);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void deployProcessesWithFormMappings() throws Exception {
        ProcessDefinitionBuilder p1Builder = new ProcessDefinitionBuilder().createNewInstance("P1", "1.0");
        p1Builder.addUserTask("step1", "actor").addUserTask("step2", "actor");
        p1Builder.addActor("actor");
        BusinessArchiveBuilder bar1 = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(p1Builder.done())
                .setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("processStartForm", FormMappingTarget.URL)
                                .addTaskForm("task1Form", FormMappingTarget.INTERNAL, "step1")
                                .addProcessOverviewForm("process1OverviewForm", FormMappingTarget.INTERNAL).build());

        ProcessDefinitionBuilder p2Builder = new ProcessDefinitionBuilder().createNewInstance("P2", "1.0");
        p2Builder.addUserTask("step1", "actor").addUserTask("step2", "actor");
        p2Builder.addActor("actor");
        BusinessArchiveBuilder bar2 = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(p2Builder.done())
                .setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("processStartForm", FormMappingTarget.URL)
                                .addTaskForm("task2Form", FormMappingTarget.URL, "step1")
                                .addProcessOverviewForm(null, FormMappingTarget.LEGACY).build());

        ProcessDefinition p1 = getProcessAPI().deploy(bar1.done());

        ProcessAPI processConfigurationAPI = getProcessAPI();

        //get
        FormMapping processStartForm1 = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.PROCESS_START).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p1.getId())
                .done()).getResult().get(0);
        FormMapping processOverviewForm1 = processConfigurationAPI
                .searchFormMappings(
                        new SearchOptionsBuilder(0, 10)
                                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.PROCESS_OVERVIEW)
                                .filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p1.getId())
                                .done()).getResult().get(0);
        FormMapping step1Form1 = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.TASK).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p1.getId())
                .filter(FormMappingSearchDescriptor.TASK, "step1").done()).getResult().get(0);
        FormMapping step2Form1 = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.TASK).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p1.getId())
                .filter(FormMappingSearchDescriptor.TASK, "step2").done()).getResult().get(0);

        assertThat(processStartForm1.getProcessDefinitionId()).isEqualTo(p1.getId());
        assertThat(processStartForm1.getPageId()).isNull();
        assertThat(processStartForm1.getURL()).isEqualTo("processStartForm");
        assertThat(processStartForm1.getTarget()).isEqualTo(FormMappingTarget.URL);
        assertThat(processOverviewForm1.getProcessDefinitionId()).isEqualTo(p1.getId());
        assertThat(processOverviewForm1.getPageId()).isNull();
        assertThat(processOverviewForm1.getURL()).isNull();
        assertThat(processOverviewForm1.getTarget()).isEqualTo(FormMappingTarget.INTERNAL); // referenced page does not exists
        assertThat(step1Form1.getProcessDefinitionId()).isEqualTo(p1.getId());
        assertThat(step1Form1.getPageId()).isNull();
        assertThat(step2Form1.getProcessDefinitionId()).isEqualTo(p1.getId());
        assertThat(step2Form1.getURL()).isNull();
        assertThat(step2Form1.getTarget()).isEqualTo(FormMappingTarget.NONE);

        //search
        SearchResult<FormMapping> formMappingSearchResult = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(
                FormMappingSearchDescriptor.ID, Order.DESC).done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(4);
        assertThat(formMappingSearchResult.getResult()).extracting("processDefinitionId").containsExactly(
                p1.getId(), p1.getId(), p1.getId(), p1.getId());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(4);
        formMappingSearchResult = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC)
                .filter(FormMappingSearchDescriptor.TASK, "step1").done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(1);
        formMappingSearchResult = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.PROCESS_START).done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(1);

        //resolve urls:
        PageURL p1Instanciation = getPageAPI().resolvePageOrURL("process/P1/1.0", context, true);
        PageURL p1Overview = getPageAPI().resolvePageOrURL("processInstance/P1/1.0", context, true);
        PageURL p1step1Instanciation = getPageAPI().resolvePageOrURL("taskInstance/P1/1.0/step1", context, true);
        assertThat(p1Instanciation.getUrl()).isEqualTo("processStartForm");
        assertThat(p1Overview.getPageId()).isNull();
        assertThat(p1step1Instanciation.getUrl()).isEqualTo(null);

        getProcessAPI().deleteProcessDefinition(p1.getId());
        assertThat(
                processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC).done())
                        .getResult()).isEmpty();

    }

    @Test
    public void deployProcessesWithV6FormMappingsFails() throws Exception {
        // given:
        ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("P2", "1.0");
        processBuilder.addUserTask("step1", "actor").addUserTask("step2", "actor");
        BusinessArchiveBuilder bar = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(processBuilder.done())
                .setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("processStartForm", FormMappingTarget.URL)
                                .addTaskForm("task2Form", FormMappingTarget.URL, "step1")
                                .addProcessOverviewForm(null, FormMappingTarget.LEGACY).build());

        // then:
        expectedException.expect(V6FormDeployException.class);
        expectedException.expectMessage("The process contains v6 forms");

        // when:
        getProcessAPI().deploy(bar.done());
    }

    @Test
    public void resolvePageOrURLThrowsNotFoundExceptionForUndefinedFormMapping() throws Exception {
        ProcessDefinitionBuilder p1Builder = new ProcessDefinitionBuilder().createNewInstance("CustomerSupport", "1.0");
        p1Builder.addActor("actor").addUserTask("step", "actor");
        BusinessArchiveBuilder bar = new BusinessArchiveBuilder()
                .createNewBusinessArchive().setProcessDefinition(p1Builder.done())
                .setFormMappings(FormMappingModelBuilder.buildFormMappingModel().addTaskForm(null, FormMappingTarget.UNDEFINED, "step").build());

        ProcessDefinition processDefinition = deployProcess(bar.done());

        expectedException.expect(NotFoundException.class);

        // try to resolve url:
        try {
            getPageAPI().resolvePageOrURL("taskInstance/CustomerSupport/1.0/step", Collections.<String, Serializable>emptyMap(), true);
        } finally {
            deleteProcess(processDefinition);
        }
    }

    @Test
    public void resolvePageOrURL_should_return_null_mapping_for_NONE() throws Exception {
        ProcessDefinitionBuilder p1Builder = new ProcessDefinitionBuilder().createNewInstance("CustomerSupport", "1.0");
        p1Builder.addActor("actor").addUserTask("step", "actor");
        BusinessArchiveBuilder bar = new BusinessArchiveBuilder()
                .createNewBusinessArchive().setProcessDefinition(p1Builder.done())
                .setFormMappings(FormMappingModelBuilder.buildFormMappingModel().addTaskForm(null, FormMappingTarget.NONE, "step").build());

        ProcessDefinition processDefinition = deployProcess(bar.done());


        // try to resolve url:
        PageURL pageURL = getPageAPI().resolvePageOrURL("taskInstance/CustomerSupport/1.0/step", context, true);

        assertThat(pageURL.getPageId()).isNull();
        assertThat(pageURL.getUrl()).isNull();


        deleteProcess(processDefinition);
    }

    @Test
    public void deployProcessWithInternalPagesIncludedShouldBeResolved() throws Exception {
        Page custompage_globalpage = getPageAPI().createPage("globalPage.zip", createTestPageContent("custompage_globalpage", "Global page", "a global page"));
        ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("CustomerSupport", "1.12");
        final String custompage_startProcessForm = "custompage_startProcessForm";
        BusinessArchiveBuilder bar = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(processBuilder.done())
                .setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("custompage_startProcessForm", FormMappingTarget.INTERNAL)
                                .addProcessOverviewForm("custompage_globalpage", FormMappingTarget.INTERNAL).build())
                .addExternalResource(
                        new BarResource("customPages/custompage_startProcessForm.zip", createTestPageContent(custompage_startProcessForm, "kikoo", "LOL")));

        final ProcessDefinition processDefinition = deployProcess(bar.done());
        final Page page = getPageAPI().getPageByNameAndProcessDefinitionId(custompage_startProcessForm, processDefinition.getId());
        assertThat(page.getId()).isNotNull();
        assertThat(getProcessAPI().getProcessResolutionProblems(processDefinition.getId())).isEmpty();
        getProcessAPI().enableProcess(processDefinition.getId());

        // Should not throw Exception
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertThat(processDeploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.RESOLVED);

        final PageURL pageURLStart = getPageAPI().resolvePageOrURL("process/CustomerSupport/1.12", context, true);
        final PageURL pageURLOverview = getPageAPI().resolvePageOrURL("processInstance/CustomerSupport/1.12", context, true);
        assertThat(pageURLStart.getPageId()).isNotNull();
        assertThat(page.getId()).isEqualTo(pageURLStart.getPageId());
        assertThat(pageURLOverview.getPageId()).isNotNull();
        assertThat(custompage_globalpage.getId()).isEqualTo(pageURLOverview.getPageId());

        getPageAPI().deletePage(page.getId());
        assertThat(getProcessAPI().getProcessResolutionProblems(processDefinition.getId())).hasSize(1);

        getPageAPI().deletePage(custompage_globalpage.getId());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void processWithEventSubProcess() throws Exception {
        ProcessDefinitionBuilder p1Builder = new ProcessDefinitionBuilder().createNewInstance("P1", "1.0");
        p1Builder.addUserTask("step1", "actor").addUserTask("step2", "actor");
        p1Builder.addActor("actor");
        final SubProcessDefinitionBuilder eventSubProc = p1Builder.addSubProcess("eventSubProc", true).getSubProcessBuilder();
        eventSubProc.addUserTask("subTask", "actor");
        eventSubProc.addStartEvent("start").addSignalEventTrigger("theSignal");
        eventSubProc.addTransition("start", "subTask");

        BusinessArchiveBuilder bar1 = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(p1Builder.done())
                .setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("processStartForm", FormMappingTarget.URL)
                                .addTaskForm("task1Form", FormMappingTarget.INTERNAL, "step1")
                                .addTaskForm("urlForThesubTask", FormMappingTarget.URL, "subTask")
                                .addProcessOverviewForm("process1OverviewForm", FormMappingTarget.INTERNAL).build());


        ProcessDefinition p1 = getProcessAPI().deploy(bar1.done());

        SearchResult<FormMapping> formMappingSearchResult = getProcessAPI().searchFormMappings(new SearchOptionsBuilder(0, 100).sort(
                FormMappingSearchDescriptor.ID, Order.DESC).filter(FormMappingSearchDescriptor.TASK, "subTask").done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(1);
        assertThat(formMappingSearchResult.getResult().get(0).getURL()).isEqualTo("urlForThesubTask");

        deleteProcess(p1);

    }

}
