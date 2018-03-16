package org.bonitasoft.engine.api.impl.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.bonitasoft.engine.bpm.form.FormMappingDefinitionBuilder.buildFormMapping;
import static org.bonitasoft.engine.bpm.form.FormMappingModelBuilder.buildFormMappingModel;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.form.impl.SFormMappingImpl;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.page.impl.SPageImpl;
import org.bonitasoft.engine.page.impl.SPageMappingImpl;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FormMappingAndPageArtifactManagerTest {

    public static final String PAGE = "myPage";
    public static final byte[] CONTENT1 = "content1".getBytes();
    public static final byte[] CONTENT2 = "content2".getBytes();
    public static final long PROCESS_DEFINITION_ID = 15L;
    public static final long USER_ID = 12L;
    public static final long PAGE_ID = 45L;
    public static final String CUSTOMPAGE_STEP1_ZIP = "custompage_step1.zip";
    public static final String CUSTOMPAGE_STEP2_ZIP = "custompage_step2.zip";
    public static final String DISPLAY_NAME = "display name";
    public static final String DESCRIPTION = "description";
    private java.util.Map<java.lang.String, byte[]> ressources;
    @Mock
    private Properties properties;
    private FormMappingAndPageArtifactManager formMappingAndPageArtifactManager;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private BusinessArchive businessArchive;
    @Mock
    private SProcessDefinition sDefinition;
    @Mock
    private PageService pageService;
    @Mock
    private FormMappingService formMappingService;
    @Mock
    private SPage sPage;
    private List<SFormMapping> formMappings;
    @Mock
    private SPageMapping sPageMapping;
    @Mock
    private SessionService sessionService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private TechnicalLoggerService technicalLoggerService;
    @Mock
    private ProcessDefinitionService processDefinitionService;

    private BusinessArchiveBuilder barBuilder;

    @Before
    public void before() {
        formMappingAndPageArtifactManager = spy(new FormMappingAndPageArtifactManager(sessionService, sessionAccessor, pageService,
                technicalLoggerService, formMappingService, processDefinitionService));
        formMappings = new ArrayList<>();

        doReturn(PROCESS_DEFINITION_ID).when(sDefinition).getId();

        ressources = new HashMap<>();
        ressources.put("resources/customPages/custompage_step1.zip", CONTENT1);
        ressources.put("resources/customPages/custompage_step2.zip", CONTENT2);
        ressources.put("resources/otherResource/data.txt", "data".getBytes());

        doReturn(ressources).when(businessArchive).getResources();
        doReturn(14l).when(sessionService).getLoggedUserFromSession(sessionAccessor);
        when(businessArchive.getResources(anyString())).thenCallRealMethod();
    }

    @Test
    public void deployPageFailure_Should_Not_Interrupt_Deployment() throws Exception {
        //given
        formMappings.add(new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.PROCESS_OVERVIEW.getId(), "task", SFormMapping.TARGET_INTERNAL));
        BusinessArchive bar = new BusinessArchiveBuilder().createNewBusinessArchive().setFormMappings(buildFormMappingModel().build())
                .setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("mockProcess", "1.0").done()).done();
        doReturn(new ArrayList<Problem>()).when(formMappingAndPageArtifactManager).checkResolution(any(SProcessDefinition.class));
        //when then
        formMappingAndPageArtifactManager.deploy(bar, sDefinition);
        verify(formMappingAndPageArtifactManager).checkResolution(any(SProcessDefinition.class));
    }

    @Test
    public void should_check_resolution_report_problem_when_page_is_missing() throws Exception {
        //given
        doReturn(1L).when(sPageMapping).getPageId();
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.TASK.getId(), null, SFormMapping.TARGET_INTERNAL);
        sFormMapping.setPageMapping(sPageMapping);
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doReturn(null).when(pageService).getPage(anyLong());

        //when
        final List<Problem> problems = formMappingAndPageArtifactManager.checkResolution(sDefinition);

        // then
        assertThat(problems).as("should return a problem").hasSize(1);
        ProblemAssert.assertThat(problems.get(0)).hasDescription(String.format(FormMappingAndPageArtifactManager.ERROR_MESSAGE_FORM_NOT_FOUND, sPageMapping.getKey(), 1L))
                .hasLevel(Problem.Level.ERROR);

    }

    @Test
    public void should_check_resolution_report_problem_when_formMapping_on_page_has_no_pageMapping() throws Exception {
        //given
        doReturn(1L).when(sPageMapping).getPageId();
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.TASK.getId(), null, SFormMapping.TARGET_INTERNAL);
        sFormMapping.setPageMapping(sPageMapping);
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doReturn(null).when(pageService).getPage(anyLong());

        //when
        final List<Problem> problems = formMappingAndPageArtifactManager.checkResolution(sDefinition);

        // then
        assertThat(problems).as("should return a problem").hasSize(1);
        ProblemAssert.assertThat(problems.get(0)).hasDescription(String.format(FormMappingAndPageArtifactManager.ERROR_MESSAGE_FORM_NOT_FOUND, sPageMapping.getKey(), sPageMapping.getPageId()))
                .hasLevel(Problem.Level.ERROR);

    }

    @Test
    public void should_check_resolution_report_problems_when_mapping_has_no_pages() throws Exception {
        //given
        doReturn(null).when(sPageMapping).getPageId();
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.TASK.getId(), null, SFormMapping.TARGET_INTERNAL);
        sFormMapping.setPageMapping(sPageMapping);
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());

        //when
        final List<Problem> problems = formMappingAndPageArtifactManager.checkResolution(sDefinition);

        // then
        assertThat(problems).as("should not return a problem").isNotEmpty();

    }

    @Test
    public void should_format_message_when_form_mapping_page_is_null() throws Exception {
        //given
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.PROCESS_OVERVIEW.getId(), null,
                SFormMapping.TARGET_INTERNAL);
        sFormMapping.setPageMapping(sPageMapping);
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doReturn(null).when(sPageMapping).getPageId();

        //when
        final List<Problem> problems = formMappingAndPageArtifactManager.checkResolution(sDefinition);

        // then
        assertThat(problems).as("should return a problem").hasSize(1);
        ProblemAssert.assertThat(problems.get(0)).hasDescription(String.format(FormMappingAndPageArtifactManager.ERROR_MESSAGE_FORM_NOT_FOUND, sPageMapping.getKey(), null))
                .hasLevel(Problem.Level.ERROR);

    }

    @Test
    public void should_check_resolution_throw_exception() throws Exception {
        //given
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.PROCESS_OVERVIEW.getId(), "task",
                SFormMapping.TARGET_INTERNAL);
        sFormMapping.setPageMapping(sPageMapping);
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doThrow(SBonitaReadException.class).when(pageService).getPage(anyLong());

        //when
        final List<Problem> problems = formMappingAndPageArtifactManager.checkResolution(sDefinition);

        // then
        assertThat(problems).as("should return a problem").hasSize(1);

    }

    @Test
    public void should_deploy_process_insert_pages() throws Exception {
        //given
        doReturn(null).when(pageService).getPageByNameAndProcessDefinitionId(anyString(), anyLong());
        doReturn(properties).when(pageService).readPageZip(any(byte[].class));

        doReturn(DISPLAY_NAME).when(properties).getProperty(PageService.PROPERTIES_DISPLAY_NAME);
        doReturn(DESCRIPTION).when(properties).getProperty(PageService.PROPERTIES_DESCRIPTION);

        //when
        formMappingAndPageArtifactManager.deployProcessPages(businessArchive, PROCESS_DEFINITION_ID, USER_ID);

        //then
        verify(pageService, times(2)).addPage(any(SPage.class), any(byte[].class));
        verify(pageService, never()).updatePageContent(anyLong(), any(byte[].class), anyString());
    }

    @Test
    public void should_deploy_process_update_pages() throws Exception {
        //given
        doReturn(PAGE_ID).when(sPage).getId();
        doReturn(sPage).when(pageService).getPageByNameAndProcessDefinitionId(anyString(), anyLong());

        //when
        formMappingAndPageArtifactManager.deployProcessPages(businessArchive, PROCESS_DEFINITION_ID, USER_ID);

        //then
        verify(pageService, never()).addPage(any(SPage.class), any(byte[].class));
        verify(pageService).updatePageContent(PAGE_ID, CONTENT1, CUSTOMPAGE_STEP1_ZIP);
        verify(pageService).updatePageContent(PAGE_ID, CONTENT2, CUSTOMPAGE_STEP2_ZIP);
    }

    @Test
    public void should_getPageResources_filter_resources() throws Exception {
        //given

        //when
        final Map<String, byte[]> pageResources = formMappingAndPageArtifactManager.getPageResources(businessArchive);

        //then
        assertThat(pageResources).hasSize(2).containsKeys("resources/customPages/custompage_step1.zip", "resources/customPages/custompage_step2.zip");
    }

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
        formMappingAndPageArtifactManager.deployFormMappings(
                barBuilder.setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel()
                                .addProcessStartForm(startForm, FormMappingTarget.INTERNAL)
                                .addProcessOverviewForm(overviewForm, FormMappingTarget.INTERNAL)
                                .build())
                        .done(),
                processDefinitionId);

        // then:
        verify(formMappingService, times(1))
                .create(processDefinitionId, null, FormMappingType.PROCESS_START.getId(), FormMappingTarget.INTERNAL.name(), startForm);
        verify(formMappingService, times(1))
                .create(processDefinitionId, null, FormMappingType.PROCESS_OVERVIEW.getId(), FormMappingTarget.INTERNAL.name(), overviewForm);
    }

    @Test
    public void deployShouldCreateNonDeclaredMappingsForProcessForms() throws Exception {
        // given:
        final long processDefinitionId = 3L;

        // when:
        formMappingAndPageArtifactManager.deployFormMappings(barBuilder.done(), processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, null, FormMappingType.PROCESS_OVERVIEW.getId(), FormMappingTarget.NONE.name(),
                null);
        verify(formMappingService, times(1)).create(processDefinitionId, null, FormMappingType.PROCESS_START.getId(), FormMappingTarget.NONE.name(), null);
    }

    @Test
    public void deployShouldCreateDeclaredMappingsForDeclaredTaskForms() throws Exception {
        // when:
        final String taskname = "taskname";
        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance("proc", "5");
        processDefBuilder.addUserTask(taskname, null);

        final long processDefinitionId = 3L;
        final String form = "pagename";
        final FormMappingType type = FormMappingType.TASK;
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefBuilder.done());
        businessArchiveBuilder.setFormMappings(buildFormMappingModel().withFormMapping(
                buildFormMapping(form, type, FormMappingTarget.INTERNAL).withTaskname(taskname).build()).build());
        formMappingAndPageArtifactManager.deployFormMappings(businessArchiveBuilder.done(), processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, taskname, type.getId(), FormMappingTarget.INTERNAL.name(), form);
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
        formMappingAndPageArtifactManager.deployFormMappings(businessArchiveBuilder.done(), processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, null, FormMappingType.PROCESS_START.getId(), FormMappingTarget.NONE.name(), null);
        verify(formMappingService, times(1)).create(processDefinitionId, null, FormMappingType.PROCESS_OVERVIEW.getId(), FormMappingTarget.NONE.name(),
                null);
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
        formMappingAndPageArtifactManager.deployFormMappings(businessArchive, processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, taskName, FormMappingType.TASK.getId(), FormMappingTarget.NONE.name(), null);
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
        businessArchiveBuilder.setFormMappings(FormMappingModelBuilder.buildFormMappingModel()
                .addTaskForm("someForm", FormMappingTarget.INTERNAL, unknownTaskName).build());
        final BusinessArchive businessArchive = businessArchiveBuilder.done();

        // when:
        formMappingAndPageArtifactManager.deployFormMappings(businessArchive, processDefinitionId);

        // then:
        verify(formMappingService, times(1)).create(eq(processDefinitionId), eq(declaredTaskName), eq(FormMappingType.TASK.getId()), anyString(),
                nullable(String.class));
        verify(formMappingService, times(0)).create(eq(processDefinitionId), eq(unknownTaskName), eq(FormMappingType.TASK.getId()), anyString(),
                nullable(String.class));
    }

    @Test
    public void checkFormMappingResolutionShouldAddProblemsIfPageIdIsNull() throws Exception {
        ArrayList<Problem> problems = new ArrayList<>();

        SFormMappingImpl sFormMappingTask = new SFormMappingImpl(321324, FormMappingType.TASK.getId(), "Step1", SFormMapping.TARGET_INTERNAL);
        SFormMappingImpl sFormMappingProcessOverview = new SFormMappingImpl(321324, FormMappingType.PROCESS_OVERVIEW.getId(), null,
                SFormMapping.TARGET_INTERNAL);
        SFormMappingImpl sFormMappingProcessStart = new SFormMappingImpl(321324, FormMappingType.PROCESS_START.getId(), null, SFormMapping.TARGET_INTERNAL);

        SPageMappingImpl pageMapping = new SPageMappingImpl();
        sFormMappingTask.setPageMapping(pageMapping);
        sFormMappingProcessOverview.setPageMapping(pageMapping);
        sFormMappingProcessStart.setPageMapping(pageMapping);

        formMappingAndPageArtifactManager.checkFormMappingResolution(sFormMappingTask, problems);
        formMappingAndPageArtifactManager.checkFormMappingResolution(sFormMappingProcessOverview, problems);
        formMappingAndPageArtifactManager.checkFormMappingResolution(sFormMappingProcessStart, problems);
        assertThat(problems).as("the problem list should contain three mapping problems").hasSize(3).extracting("resource", "resourceId")
                .contains(tuple("form mapping", "Step1"),
                        tuple("form mapping", FormMappingType.PROCESS_OVERVIEW.name()),
                        tuple("form mapping", FormMappingType.PROCESS_START.name()));
    }

    @Test
    public void checkFormMappingResolutionShouldNotAddProblemsIfThereIsNONE() throws Exception {
        ArrayList<Problem> problems = new ArrayList<>();

        SFormMappingImpl sFormMappingTask = new SFormMappingImpl(321324, FormMappingType.TASK.getId(), "Step1", SFormMapping.TARGET_NONE);
        SFormMappingImpl sFormMappingProcessOverview = new SFormMappingImpl(321324, FormMappingType.PROCESS_OVERVIEW.getId(), null, SFormMapping.TARGET_NONE);
        SFormMappingImpl sFormMappingProcessStart = new SFormMappingImpl(321324, FormMappingType.PROCESS_START.getId(), null, SFormMapping.TARGET_NONE);

        formMappingAndPageArtifactManager.checkFormMappingResolution(sFormMappingTask, problems);
        formMappingAndPageArtifactManager.checkFormMappingResolution(sFormMappingProcessOverview, problems);
        formMappingAndPageArtifactManager.checkFormMappingResolution(sFormMappingProcessStart, problems);
        assertThat(problems).as("the problem list should not contain any mapping problem").hasSize(0);
    }

    @Test
    public void checkFormMappingResolutionShouldAddProblemsIfThereIsUNDEFINED() throws Exception {
        ArrayList<Problem> problems = new ArrayList<>();

        SFormMappingImpl sFormMappingTask = new SFormMappingImpl(321324, FormMappingType.TASK.getId(), "Step1", SFormMapping.TARGET_UNDEFINED);
        SFormMappingImpl sFormMappingProcessOverview = new SFormMappingImpl(321324, FormMappingType.PROCESS_OVERVIEW.getId(), null,
                SFormMapping.TARGET_UNDEFINED);
        SFormMappingImpl sFormMappingProcessStart = new SFormMappingImpl(321324, FormMappingType.PROCESS_START.getId(), null, SFormMapping.TARGET_UNDEFINED);

        formMappingAndPageArtifactManager.checkFormMappingResolution(sFormMappingTask, problems);
        formMappingAndPageArtifactManager.checkFormMappingResolution(sFormMappingProcessOverview, problems);
        formMappingAndPageArtifactManager.checkFormMappingResolution(sFormMappingProcessStart, problems);

        assertThat(problems).as("the problem list should contain three mapping problems").hasSize(3).extracting("resource", "resourceId").contains(
                tuple("form mapping", "Step1"),
                tuple("form mapping", FormMappingType.PROCESS_OVERVIEW.name()),
                tuple("form mapping", FormMappingType.PROCESS_START.name()));
    }

    @Test
    public void checkFormMappingResolutionShouldNotContainAnyProblem() throws Exception {
        ArrayList<Problem> problems = new ArrayList<>();
        SFormMappingImpl sFormMapping = new SFormMappingImpl(321324, 1, "Step1", SFormMapping.TARGET_INTERNAL);
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        long pageId = 1322L;
        pageMapping.setPageId(pageId);
        when(pageService.getPage(pageId)).thenReturn(mock(SPage.class));

        sFormMapping.setPageMapping(pageMapping);

        formMappingAndPageArtifactManager.checkFormMappingResolution(sFormMapping, problems);
        assertThat(problems).as("the problem list should not contain any mapping problems").hasSize(0);
    }

    @Test
    public void should_export_form_mapping_with_existing_page() throws Exception {
        //given
        BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("myProcess", "1.0").addActor("actor")
                .addUserTask("task1", "actor").addUserTask("task2", "actor").getProcess());
        doReturn(Arrays.asList(withFormMapping("myPage1", PROCESS_DEFINITION_ID, FormMappingType.PROCESS_OVERVIEW, FormMappingTarget.INTERNAL, null),
                withFormMapping("myPage2", PROCESS_DEFINITION_ID, FormMappingType.PROCESS_START, FormMappingTarget.INTERNAL, null),
                withFormMapping("myPage3", PROCESS_DEFINITION_ID, FormMappingType.TASK, FormMappingTarget.INTERNAL, "task1"),
                withFormMapping("myPage4", PROCESS_DEFINITION_ID, FormMappingType.TASK, FormMappingTarget.INTERNAL, "task2")))
                        .when(formMappingService).list(PROCESS_DEFINITION_ID, 0, Integer.MAX_VALUE);
        //when
        formMappingAndPageArtifactManager.exportToBusinessArchive(PROCESS_DEFINITION_ID, businessArchiveBuilder);
        //then
        FormMappingModel formMappingModel = businessArchiveBuilder.done().getFormMappingModel();
        assertThat(formMappingModel.getFormMappings())
                .containsOnly(new FormMappingDefinition("myPage1", FormMappingType.PROCESS_OVERVIEW, FormMappingTarget.INTERNAL),
                        new FormMappingDefinition("myPage2", FormMappingType.PROCESS_START, FormMappingTarget.INTERNAL),
                        new FormMappingDefinition("myPage3", FormMappingType.TASK, FormMappingTarget.INTERNAL, "task1"),
                        new FormMappingDefinition("myPage4", FormMappingType.TASK, FormMappingTarget.INTERNAL, "task2"));
    }

    @Test
    public void should_export_form_mapping_with_unexisting_page() throws Exception {
        //given
        BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("myProcess", "1.0").addActor("actor")
                .addUserTask("task1", "actor").addUserTask("task2", "actor").getProcess());
        doReturn(Collections.singletonList(withFormMapping(null, PROCESS_DEFINITION_ID, FormMappingType.TASK, FormMappingTarget.INTERNAL, "task2")))
                .when(formMappingService).list(PROCESS_DEFINITION_ID, 0, Integer.MAX_VALUE);
        //when
        formMappingAndPageArtifactManager.exportToBusinessArchive(PROCESS_DEFINITION_ID, businessArchiveBuilder);
        //then
        FormMappingModel formMappingModel = businessArchiveBuilder.done().getFormMappingModel();
        assertThat(formMappingModel.getFormMappings())
                .containsOnly(
                        new FormMappingDefinition(null, FormMappingType.TASK, FormMappingTarget.INTERNAL, null));
    }

    private SFormMappingImpl withFormMapping(String pageName, long processDefinitionId, FormMappingType type, FormMappingTarget target, String task)
            throws SBonitaReadException, SObjectNotFoundException {
        SFormMappingImpl sFormMapping = new SFormMappingImpl(processDefinitionId, type.getId(), task, target.name());
        SPageMappingImpl pageMapping = new SPageMappingImpl();
        if (pageName != null) {
            long pageId = UUID.randomUUID().getMostSignificantBits();
            pageMapping.setPageId(pageId);
            sFormMapping.setPageMapping(pageMapping);
            doReturn(new SPageImpl(pageName, 0, 0, false, "page.zip")).when(pageService).getPage(pageId);
        }
        return sFormMapping;
    }

}
