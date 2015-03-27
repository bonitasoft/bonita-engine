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

package org.bonitasoft.engine.form;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessConfigurationAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class FormMappingIT extends TestWithUser {

    public static final String PROCESS1_OVERVIEW_FORM = "process1OverviewForm";
    public static final String PROCESS_2_OVERVIEW_FORM = "process2OverviewForm";

    @Override
    @Before
    public void apiTestUtilBeforeClass() {
        setForceToNonBlockingFormMappingToDefaultValue(false);
    }

    @Test
    public void deployProcessesWithFormMappingAndUpdateThem() throws Exception {

        ProcessDefinitionBuilder p1Builder = new ProcessDefinitionBuilder().createNewInstance("P1", "1.0");
        p1Builder.addUserTask("step1", "actor").addUserTask("step2", "actor");
        p1Builder.addActor("actor");
        BusinessArchiveBuilder bar1 = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(p1Builder.done())
                .setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("processStartForm", FormMappingTarget.URL)
                                .addTaskForm("task1Form", FormMappingTarget.LEGACY, "step1")
                                .addTaskForm("", FormMappingTarget.URL, "step2")
                                .addProcessOverviewForm(PROCESS1_OVERVIEW_FORM, FormMappingTarget.LEGACY).build());
        ProcessDefinitionBuilder p2Builder = new ProcessDefinitionBuilder().createNewInstance("P2", "1.0");
        p2Builder.addUserTask("step1", "actor").addUserTask("step2", "actor");
        p2Builder.addActor("actor");
        BusinessArchiveBuilder bar2 = new BusinessArchiveBuilder()
                .createNewBusinessArchive()
                .setProcessDefinition(p2Builder.done())
                .setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("processStartForm", FormMappingTarget.LEGACY)
                                .addTaskForm("task2Form", FormMappingTarget.LEGACY, "step1")
                                .addTaskForm("", FormMappingTarget.URL, "step2")
                                .addProcessOverviewForm(PROCESS_2_OVERVIEW_FORM, FormMappingTarget.LEGACY).build());

        ProcessDefinition p1 = deployAndEnableProcessWithActor(bar1.done(), "actor", user);
        ProcessDefinition p2 = deployAndEnableProcessWithActor(bar2.done(), "actor", user);

        long afterDeploy = System.currentTimeMillis();
        ProcessConfigurationAPI processConfigurationAPI = getProcessConfigurationAPI();

        //get
        FormMapping processStartForm1 = processConfigurationAPI.getProcessStartForm(p1.getId());
        FormMapping processOverviewForm1 = processConfigurationAPI.getProcessOverviewForm(p1.getId());
        FormMapping step1Form1 = processConfigurationAPI.getTaskForm(p1.getId(), "step1");
        FormMapping step2Form1 = processConfigurationAPI.getTaskForm(p1.getId(), "step2");
        FormMapping processStartForm2 = processConfigurationAPI.getProcessStartForm(p2.getId());
        FormMapping processOverviewForm2 = processConfigurationAPI.getProcessOverviewForm(p2.getId());
        FormMapping step1Form2 = processConfigurationAPI.getTaskForm(p2.getId(), "step1");
        FormMapping step2Form2 = processConfigurationAPI.getTaskForm(p2.getId(), "step2");

        assertThat(processStartForm1.getProcessDefinitionId()).isEqualTo(p1.getId());
        assertThat(processStartForm1.getForm()).isEqualTo("processStartForm");
        assertThat(processOverviewForm1.getProcessDefinitionId()).isEqualTo(p1.getId());
        assertThat(processOverviewForm1.getForm()).isEqualTo(PROCESS1_OVERVIEW_FORM);
        assertThat(step1Form1.getProcessDefinitionId()).isEqualTo(p1.getId());
        assertThat(step1Form1.getForm()).isEqualTo("task1Form");
        assertThat(step2Form1.getProcessDefinitionId()).isEqualTo(p1.getId());
        assertThat(step2Form1.getForm()).isEqualTo("");

        assertThat(processStartForm2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(processStartForm2.getForm()).isEqualTo("processStartForm");
        assertThat(processOverviewForm2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(processOverviewForm2.getForm()).isEqualTo(PROCESS_2_OVERVIEW_FORM);
        assertThat(step1Form2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(step1Form2.getForm()).isEqualTo("task2Form");
        assertThat(step2Form2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(step2Form2.getForm()).isEqualTo("");

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
                .filter(FormMappingSearchDescriptor.FORM, PROCESS1_OVERVIEW_FORM).done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(1);
        assertThat(formMappingSearchResult.getResult()).extracting("processDefinitionId").containsExactly(p1.getId());
        formMappingSearchResult = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC)
                .filter(FormMappingSearchDescriptor.TYPE, FormMappingType.PROCESS_START.name()).done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(2);
        assertThat(formMappingSearchResult.getResult()).extracting("processDefinitionId").containsExactly(p2.getId(), p1.getId());
        formMappingSearchResult = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC)
                .filter(FormMappingSearchDescriptor.TARGET, "URL").done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(3);
        assertThat(formMappingSearchResult.getResult()).extracting("processDefinitionId").containsExactly(p1.getId());

        //update
        processConfigurationAPI.updateFormMapping(step2Form1.getId(), "newFormUrlForStep2", FormMappingTarget.LEGACY);
        FormMapping updatedStep2Form1 = processConfigurationAPI.getTaskForm(p1.getId(), "step2");
        assertThat(updatedStep2Form1).isEqualToIgnoringGivenFields(step2Form1, "form", "target", "lastUpdateDate", "lastUpdatedBy");
        assertThat(updatedStep2Form1.getForm()).isEqualTo("newFormUrlForStep2");
        assertThat(updatedStep2Form1.getTarget()).isEqualTo(FormMappingTarget.LEGACY);
        assertThat(updatedStep2Form1.getLastUpdateDate()).isAfter(new Date(afterDeploy));
        assertThat(updatedStep2Form1.getLastUpdatedBy()).isEqualTo(user.getId());
        assertThat(step2Form1.getLastUpdateDate()).isNull();

        disableAndDeleteProcess(p1, p2);
        assertThat(
                processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC).done())
                        .getResult()).isEmpty();
    }
}
