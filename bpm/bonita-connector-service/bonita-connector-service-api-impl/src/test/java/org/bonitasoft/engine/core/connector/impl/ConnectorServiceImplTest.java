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
package org.bonitasoft.engine.core.connector.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ResourcesService;
import org.bonitasoft.engine.resources.SBARResource;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SInvalidConnectorImplementationException;
import org.bonitasoft.engine.core.connector.parser.JarDependencies;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.SXMLParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class ConnectorServiceImplTest {

    private static final long PROCESS_DEFINITION_ID = 123153L;
    @Mock
    private BonitaHomeServer bonitaHomeServer;
    @Mock
    private Parser parser;
    @Mock
    private CacheService cacheService;
    @Mock
    private ParserFactory parserFactory;
    @Mock
    private DependencyService dependencyService;
    @Mock
    private ResourcesService resourcesService;
    @Mock
    private SConnectorImplementationDescriptor connectorImplDescriptorInCache;
    @Mock
    private ConnectorExecutor connectorExecutor;
    @Mock
    private ExpressionResolverService expressionResolverService;
    @Mock
    private OperationService operationService;
    @Mock
    private TimeTracker timeTracker;
    @Mock
    private TechnicalLoggerService technicalLoggerService;
    @Captor
    private ArgumentCaptor<SConnector> connectorArgumentCaptor;

    private ConnectorServiceImpl connectorService;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        doReturn(parser).when(parserFactory).createParser(anyList());
        connectorService = new ConnectorServiceImpl(cacheService, connectorExecutor, parserFactory, expressionResolverService,
                operationService, dependencyService,
                technicalLoggerService, timeTracker, resourcesService);
    }

    @Test(expected = SInvalidConnectorImplementationException.class)
    public void checkConnectorImplementationIsValidWithCorruptFile() throws Exception {
        connectorService.checkConnectorImplementationIsValid(new byte[] { 1, 5, 6, 87, 9, 9, 36, 1, 6, 6, 5, 3, 5, 5, 5, 64, 6, 5, 5 }, "myConnector", "1.0.0");
    }

    @Test(expected = SInvalidConnectorImplementationException.class)
    public void checkConnectorImplementationIsValidWithZipHavingNoImpl() throws Exception {
        final byte[] zip = IOUtil.zip(Collections.singletonMap("connector.notImpl", "mocked".getBytes()));
        connectorService.checkConnectorImplementationIsValid(zip, "myConnector", "1.0.0");
    }

    @Test
    public void checkConnectorImplementationIsValidWithValidZip() throws Exception {
        when(parser.getObjectFromXML(eq("mocked".getBytes()))).thenReturn(
                new SConnectorImplementationDescriptor("org.Test", "myConnector", "1.0.0", "myConnector", "1.0.0", new JarDependencies(Collections
                        .<String> emptyList())));
        final byte[] zip = IOUtil.zip(Collections.singletonMap("connector.impl", "mocked".getBytes()));
        connectorService.checkConnectorImplementationIsValid(zip, "myConnector", "1.0.0");
    }

    @Test(expected = SInvalidConnectorImplementationException.class)
    public void checkConnectorImplementationIsValidWithValidFileButWrongImpl() throws Exception {
        when(parser.getObjectFromXML(eq("mocked".getBytes()))).thenReturn(
                new SConnectorImplementationDescriptor("org.Test", "myConnector", "1.0.0", "myConnectorWrong", "1.0.0", new JarDependencies(Collections
                        .<String> emptyList())));
        final byte[] zip = IOUtil.zip(Collections.singletonMap("connector.impl", "mocked".getBytes()));
        connectorService.checkConnectorImplementationIsValid(zip, "myConnector", "1.0.0");
    }

    @Test
    public void setNewConnectorImplemCleansOldDependencies() throws Exception {
        final long processDefId = 17L;

        final SProcessDefinitionImpl sProcessDef = new SProcessDefinitionImpl("MyProcess", "1.0");
        sProcessDef.setId(processDefId);
        final String connectorDefId = "org.bonitasoft.connector.BeerConnector";
        final String connectorDefVersion = "1.0.0";
        final String connectorImplId = "org.bonitasoft.connector.HoogardenConnector";
        final String connectorImplVersion = "1.0";
        final String implementationClassName = "org.bonitasoft.engine.connectors.HoogardenBeerConnector";
        final SConnectorImplementationDescriptor hoogardenConnectorDescriptor = new SConnectorImplementationDescriptor(implementationClassName,
                connectorImplId,
                connectorImplVersion, connectorDefId, connectorDefVersion, new JarDependencies(Arrays.asList("some1.jar", "HoogardenConnector.jar")));
        final SConnectorImplementationDescriptor oldConnectorDescriptor = new SConnectorImplementationDescriptor(implementationClassName, connectorImplId,
                connectorImplVersion, connectorDefId, connectorDefVersion, new JarDependencies(Collections.singletonList("file.jar")));

        Map<String, byte[]> zipFileMap = new HashMap<>(3);
        final byte[] implBytes = "tototo".getBytes();
        zipFileMap.put("HoogardenBeerConnector.impl", implBytes);
        final byte[] dep1Bytes = { 12, 94, 14, 12 };
        zipFileMap.put("some1.jar", dep1Bytes);
        final byte[] hoogardenConnectorBytes = { 12, 94, 14, 9, 54, 65, 98, 54, 21, 32, 65 };
        zipFileMap.put("HoogardenConnector.jar", hoogardenConnectorBytes);
        final byte[] zip1 = IOUtil.zip(zipFileMap);
        when(parser.getObjectFromXML(eq(new byte[] { 2 }))).thenReturn(oldConnectorDescriptor);
        when(parser.getObjectFromXML(eq(implBytes))).thenReturn(hoogardenConnectorDescriptor);

        final SBARResource originalConnector = new SBARResource("file.impl", BARResourceType.CONNECTOR, processDefId, new byte[] { 2 });
        doReturn(Collections.singletonList(originalConnector)).when(resourcesService)
                .get(eq(processDefId), eq(BARResourceType.CONNECTOR), eq(0), anyInt());
        SDependency dependency = mock(SDependency.class);
        doReturn(dependency).when(dependencyService).getDependencyOfArtifact(processDefId, ScopeType.PROCESS, "file.jar");

        connectorService.setConnectorImplementation(sProcessDef, connectorDefId, connectorDefVersion, zip1);
        verify(dependencyService).createMappedDependency("HoogardenConnector.jar", hoogardenConnectorBytes, "HoogardenConnector.jar", processDefId,
                ScopeType.PROCESS);
        verify(dependencyService).createMappedDependency("some1.jar", dep1Bytes, "some1.jar", processDefId, ScopeType.PROCESS);
        verify(resourcesService).add(processDefId, "HoogardenBeerConnector.impl", BARResourceType.CONNECTOR, implBytes);
        verify(dependencyService).deleteDependency(dependency);
        verify(resourcesService).remove(originalConnector);
    }

    @Test
    public void setNewConnectorImplemShouldIgnoreSourceFiles() throws Exception {
        final long processDefId = 1324565477444L;

        Map<String, byte[]> zipFileMap = new HashMap<>(1);
        zipFileMap.put("src/net/company/MyImplem.java", "some Java source file content".getBytes());
        final byte[] zip = IOUtil.zip(zipFileMap);

        connectorService.extractConnectorImplementation(zip);

        verify(resourcesService, times(0)).add(eq(processDefId), anyString(), any(BARResourceType.class), any(byte[].class));
    }

    @Test
    public void getConnectorImplementationShouldReadFileWhenCacheIsVoid() throws Exception {
        checkGetConnectorImplementationUsesCache(0, 1, true);
    }

    @Test
    public void getConnectorImplementationShouldReadFileWhenCacheIsNotEmpty() throws Exception {
        checkGetConnectorImplementationUsesCache(1, 0, true);
    }

    @Test
    public void getConnectorImplementationShouldReadFileWhenCacheDoesNotContainsConnector() throws Exception {
        checkGetConnectorImplementationUsesCache(1, 1, false);
    }

    @Test
    public void getConnectorImplementationShouldNoteReadFileWhenCacheContainsConnector() throws Exception {
        checkGetConnectorImplementationUsesCache(1, 0, true);
    }

    @Test
    public void should_executeConnector_call_connector_executor() throws Exception {
        //given
        SConnectorImplementationDescriptor connectorImplementationDescriptor = new SConnectorImplementationDescriptor(MyTestConnector.class.getName(), "implId",
                "impplVersion", "defId", "defVersion", new JarDependencies(Collections.<String> emptyList()));
        SConnectorInstance connectorInstance = mock(SConnectorInstance.class);

        //when
        Map<String, Object> inputParameters = Collections.<String, Object> singletonMap("key", "value");
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        connectorService.executeConnector(PROCESS_DEFINITION_ID, connectorInstance, connectorImplementationDescriptor, contextClassLoader, inputParameters);
        //then
        verify(connectorExecutor).execute(connectorArgumentCaptor.capture(), eq(inputParameters), eq(contextClassLoader));
        SConnector sConnector = connectorArgumentCaptor.getValue();
        assertThat(sConnector).isInstanceOf(SConnectorAdapter.class);
        assertThat(((SConnectorAdapter) sConnector).getConnector()).isInstanceOf(MyTestConnector.class);
    }

    private void checkGetConnectorImplementationUsesCache(final int givenCacheSizeToBeReturned, final int expectedNumberOfCacheStoreInvocations,
            final boolean shouldCacheContainsConnectorImplementation)
                    throws BonitaHomeNotSetException, SXMLParseException,
                    IOException, SConnectorException, SInvalidConnectorImplementationException, SCacheException, SBonitaReadException {

        final long processDefId = 17L;
        final SProcessDefinition sProcessDef;
        sProcessDef = mock(SProcessDefinition.class);

        when(sProcessDef.getId()).thenReturn(processDefId);
        final String connectorDefId = "org.bonitasoft.connector.BeerConnector";
        final String connectorDefVersion = "1.0.0";
        final String connectorImplId = "org.bonitasoft.connector.HoogardenConnector";
        final String connectorImplVersion = "1.0";
        final String implementationClassName = "org.bonitasoft.engine.connectors.HoogardenBeerConnector";
        final SConnectorImplementationDescriptor connectorImplDescriptor = new SConnectorImplementationDescriptor(implementationClassName, connectorImplId,
                connectorImplVersion, connectorDefId, connectorDefVersion, new JarDependencies(Arrays.asList("some1.jar", "HoogardenConnector.jar")));
        when(parser.getObjectFromXML(eq("tototo".getBytes()))).thenReturn(connectorImplDescriptor);

        final Map<String, byte[]> zipFileMap = new HashMap<>(3);

        zipFileMap.put("HoogardenBeerConnector.impl", "tototo".getBytes());
        zipFileMap.put("some1.jar", new byte[] { 12, 94, 14, 12 });
        zipFileMap.put("HoogardenConnector.jar", new byte[] { 12, 94, 14, 9, 54, 65, 98, 54, 21, 32, 65 });
        final byte[] zip1 = IOUtil.zip(zipFileMap);

        doReturn(Collections.singletonList(new SBARResource("HoogardenBeerConnector.impl", BARResourceType.CONNECTOR, processDefId, "tototo".getBytes())))
                .when(resourcesService).get(eq(processDefId), eq(BARResourceType.CONNECTOR), eq(0), anyInt());

        //setConnectorImplementation store to cache
        connectorService.setConnectorImplementation(sProcessDef, connectorDefId, connectorDefVersion, zip1);

        //given
        doReturn(givenCacheSizeToBeReturned).when(cacheService).getCacheSize(ConnectorServiceImpl.CONNECTOR_CACHE_NAME);

        List<String> cacheContentKeys = Collections.emptyList();
        final String buildConnectorImplementationKey = connectorService
                .buildConnectorImplementationKey(processDefId, connectorImplId, connectorImplVersion);
        if (shouldCacheContainsConnectorImplementation) {
            cacheContentKeys = Collections.singletonList(buildConnectorImplementationKey);
        }
        doReturn(cacheContentKeys).when(cacheService).getKeys(ConnectorServiceImpl.CONNECTOR_CACHE_NAME);
        doReturn(connectorImplDescriptor).when(cacheService).get(ConnectorServiceImpl.CONNECTOR_CACHE_NAME, buildConnectorImplementationKey);

        //when
        connectorService.getConnectorImplementations(processDefId, 0,
                10, "", OrderByType.ASC);

        //then
        verify(cacheService, times(expectedNumberOfCacheStoreInvocations + 1)).store(anyString(), any(Serializable.class), any(Object.class));

    }

    public static class MyTestConnector extends AbstractConnector {

        @Override
        public void validateInputParameters() throws ConnectorValidationException {

        }

        @Override
        protected void executeBusinessLogic() throws ConnectorException {

        }
    }

    @Test
    public void getNumberOfConnectorImplementations_should_call_count_on_resource_service() throws Exception {
        final long processDefinitionId = 451L;
        final long expectedCount = 11L;
        doReturn(expectedCount).when(resourcesService).count(processDefinitionId, BARResourceType.CONNECTOR);

        final Long numberOfConnectorImplementations = connectorService.getNumberOfConnectorImplementations(processDefinitionId);

        assertThat(numberOfConnectorImplementations).isEqualTo(expectedCount);
    }
}
