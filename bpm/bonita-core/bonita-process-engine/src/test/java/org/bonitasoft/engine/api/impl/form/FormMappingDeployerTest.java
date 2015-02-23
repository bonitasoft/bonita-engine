package org.bonitasoft.engine.api.impl.form;

import static org.bonitasoft.engine.bpm.form.FormMappingDefinitionBuilder.buildFormMapping;
import static org.bonitasoft.engine.bpm.form.FormMappingModelBuilder.buildFormMappingModel;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.core.form.FormMappingService;
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
        final boolean isExternal = false;
        deployer.deployFormMappings(
                barBuilder.setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel()
                                .addProcessStartForm(startForm, isExternal)
                                .addProcessOverviewForm(overviewForm, isExternal)
                                .build()).done(), processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, null, startForm, isExternal, FormMappingType.PROCESS_START.name());
        verify(formMappingService, times(1)).create(processDefinitionId, null, overviewForm, isExternal, FormMappingType.PROCESS_OVERVIEW.name());
    }

    @Test
    public void deployShouldCreateNonDeclaredMappingsForProcessForms() throws Exception {
        // given:
        final long processDefinitionId = 3L;

        // when:
        deployer.deployFormMappings(barBuilder.done(), processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, null, null, false, FormMappingType.PROCESS_OVERVIEW.name());
        verify(formMappingService, times(1)).create(processDefinitionId, null, null, false, FormMappingType.PROCESS_START.name());
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
        final boolean isExternal = false;
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefBuilder.done());
        businessArchiveBuilder.setFormMappings(buildFormMappingModel().withFormMapping(
                buildFormMapping(form, FormMappingType.valueOf(type), isExternal).withTaskname(taskname).build()).build());
        deployer.deployFormMappings(businessArchiveBuilder.done(), processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, taskname, form, isExternal, type);
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
        final boolean isExternal = false;
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefBuilder.done());
        businessArchiveBuilder.setFormMappings(buildFormMappingModel().withFormMapping(
                buildFormMapping(form, FormMappingType.valueOf(type), isExternal).withTaskname(taskname).build()).build());
        deployer.deployFormMappings(businessArchiveBuilder.done(), processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, null, null, false, FormMappingType.PROCESS_START.name());
        verify(formMappingService, times(1)).create(processDefinitionId, null, null, false, FormMappingType.PROCESS_OVERVIEW.name());
        verifyNoMoreInteractions(formMappingService);
    }

    @Test
    public void deployShouldCreateEmptyMappingsForNonDeclaredTasks() throws Exception {
        // Given:
        final long processDefinitionId = 5L;
        final String taskName = "taskname";
        final boolean isExternal = false;
        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance("proc", "5");
        processDefBuilder.addUserTask(taskName, null);
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefBuilder.done()).done();

        // when:
        deployer.deployFormMappings(businessArchive, processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, taskName, null, isExternal, FormMappingType.TASK.name());
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
        businessArchiveBuilder.setFormMappings(FormMappingModelBuilder.buildFormMappingModel().addTaskForm("someForm", false, unknownTaskName).build());
        final BusinessArchive businessArchive = businessArchiveBuilder.done();

        // when:
        deployer.deployFormMappings(businessArchive, processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(eq(processDefinitionId), eq(declaredTaskName), anyString(), anyBoolean(), eq(FormMappingType.TASK.name()));
        verify(formMappingService, times(0)).create(eq(processDefinitionId), eq(unknownTaskName), anyString(), anyBoolean(), eq(FormMappingType.TASK.name()));
    }
}
