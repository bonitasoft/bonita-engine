package org.bonitasoft.engine.core.filter.impl;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.bpm.userfilter.impl.UserFilterDefinitionImpl;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.filter.JarDependencies;
import org.bonitasoft.engine.core.filter.UserFilterImplementationDescriptor;
import org.bonitasoft.engine.core.filter.exception.SUserFilterExecutionException;
import org.bonitasoft.engine.core.process.definition.model.impl.SUserFilterDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.filter.AbstractUserFilter;
import org.bonitasoft.engine.filter.UserFilterException;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class UserFilterServiceImplTest {

    public static final long PROCESS_DEFINITION_ID = 123456l;
    @Mock
    private Parser parser;
    @Mock
    private ParserFactory parserFactory;
    @Mock
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

    @Before
    public void setup() {
        doReturn(parser).when(parserFactory).createParser(anyList());
        userFilterService = new UserFilterServiceImpl(connectorExecutor, cacheService, expressionResolverService, parserFactory, logger, resourceService);
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
