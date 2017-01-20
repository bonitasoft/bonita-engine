package org.bonitasoft.engine.core.filter.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.userfilter.impl.UserFilterDefinitionImpl;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.filter.exception.SUserFilterExecutionException;
import org.bonitasoft.engine.core.filter.model.JarDependencies;
import org.bonitasoft.engine.core.filter.model.UserFilterImplementationDescriptor;
import org.bonitasoft.engine.core.process.definition.model.impl.SUserFilterDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.resources.SBARResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Before
    public void setup() {
        sUserFilterDefinition = new SUserFilterDefinitionImpl(new UserFilterDefinitionImpl("UserFiler", "filterId", "version"));
        userFilterImplementationDescriptor = new UserFilterImplementationDescriptor(MyUserFilter.class.getName(), "id", "version", "filterId", "version",
                new JarDependencies(Collections.singletonList("dep.jar")));
    }

    @Test
    public void executeFilter_should_work_for_existing_descriptor() throws Exception {
        doReturn(userFilterImplementationDescriptor).when(cacheService).get(eq("USER_FILTER"), eq("" + PROCESS_DEFINITION_ID + ":filterId-version"));

        userFilterService.executeFilter(PROCESS_DEFINITION_ID, sUserFilterDefinition, Collections.<String, SExpression> emptyMap(),
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
