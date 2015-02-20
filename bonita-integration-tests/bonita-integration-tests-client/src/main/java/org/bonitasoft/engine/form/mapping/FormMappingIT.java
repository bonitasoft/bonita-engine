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

package org.bonitasoft.engine.form.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessConfigurationAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.formmapping.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class FormMappingIT extends TestWithUser {

    @Ignore("wait for deploy to be implemented")
    @Test
    public void deployProcessesWithFormMappingAndUpdateThem() throws Exception {

        ProcessDefinitionBuilder p1Builder = new ProcessDefinitionBuilder().createNewInstance("P1", "1.0");
        p1Builder.addUserTask("step1", "actor").addUserTask("step2", "actor");
        p1Builder.addActor("actor");
        BusinessArchiveBuilder bar1 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(p1Builder.done())
                .setFormMappings(FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("processStartForm", false)
                        .addTaskForm("task1Form", false, "step1").addProcessOverviewForm("process1OverviewForm", false).build());
        ProcessDefinitionBuilder p2Builder = new ProcessDefinitionBuilder().createNewInstance("P2", "1.0");
        p2Builder.addUserTask("step1", "actor").addUserTask("step2", "actor");
        p2Builder.addActor("actor");
        BusinessArchiveBuilder bar2 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(p2Builder.done())
                .setFormMappings(FormMappingModelBuilder.buildFormMappingModel().addProcessStartForm("processStartForm", false)
                        .addTaskForm("task2Form", false, "step1").addProcessOverviewForm("process2OverviewForm", false).build());

        ProcessDefinition p1 = deployAndEnableProcessWithActor(bar1.done(), "actor", user);
        ProcessDefinition p2 = deployAndEnableProcessWithActor(bar2.done(), "actor", user);

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
        assertThat(processOverviewForm1.getForm()).isEqualTo("process1OverviewForm");
        assertThat(step1Form1.getProcessDefinitionId()).isEqualTo(p1.getId());
        assertThat(step1Form1.getForm()).isEqualTo("task1Form");
        assertThat(step2Form1.getProcessDefinitionId()).isEqualTo(p1.getId());
        assertThat(step2Form1.getForm()).isEqualTo(null);

        assertThat(processStartForm2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(processStartForm2.getForm()).isEqualTo("processStartForm");
        assertThat(processOverviewForm2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(processOverviewForm2.getForm()).isEqualTo("process2OverviewForm");
        assertThat(step1Form2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(step1Form2.getForm()).isEqualTo("task2Form");
        assertThat(step2Form2.getProcessDefinitionId()).isEqualTo(p2.getId());
        assertThat(step2Form2.getForm()).isEqualTo(null);


        //search
        SearchResult<FormMapping> formMappingSearchResult = processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC).done());
        assertThat(formMappingSearchResult.getCount()).isEqualTo(8);
        assertThat(formMappingSearchResult.getResult()).extracting("processDefinitionId").containsExactly(p2.getId(),p2.getId(),p2.getId(),p2.getId(),p1.getId(),p1.getId(),p1.getId(),p1.getId());

        //update
        processConfigurationAPI.updateFormMapping(step2Form1.getId(),"newFormUrlForStep2",true);
        FormMapping updatedStep2Form1 = processConfigurationAPI.getTaskForm(p1.getId(), "step2");
        assertThat(updatedStep2Form1).isEqualToIgnoringGivenFields(step2Form1,"form", "external");
        assertThat(updatedStep2Form1.getForm()).isEqualTo("newFormUrlForStep2");
        assertThat(updatedStep2Form1.isExternal()).isTrue();

        disableAndDeleteProcess(p1,p2);
        assertThat(processConfigurationAPI.searchFormMappings(new SearchOptionsBuilder(0, 100).sort(FormMappingSearchDescriptor.ID, Order.DESC).done()).getResult()).isEmpty();
    }
}
