package org.bonitasoft.engine.api.impl.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.form.impl.SFormMappingImpl;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PageProcessDependencyResolverTest {

    public static final long PROCESS_DEFINITION_ID = 1L;
    public static final String PAGE = "myPage";

    private PageProcessDependencyResolver pageProcessDependencyResolver;

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

    @Before
    public void before() {
        pageProcessDependencyResolver = new PageProcessDependencyResolver();
        formMappings = new ArrayList<>();

        doReturn(pageService).when(tenantServiceAccessor).getPageService();
        doReturn(formMappingService).when(tenantServiceAccessor).getFormMappingService();

        doReturn(PROCESS_DEFINITION_ID).when(sDefinition).getId();
    }

    @Test
    public void should_retreive_custom_page_in_form_mapping() throws Exception {
        //given
        formMappings.add(new SFormMappingImpl(PROCESS_DEFINITION_ID, "task", PAGE, FormMappingTarget.INTERNAL.name(), FormMappingType.PROCESS_OVERVIEW.name()));
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doReturn(null).when(pageService).getPageByNameAndProcessDefinitionId(PAGE, PROCESS_DEFINITION_ID);

        //when then
        assertThat(pageProcessDependencyResolver.resolve(tenantServiceAccessor, businessArchive, sDefinition)).as("missing page should not block resolve step")
                .isTrue();
    }

    @Test
    public void should_check_resolution_report_problem_when_page_is_missing() throws Exception {
        //given
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, "task", PAGE, FormMappingTarget.INTERNAL.name(), FormMappingType.PROCESS_OVERVIEW.name());
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doReturn(null).when(pageService).getPageByNameAndProcessDefinitionId(PAGE, PROCESS_DEFINITION_ID);

        //when
        final List<Problem> problems = pageProcessDependencyResolver.checkResolution(tenantServiceAccessor, sDefinition);

        // then
        assertThat(problems).as("should return a problem").hasSize(1);
        ProblemAssert.assertThat(problems.get(0)).hasDescription(String.format(PageProcessDependencyResolver.ERROR_MESSAGE, sFormMapping))
                .hasLevel(Problem.Level.ERROR);

    }

    @Test
    public void should_format_message_when_form_mapping_page_is_null() throws Exception {
        //given
        final SFormMappingImpl sFormMapping = new SFormMappingImpl(PROCESS_DEFINITION_ID, "task", null, FormMappingTarget.INTERNAL.name(), FormMappingType.PROCESS_OVERVIEW.name());
        formMappings.add(sFormMapping);
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());

        //when
        final List<Problem> problems = pageProcessDependencyResolver.checkResolution(tenantServiceAccessor, sDefinition);

        // then
        assertThat(problems).as("should return a problem").hasSize(1);
        ProblemAssert.assertThat(problems.get(0)).hasDescription(String.format(PageProcessDependencyResolver.ERROR_MESSAGE, sFormMapping))
                .hasLevel(Problem.Level.ERROR);

    }

    @Test
    public void should_check_resolution_throw_exception() throws Exception {
        //given
        formMappings.add(new SFormMappingImpl(PROCESS_DEFINITION_ID, "task", PAGE, FormMappingTarget.INTERNAL.name(), FormMappingType.PROCESS_OVERVIEW.name()));
        doReturn(formMappings).when(formMappingService).list(eq(PROCESS_DEFINITION_ID), anyInt(), anyInt());
        doThrow(SBonitaReadException.class).when(pageService).getPageByNameAndProcessDefinitionId(PAGE, PROCESS_DEFINITION_ID);

        //when
        final List<Problem> problems = pageProcessDependencyResolver.checkResolution(tenantServiceAccessor, sDefinition);

        // then
        assertThat(problems).as("should return a problem").hasSize(1);

    }

}
