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
package org.bonitasoft.engine.api.impl.form;

import static org.bonitasoft.engine.bpm.form.FormMappingDefinitionBuilder.buildFormMapping;
import static org.bonitasoft.engine.bpm.form.FormMappingModelBuilder.buildFormMappingModel;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FormMappingDeployerTest {

    @Mock
    private FormMappingService formMappingService;

    @InjectMocks
    private FormMappingDeployer deployer;

    private BusinessArchiveBuilder barBuilder;

    @Before
    public void init() throws InvalidProcessDefinitionException {
        barBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("p", "1").done());
    }

    @Test
    public void deployShouldCreateDeclaredMappingsForProcessForms() throws Exception {
        // when:
        final long processDefinitionId = 3L;
        final String startForm = "startPage";
        final String overviewForm = "overviewPage";
        deployer.deployFormMappings(
                barBuilder.setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel()
                                .addProcessStartForm(startForm, FormMappingTarget.INTERNAL)
                                .addProcessOverviewForm(overviewForm, FormMappingTarget.INTERNAL)
                                .build()).done(), processDefinitionId);

        // then:
//        verify(formMappingService, times(1)).create(processDefinitionId, null, startForm, FormMappingTarget.INTERNAL.name(), FormMappingType.PROCESS_START.name(), pageMappingKey);
//        verify(formMappingService, times(1)).create(processDefinitionId, null, overviewForm, FormMappingTarget.INTERNAL.name(), FormMappingType.PROCESS_OVERVIEW.name(), pageMappingKey);
    }

    @Test
    public void deployShouldCreateNonDeclaredMappingsForProcessForms() throws Exception {
        // given:
        final long processDefinitionId = 3L;

        // when:
        deployer.deployFormMappings(barBuilder.done(), processDefinitionId);

        // then:
        //        verify(formMappingService, times(1)).create(processDefinitionId, null, null, FormMappingTarget.INTERNAL.name(), FormMappingType.PROCESS_OVERVIEW.name(), pageMappingKey);
        //        verify(formMappingService, times(1)).create(processDefinitionId, null, null, FormMappingTarget.INTERNAL.name(), FormMappingType.PROCESS_START.name(), pageMappingKey);
    }

    @Test
    public void deployShouldCreateDeclaredMappingsForDeclaredTaskForms() throws Exception {
        // when:
        final String taskname = "taskname";
        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance("proc", "5");
        processDefBuilder.addUserTask(taskname, null);

        final long processDefinitionId = 3L;
        final String form = "pagename";
        final String type = FormMappingType.TASK.name();
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefBuilder.done());
        businessArchiveBuilder.setFormMappings(buildFormMappingModel().withFormMapping(
                buildFormMapping(form, FormMappingType.valueOf(type), FormMappingTarget.INTERNAL).withTaskname(taskname).build()).build());
        deployer.deployFormMappings(businessArchiveBuilder.done(), processDefinitionId);

        // then:
//        verify(formMappingService, times(1)).create(processDefinitionId, taskname, form, FormMappingTarget.INTERNAL.name(), type, pageMappingKey);
    }

    @Test
    public void deployShouldNotCreateDeclaredMappingsForDeclaredAutomaticTaskForms() throws Exception {
        // when:
        final String taskname = "taskname";
        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance("proc", "5");
        processDefBuilder.addAutomaticTask(taskname);

        final long processDefinitionId = 3L;
        final String form = "pagename";
        final String type = FormMappingType.TASK.name();
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefBuilder.done());
        businessArchiveBuilder.setFormMappings(buildFormMappingModel().withFormMapping(
                buildFormMapping(form, FormMappingType.valueOf(type), FormMappingTarget.INTERNAL).withTaskname(taskname).build()).build());
        deployer.deployFormMappings(businessArchiveBuilder.done(), processDefinitionId);

        // then:
//        verify(formMappingService, times(1)).create(processDefinitionId, null, null, FormMappingTarget.INTERNAL.name(), FormMappingType.PROCESS_START.name(), pageMappingKey);
//        verify(formMappingService, times(1)).create(processDefinitionId, null, null, FormMappingTarget.INTERNAL.name(), FormMappingType.PROCESS_OVERVIEW.name(), pageMappingKey);
        verifyNoMoreInteractions(formMappingService);
    }

    @Test
    public void deployShouldCreateEmptyMappingsForNonDeclaredTasks() throws Exception {
        // Given:
        final long processDefinitionId = 5L;
        final String taskName = "taskname";
        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance("proc", "5");
        processDefBuilder.addUserTask(taskName, null);
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefBuilder.done()).done();

        // when:
        deployer.deployFormMappings(businessArchive, processDefinitionId);

        // then:
//        verify(formMappingService, times(1)).create(processDefinitionId, taskName, null, FormMappingTarget.INTERNAL.name(), FormMappingType.TASK.name(), pageMappingKey);
    }

    @Test
    public void deployShouldIgnoreDeclaredMappingsForUndeclaredTasks() throws Exception {
        // Given:
        final long processDefinitionId = 5L;
        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance("proc", "5");
        final String declaredTaskName = "declaredtaskName";
        processDefBuilder.addUserTask(declaredTaskName, null);
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefBuilder.done());
        final String unknownTaskName = "Unknown__Task__Name";
        businessArchiveBuilder.setFormMappings(FormMappingModelBuilder.buildFormMappingModel().addTaskForm("someForm", FormMappingTarget.INTERNAL, unknownTaskName).build());
        final BusinessArchive businessArchive = businessArchiveBuilder.done();

        // when:
        deployer.deployFormMappings(businessArchive, processDefinitionId);

        // then:
//        verify(formMappingService, times(1)).create(eq(processDefinitionId), eq(declaredTaskName), anyString(), anyString(), eq(FormMappingType.TASK.name()), pageMappingKey);
//        verify(formMappingService, times(0)).create(eq(processDefinitionId), eq(unknownTaskName), anyString(), anyString(), eq(FormMappingType.TASK.name()), pageMappingKey);
    }
}
