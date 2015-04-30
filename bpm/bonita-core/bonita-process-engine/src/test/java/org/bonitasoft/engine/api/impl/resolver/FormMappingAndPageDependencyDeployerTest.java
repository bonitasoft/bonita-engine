package org.bonitasoft.engine.api.impl.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bpm.form.FormMappingDefinitionBuilder.buildFormMapping;
import static org.bonitasoft.engine.bpm.form.FormMappingModelBuilder.buildFormMappingModel;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.api.impl.converter.SPageAssert;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.form.FormMappingModelBuilder;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.form.impl.SFormMappingImpl;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SInvalidPageZipMissingIndexException;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class FormMappingAndPageDependencyDeployerTest {

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
    private FormMappingAndPageDependencyDeployer pageProcessDependencyResolver;
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


    private BusinessArchiveBuilder barBuilder;


    @Before
    public void before() {
        pageProcessDependencyResolver = new FormMappingAndPageDependencyDeployer();
        formMappings = new ArrayList<>();

        doReturn(pageService).when(tenantServiceAccessor).getPageService();
        doReturn(formMappingService).when(tenantServiceAccessor).getFormMappingService();
        doReturn(sessionAccessor).when(tenantServiceAccessor).getSessionAccessor();
        doReturn(sessionService).when(tenantServiceAccessor).getSessionService();
        doReturn(technicalLoggerService).when(tenantServiceAccessor).getTechnicalLoggerService();

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
    public void should_retrieve_custom_page_in_form_mapping() throws Exception {
        //given
        doThrow(SInvalidPageZipMissingIndexException.class).when(pageService).readPageZip(any(byte[].class));
        formMappings.add(new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.PROCESS_OVERVIEW.getId(), "task"));
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doReturn(null).when(pageService).getPageByNameAndProcessDefinitionId(PAGE, PROCESS_DEFINITION_ID);
        BusinessArchive bar = new BusinessArchiveBuilder().createNewBusinessArchive().setFormMappings(buildFormMappingModel().build()).setProcessDefinition(new ProcessDefinitionBuilder().createNewInstance("mockProcess", "1.0").done()).done();

        //when then
        assertThat(pageProcessDependencyResolver.deploy(tenantServiceAccessor, bar, sDefinition)).as("missing page should not block resolve step")
                .isTrue();
    }

    @Test
    public void should_check_resolution_report_problem_when_page_is_missing() throws Exception {
        //given
        doReturn(1L).when(sPageMapping).getPageId();
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.TASK.getId(), null);
        sFormMapping.setPageMapping(sPageMapping);
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doReturn(null).when(pageService).getPage(anyLong());

        //when
        final List<Problem> problems = pageProcessDependencyResolver.checkResolution(tenantServiceAccessor, sDefinition);

        // then
        assertThat(problems).as("should return a problem").hasSize(1);
        ProblemAssert.assertThat(problems.get(0)).hasDescription(String.format(FormMappingAndPageDependencyDeployer.ERROR_MESSAGE, sFormMapping))
                .hasLevel(Problem.Level.ERROR);

    }


    @Test
    public void should_check_resolution_report_problem_when_formMapping_on_page_has_no_pageMapping() throws Exception {
        //given
        doReturn(1L).when(sPageMapping).getPageId();
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.TASK.getId(), null);
        sFormMapping.setPageMapping(sPageMapping);
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doReturn(null).when(pageService).getPage(anyLong());

        //when
        final List<Problem> problems = pageProcessDependencyResolver.checkResolution(tenantServiceAccessor, sDefinition);

        // then
        assertThat(problems).as("should return a problem").hasSize(1);
        ProblemAssert.assertThat(problems.get(0)).hasDescription(String.format(FormMappingAndPageDependencyDeployer.ERROR_MESSAGE, sFormMapping))
                .hasLevel(Problem.Level.ERROR);

    }

    @Test
    public void should_check_resolution_report_problems_when_mapping_has_no_pages() throws Exception {
        //given
        doReturn(null).when(sPageMapping).getPageId();
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.TASK.getId(), null);
        sFormMapping.setPageMapping(sPageMapping);
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doReturn(null).when(pageService).getPage(anyLong());

        //when
        final List<Problem> problems = pageProcessDependencyResolver.checkResolution(tenantServiceAccessor, sDefinition);

        // then
        assertThat(problems).as("should not return a problem").isNotEmpty();

    }


    @Test
    public void should_format_message_when_form_mapping_page_is_null() throws Exception {
        //given
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.PROCESS_OVERVIEW.getId(), null);
        sFormMapping.setPageMapping(sPageMapping);
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());

        //when
        final List<Problem> problems = pageProcessDependencyResolver.checkResolution(tenantServiceAccessor, sDefinition);

        // then
        assertThat(problems).as("should return a problem").hasSize(1);
        ProblemAssert.assertThat(problems.get(0)).hasDescription(String.format(FormMappingAndPageDependencyDeployer.ERROR_MESSAGE, sFormMapping))
                .hasLevel(Problem.Level.ERROR);

    }

    @Test
    public void should_check_resolution_throw_exception() throws Exception {
        //given
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, FormMappingType.PROCESS_OVERVIEW.getId(), "task");
        sFormMapping.setPageMapping(sPageMapping);
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doThrow(SBonitaReadException.class).when(pageService).getPage(anyLong());

        //when
        final List<Problem> problems = pageProcessDependencyResolver.checkResolution(tenantServiceAccessor, sDefinition);

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

        Answer<SPage> answer = new Answer<SPage>() {

            @Override
            public SPage answer(InvocationOnMock invocation) throws Throwable {
                final SPage pageToAdd = (SPage) invocation.getArguments()[0];
                SPageAssert.assertThat(pageToAdd).hasDisplayName(DISPLAY_NAME);
                SPageAssert.assertThat(pageToAdd).hasDescription(DESCRIPTION);

                return pageToAdd;
            }
        };
        when(pageService.addPage(any(sPage.getClass()), any(byte[].class))).then(answer);

        //when
        pageProcessDependencyResolver.deployProcessPages(businessArchive, PROCESS_DEFINITION_ID, USER_ID, tenantServiceAccessor);

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
        pageProcessDependencyResolver.deployProcessPages(businessArchive, PROCESS_DEFINITION_ID, USER_ID, tenantServiceAccessor);

        //then
        verify(pageService, never()).addPage(any(SPage.class), any(byte[].class));
        verify(pageService).updatePageContent(PAGE_ID, CONTENT1, CUSTOMPAGE_STEP1_ZIP);
        verify(pageService).updatePageContent(PAGE_ID, CONTENT2, CUSTOMPAGE_STEP2_ZIP);
    }

    @Test
    public void should_getPageResources_filter_resources() throws Exception {
        //given

        //when
        final Map<String, byte[]> pageResources = pageProcessDependencyResolver.getPageResources(businessArchive);

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
        pageProcessDependencyResolver.deployFormMappings(
                barBuilder.setFormMappings(
                        FormMappingModelBuilder.buildFormMappingModel()
                                .addProcessStartForm(startForm, FormMappingTarget.INTERNAL)
                                .addProcessOverviewForm(overviewForm, FormMappingTarget.INTERNAL)
                                .build()).done(), processDefinitionId, tenantServiceAccessor);

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
        pageProcessDependencyResolver.deployFormMappings(barBuilder.done(), processDefinitionId, tenantServiceAccessor);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, null, FormMappingType.PROCESS_OVERVIEW.getId(), FormMappingTarget.UNDEFINED.name(),
                null);
        verify(formMappingService, times(1)).create(processDefinitionId, null, FormMappingType.PROCESS_START.getId(), FormMappingTarget.UNDEFINED.name(), null);
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
        pageProcessDependencyResolver.deployFormMappings(businessArchiveBuilder.done(), processDefinitionId, tenantServiceAccessor);

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
        pageProcessDependencyResolver.deployFormMappings(businessArchiveBuilder.done(), processDefinitionId, tenantServiceAccessor);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, null, FormMappingType.PROCESS_START.getId(), FormMappingTarget.UNDEFINED.name(), null);
        verify(formMappingService, times(1)).create(processDefinitionId, null, FormMappingType.PROCESS_OVERVIEW.getId(), FormMappingTarget.UNDEFINED.name(),
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
        pageProcessDependencyResolver.deployFormMappings(businessArchive, processDefinitionId, tenantServiceAccessor);

        // then:
        verify(formMappingService, times(1)).create(processDefinitionId, taskName, FormMappingType.TASK.getId(), FormMappingTarget.UNDEFINED.name(), null);
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
        pageProcessDependencyResolver.deployFormMappings(businessArchive, processDefinitionId, tenantServiceAccessor);

        // then:
        verify(formMappingService, times(1)).create(eq(processDefinitionId), eq(declaredTaskName), eq(FormMappingType.TASK.getId()), anyString(), anyString());
        verify(formMappingService, times(0)).create(eq(processDefinitionId), eq(unknownTaskName), eq(FormMappingType.TASK.getId()), anyString(), anyString());
    }

}
