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
import org.bonitasoft.engine.api.ProcessConfigurationAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
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
        ProcessDefinition p2 = getProcessAPI().deploy(bar2.done());

        ProcessConfigurationAPI processConfigurationAPI = getProcessConfigurationAPI();

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
        FormMapping processStartForm2 = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.PROCESS_START).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p2.getId())
                .done()).getResult().get(0);
        FormMapping processOverviewForm2 = processConfigurationAPI.searchFormMappings(
                new SearchOptionsBuilder(0, 10)
                        .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.PROCESS_OVERVIEW)
                        .filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p2.getId())
                        .done()).getResult().get(0);
        FormMapping step1Form2 = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.TASK).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p2.getId())
                .filter(FormMappingSearchDescriptor.TASK, "step1").done()).getResult().get(0);
        FormMapping step2Form2 = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 10)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.TASK).filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p2.getId())
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
        assertThat(step2Form1.getTarget()).isEqualTo(FormMappingTarget.UNDEFINED);

        assertThat(processStartForm2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(processStartForm2.getURL()).isEqualTo("processStartForm");
        assertThat(processOverviewForm2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(processOverviewForm2.getURL()).isNull();
        assertThat(processOverviewForm2.getTarget()).isEqualTo(FormMappingTarget.LEGACY);
        assertThat(step1Form2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(step1Form2.getURL()).isEqualTo("task2Form");
        assertThat(step2Form2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(step2Form2.getURL()).isNull();
        assertThat(step2Form2.getTarget()).isEqualTo(FormMappingTarget.UNDEFINED);

        //search
        SearchResult<FormMapping> formMappingSearchResult = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(
                FormMappingSearchDescriptor.ID, Order.DESC).done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(8);
        assertThat(formMappingSearchResult.getResult()).extracting("processDefinitionId").containsExactly(p2.getId(), p2.getId(), p2.getId(), p2.getId(),
                p1.getId(), p1.getId(), p1.getId(), p1.getId());
        formMappingSearchResult = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC)
                .filter(FormMappingSearchDescriptor.PROCESS_DEFINITION_ID, p2.getId()).done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(4);
        assertThat(formMappingSearchResult.getResult()).extracting("processDefinitionId").containsExactly(p2.getId(), p2.getId(), p2.getId(), p2.getId());
        formMappingSearchResult = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC)
                .filter(FormMappingSearchDescriptor.TASK, "step1").done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(2);
        assertThat(formMappingSearchResult.getResult()).extracting("processDefinitionId").containsExactly(p2.getId(), p1.getId());
        formMappingSearchResult = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.PROCESS_START).done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(2);
        assertThat(formMappingSearchResult.getResult()).extracting("processDefinitionId").containsExactly(p2.getId(), p1.getId());

        //resolve urls:
        PageURL p1Instanciation = getProcessConfigurationAPI().resolvePageOrURL("process/P1/1.0", context, true);
        PageURL p1Overview = getProcessConfigurationAPI().resolvePageOrURL("processInstance/P1/1.0", context, true);
        PageURL p1step1Instanciation = getProcessConfigurationAPI().resolvePageOrURL("taskInstance/P1/1.0/step1", context, true);
        assertThat(p1Instanciation.getUrl()).isEqualTo("processStartForm");
        assertThat(p1Overview.getPageId()).isNull();
        assertThat(p1step1Instanciation.getUrl()).isEqualTo(null);

        getProcessAPI().deleteProcessDefinition(p1.getId());
        getProcessAPI().deleteProcessDefinition(p2.getId());
        assertThat(
                processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC).done())
                        .getResult()).isEmpty();

    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
            getProcessConfigurationAPI().resolvePageOrURL("taskInstance/CustomerSupport/1.0/step", Collections.<String, Serializable> emptyMap(), true);
        } finally {
            deleteProcess(processDefinition);
        }
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

        final PageURL pageURLStart = getProcessConfigurationAPI().resolvePageOrURL("process/CustomerSupport/1.12", context, true);
        final PageURL pageURLOverview = getProcessConfigurationAPI().resolvePageOrURL("processInstance/CustomerSupport/1.12", context, true);
        assertThat(pageURLStart.getPageId()).isNotNull();
        assertThat(page.getId()).isEqualTo(pageURLStart.getPageId());
        assertThat(pageURLOverview.getPageId()).isNotNull();
        assertThat(custompage_globalpage.getId()).isEqualTo(pageURLOverview.getPageId());

        getPageAPI().deletePage(page.getId());
        assertThat(getProcessAPI().getProcessResolutionProblems(processDefinition.getId())).hasSize(1);

        getPageAPI().deletePage(custompage_globalpage.getId());
        disableAndDeleteProcess(processDefinition);
    }

}
