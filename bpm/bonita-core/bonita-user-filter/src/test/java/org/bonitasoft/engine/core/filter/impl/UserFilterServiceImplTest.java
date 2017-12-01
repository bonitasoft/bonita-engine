package org.bonitasoft.engine.core.filter.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.bpm.userfilter.impl.UserFilterDefinitionImpl;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.filter.exception.SUserFilterExecutionException;
import org.bonitasoft.engine.core.filter.model.JarDependencies;
import org.bonitasoft.engine.core.filter.model.UserFilterImplementationDescriptor;
import org.bonitasoft.engine.core.process.definition.model.impl.SUserFilterDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.resources.SBARResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class UserFilterServiceImplTest {

    public static final long PROCESS_DEFINITION_ID = 123456L;
    @InjectMocks
    private UserFilterServiceImpl userFilterService;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private ConnectorExecutor connectorExecutor;
    @Mock
    private CacheService cacheService;
    @Mock
    private ExpressionResolverService expressionResolverService;
    @Mock
    private ProcessResourcesService resourceService;
    private SUserFilterDefinitionImpl sUserFilterDefinition;
    private UserFilterImplementationDescriptor userFilterImplementationDescriptor;

    @Captor
    private ArgumentCaptor<UserFilterImplementationDescriptor> userFilterImplementationDescriptorArgumentCaptor;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        sUserFilterDefinition = new SUserFilterDefinitionImpl(new UserFilterDefinitionImpl("UserFiler", "filterId", "version"));
        userFilterImplementationDescriptor = new UserFilterImplementationDescriptor(MyUserFilter.class.getName(), "id", "version", "filterId", "version",
                new JarDependencies(Collections.singletonList("dep.jar")));
    }

    @Test
    public void executeFilter_should_work_for_existing_descriptor() throws Exception {
        doReturn(userFilterImplementationDescriptor).when(cacheService).get(eq("USER_FILTER"), eq("" + PROCESS_DEFINITION_ID + ":filterId-version"));

        userFilterService.executeFilter(PROCESS_DEFINITION_ID, sUserFilterDefinition, Collections.emptyMap(),
                new URLClassLoader(new URL[] {}, Thread.currentThread().getContextClassLoader()), new SExpressionContext(), "actorName");
    }

    @Test(expected = SUserFilterExecutionException.class)
    public void executeFilter_should_fail_for_non_existing_descriptor() throws Exception {
        userFilterService.executeFilter(PROCESS_DEFINITION_ID, sUserFilterDefinition, Collections.<String, SExpression> emptyMap(),
                new URLClassLoader(new URL[] {}, Thread.currentThread().getContextClassLoader()), new SExpressionContext(), "actorName");
    }

    @Test
    public void loadUserFilters_should_load_from_resourceService() throws Exception {
        userFilterService.loadUserFilters(PROCESS_DEFINITION_ID);

        verify(resourceService).get(eq(PROCESS_DEFINITION_ID), eq(BARResourceType.USER_FILTER), anyInt(), anyInt());
    }

    @Test
    public void should_parse_user_filter_implementation_file_and_cache_it_when_loading_userfilters() throws Exception {
        //given
        byte[] userFilterImplContent = ("<connectorImplementation>\n" +
                "\n" +
                "\t<definitionId>user-filter-def</definitionId>\n" +
                "\t<definitionVersion>1.0</definitionVersion>\n" +
                "\t<implementationClassname>org.bonitasoft.user.filter.TestUserFilter</implementationClassname>\n" +
                "\t<implementationId>user-filter-impl</implementationId>\n" +
                "\t<implementationVersion>1.0</implementationVersion>\n" +
                "\n" +
                "\t<jarDependencies>\n" +
                "\t\t<jarDependency>UserFilterDependency.jar</jarDependency>\n" +
                "\t</jarDependencies>\n" +
                "</connectorImplementation>\n").getBytes();
        doReturn(Collections.singletonList(new SBARResource("my-user-filter.impl", BARResourceType.USER_FILTER, PROCESS_DEFINITION_ID, userFilterImplContent)))
                .when(resourceService).get(eq(PROCESS_DEFINITION_ID), eq(BARResourceType.USER_FILTER), anyInt(), anyInt());
        //when
        userFilterService.loadUserFilters(PROCESS_DEFINITION_ID);

        //then
        verify(cacheService).store(eq("USER_FILTER"), eq(PROCESS_DEFINITION_ID + ":user-filter-def-1.0"),
                userFilterImplementationDescriptorArgumentCaptor.capture());
        UserFilterImplementationDescriptor userFilterImplementationDescriptor = userFilterImplementationDescriptorArgumentCaptor.getValue();
        assertThat(userFilterImplementationDescriptor.getDefinitionId()).isEqualTo("user-filter-def");
        assertThat(userFilterImplementationDescriptor.getDefinitionVersion()).isEqualTo("1.0");
        assertThat(userFilterImplementationDescriptor.getImplementationClassName()).isEqualTo("org.bonitasoft.user.filter.TestUserFilter");
        assertThat(userFilterImplementationDescriptor.getId()).isEqualTo("user-filter-impl");
        assertThat(userFilterImplementationDescriptor.getVersion()).isEqualTo("1.0");
        assertThat(userFilterImplementationDescriptor.getJarDependencies().getDependencies()).containsOnly("UserFilterDependency.jar");

    }

    public void executeFilter_should_throw_a_SUserFilterExecutionException_when_receiving_SConnectorException_with_null_cause() throws Exception {

        //given
        UserFilterServiceImpl spyUserFilterService = spy(
                new UserFilterServiceImpl(connectorExecutor, cacheService, expressionResolverService, logger, resourceService));
        doReturn(userFilterImplementationDescriptor).when(cacheService).get(eq("USER_FILTER"), eq("" + PROCESS_DEFINITION_ID + ":filterId-version"));
        doThrow(new SConnectorException("Test exception")).when(spyUserFilterService).executeFilterInClassloader(anyString(), anyMap(), (URLClassLoader) any(),
                (SExpressionContext) any(), anyString());
        //then
        expectedException.expect(SUserFilterExecutionException.class);
        expectedException.expectMessage("Test exception");
        //when
        spyUserFilterService.executeFilter(PROCESS_DEFINITION_ID, sUserFilterDefinition, Collections.<String, SExpression> emptyMap(),
                new URLClassLoader(new URL[] {}, Thread.currentThread().getContextClassLoader()), new SExpressionContext(), "actorName");

    }

    @Test
    public void executeFilter_should_throw_a_SUserFilterExecutionException_when_receiving_SConnectorException_with_nonNull_cause() throws Exception {

        //given
        SConnectorException theException = mock(SConnectorException.class);
        UserFilterServiceImpl spyUserFilterService = spy(
                new UserFilterServiceImpl(connectorExecutor, cacheService, expressionResolverService, logger, resourceService));
        doReturn(userFilterImplementationDescriptor).when(cacheService).get(eq("USER_FILTER"), eq("" + PROCESS_DEFINITION_ID + ":filterId-version"));
        when(theException.getCause()).thenReturn(new RuntimeException(" The root cause"));
        doThrow(theException).when(spyUserFilterService).executeFilterInClassloader(anyString(), anyMap(), (URLClassLoader) any(), (SExpressionContext) any(),
                anyString());

        //then
        expectedException.expect(SUserFilterExecutionException.class);
        expectedException.expectMessage("The root cause");
        //when
        spyUserFilterService.executeFilter(PROCESS_DEFINITION_ID, sUserFilterDefinition, Collections.<String, SExpression> emptyMap(),
                new URLClassLoader(new URL[] {}, Thread.currentThread().getContextClassLoader()), new SExpressionContext(), "actorName");
    }

    @Test
    public void executeFilter_should_throw_a_SUserFilterExecutionException_when_receiving_SConnectorException_in_debug_mode() throws Exception {

        //given
        SConnectorException theException = mock(SConnectorException.class);
        UserFilterServiceImpl spyUserFilterService = spy(
                new UserFilterServiceImpl(connectorExecutor, cacheService, expressionResolverService, logger, resourceService));
        doReturn(userFilterImplementationDescriptor).when(cacheService).get(eq("USER_FILTER"), eq("" + PROCESS_DEFINITION_ID + ":filterId-version"));
        when(theException.getCause()).thenReturn(new RuntimeException(" The root cause"));
        doThrow(theException).when(spyUserFilterService).executeFilterInClassloader(anyString(), anyMap(), (URLClassLoader) any(), (SExpressionContext) any(),
                anyString());
        when(logger.isLoggable((Class) any(), eq(TechnicalLogSeverity.DEBUG))).thenReturn(true);

        //then
        expectedException.expect(SUserFilterExecutionException.class);
        expectedException.expectMessage("Current Thread ID : <");
        //when
        spyUserFilterService.executeFilter(PROCESS_DEFINITION_ID, sUserFilterDefinition, Collections.<String, SExpression> emptyMap(),
                new URLClassLoader(new URL[] {}, Thread.currentThread().getContextClassLoader()), new SExpressionContext(), "actorName");
    }

    @Test
    public void buildDebugMessage_should_contain_all_the_debug_info() {

        //when
        String result = userFilterService.buildDebugMessage(45L, sUserFilterDefinition, new HashMap<String, SExpression>(),
                new URLClassLoader(new URL[] {}, Thread.currentThread().getContextClassLoader()), new SExpressionContext(), "an actor",
                "an implementation class name", userFilterImplementationDescriptor);
        //then
        assertThat(result).contains("an actor");
        assertThat(result).contains("an implementation class name");
        assertThat(result).contains("45");
        assertThat(result).contains(sUserFilterDefinition.toString());
        assertThat(result).contains(userFilterImplementationDescriptor.toString());
        assertThat(result).contains("RUNNABLE");
    }

    public static class MyUserFilter extends AbstractUserFilter {

        @Override
        public void validateInputParameters() throws ConnectorValidationException {

        }

        @Override
        public List<Long> filter(String actorName) throws UserFilterException {
            return null;
        }
    }
}
