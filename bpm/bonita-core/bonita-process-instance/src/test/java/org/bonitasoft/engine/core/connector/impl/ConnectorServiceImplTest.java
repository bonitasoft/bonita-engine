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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.notNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SInvalidConnectorImplementationException;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.impl.SDependencyImpl;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.resources.BARResourceType;
import org.bonitasoft.engine.resources.ProcessResourcesService;
import org.bonitasoft.engine.resources.SBARResource;
import org.bonitasoft.engine.tracking.TimeTracker;
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
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class ConnectorServiceImplTest {

    private static final long PROCESS_DEFINITION_ID = 123153L;
    private SProcessDefinitionImpl processDefinition;
    @Mock
    private BonitaHomeServer bonitaHomeServer;
    @Mock
    private CacheService cacheService;
    @Mock
    private DependencyService dependencyService;
    @Mock
    private ProcessResourcesService processResourcesService;
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
    @Captor
    ArgumentCaptor<SDependency> dependencyArgumentCaptor;
    @Captor
    ArgumentCaptor<SBARResource> sBarResourceArgumentCaptor;
    @InjectMocks
    private ConnectorServiceImpl connectorService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        processDefinition = new SProcessDefinitionImpl("proc", "1");
        processDefinition.setId(PROCESS_DEFINITION_ID);
    }

    @Test(expected = SInvalidConnectorImplementationException.class)
    public void setConnectorImplementationWithCorruptFile() throws Exception {
        connectorService.setConnectorImplementation(processDefinition, "myConnector", "1.0.0",
                new byte[] { 1, 5, 6, 87, 9, 9, 36, 1, 6, 6, 5, 3, 5, 5, 5, 64, 6, 5, 5 });
    }

    @Test(expected = SInvalidConnectorImplementationException.class)
    public void setConnectorImplementationZipHavingNoImpl() throws Exception {
        final byte[] zip = IOUtil.zip(Collections.singletonMap("connector.notImpl", "mocked".getBytes()));
        connectorService.setConnectorImplementation(processDefinition, "myConnector", "1.0.0", zip);
    }

    @Test(expected = SInvalidConnectorImplementationException.class)
    public void setConnectorImplementationValidFileButWrongImpl() throws Exception {
        final byte[] zip = IOUtil.zip(Collections.singletonMap("connector.impl", "mocked".getBytes()));
        connectorService.setConnectorImplementation(processDefinition, "myConnector", "1.0.0", zip);
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
                connectorImplVersion, connectorDefId, connectorDefVersion, new ArrayList<>(Arrays.asList("some1.jar", "HoogardenConnector.jar")));
        final SConnectorImplementationDescriptor oldConnectorDescriptor = new SConnectorImplementationDescriptor(implementationClassName, connectorImplId,
                connectorImplVersion, connectorDefId, connectorDefVersion, new ArrayList<>(Collections.singletonList("file.jar")));

        Map<String, byte[]> zipFileMap = new HashMap<>(3);
        byte[] connectorImplFile = createConnectorImplFile(hoogardenConnectorDescriptor);
        zipFileMap.put("HoogardenBeerConnector.impl", connectorImplFile);
        final byte[] dep1Bytes = { 12, 94, 14, 12 };
        zipFileMap.put("some1.jar", dep1Bytes);
        final byte[] hoogardenConnectorBytes = { 12, 94, 14, 9, 54, 65, 98, 54, 21, 32, 65 };
        zipFileMap.put("HoogardenConnector.jar", hoogardenConnectorBytes);
        final byte[] zip1 = IOUtil.zip(zipFileMap);

        final SBARResource originalConnector = new SBARResource("file.impl", BARResourceType.CONNECTOR, processDefId,
                createConnectorImplFile(oldConnectorDescriptor));
        doReturn(Collections.singletonList(originalConnector)).when(processResourcesService)
                .get(eq(processDefId), eq(BARResourceType.CONNECTOR), eq(0), anyInt());
        SDependency dependency = mock(SDependency.class);
        doReturn(dependency).when(dependencyService).getDependencyOfArtifact(processDefId, ScopeType.PROCESS, "file.jar");

        connectorService.setConnectorImplementation(sProcessDef, connectorDefId, connectorDefVersion, zip1);
        verify(dependencyService).createMappedDependency("HoogardenConnector.jar", hoogardenConnectorBytes, "HoogardenConnector.jar", processDefId,
                ScopeType.PROCESS);
        verify(dependencyService).createMappedDependency("some1.jar", dep1Bytes, "some1.jar", processDefId, ScopeType.PROCESS);
        verify(processResourcesService).add(processDefId, "HoogardenBeerConnector.impl", BARResourceType.CONNECTOR, connectorImplFile);
        verify(dependencyService).deleteDependency(dependency);
        verify(processResourcesService).remove(originalConnector);
    }

    @Test
    public void setNewConnectorImplemShouldIgnoreSourceFiles() throws Exception {
        final long processDefId = 1324565477444L;

        Map<String, byte[]> zipFileMap = new HashMap<>(1);
        zipFileMap.put("src/net/company/MyImplem.java", "some Java source file content".getBytes());
        zipFileMap.put("connector.impl", "thecontent".getBytes());
        final byte[] zip = IOUtil.zip(zipFileMap);

        connectorService.extractConnectorImplementation(zip);

        verify(processResourcesService, times(0)).add(eq(processDefId), anyString(), any(BARResourceType.class), any(byte[].class));
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
                "impplVersion", "defId", "defVersion", new ArrayList<>(Collections.<String> emptyList()));
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
            throws Exception {

        final String connectorDefId = "org.bonitasoft.connector.BeerConnector";
        final String connectorDefVersion = "1.0.0";
        final String connectorImplId = "org.bonitasoft.connector.HoogardenConnector";
        final String connectorImplVersion = "1.0";
        final String implementationClassName = "org.bonitasoft.engine.connectors.HoogardenBeerConnector";
        final SConnectorImplementationDescriptor connectorImplDescriptor = new SConnectorImplementationDescriptor(implementationClassName, connectorImplId,
                connectorImplVersion, connectorDefId, connectorDefVersion, new ArrayList<>(Arrays.asList("some1.jar", "HoogardenConnector.jar")));
        byte[] connectorImplFile = createConnectorImplFile(connectorImplDescriptor);

        final Map<String, byte[]> zipFileMap = new HashMap<>(3);

        zipFileMap.put("HoogardenBeerConnector.impl", connectorImplFile);
        zipFileMap.put("some1.jar", new byte[] { 12, 94, 14, 12 });
        zipFileMap.put("HoogardenConnector.jar", new byte[] { 12, 94, 14, 9, 54, 65, 98, 54, 21, 32, 65 });
        final byte[] zip1 = IOUtil.zip(zipFileMap);

        doReturn(Collections
                .singletonList(new SBARResource("HoogardenBeerConnector.impl", BARResourceType.CONNECTOR, processDefinition.getId(),
                        connectorImplFile)))
                                .when(processResourcesService).get(eq(processDefinition.getId()), eq(BARResourceType.CONNECTOR), eq(0), anyInt());

        //setConnectorImplementation store to cache
        connectorService.setConnectorImplementation(processDefinition, connectorDefId, connectorDefVersion, zip1);

        //given
        doReturn(givenCacheSizeToBeReturned).when(cacheService).getCacheSize(ConnectorServiceImpl.CONNECTOR_CACHE_NAME);

        List<String> cacheContentKeys = Collections.emptyList();
        final String buildConnectorImplementationKey = connectorService
                .buildConnectorImplementationKey(processDefinition.getId(), connectorImplId, connectorImplVersion);
        if (shouldCacheContainsConnectorImplementation) {
            cacheContentKeys = Collections.singletonList(buildConnectorImplementationKey);
        }
        doReturn(cacheContentKeys).when(cacheService).getKeys(ConnectorServiceImpl.CONNECTOR_CACHE_NAME);
        doReturn(connectorImplDescriptor).when(cacheService).get(ConnectorServiceImpl.CONNECTOR_CACHE_NAME, buildConnectorImplementationKey);

        //when
        connectorService.getConnectorImplementations(processDefinition.getId(), 0,
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
        doReturn(expectedCount).when(processResourcesService).count(processDefinitionId, BARResourceType.CONNECTOR);

        final Long numberOfConnectorImplementations = connectorService.getNumberOfConnectorImplementations(processDefinitionId);

        assertThat(numberOfConnectorImplementations).isEqualTo(expectedCount);
    }

    @Test
    public void should_read_connector_archive_correctly() throws Exception {
        //given
        String connectorImplFileName = "myConnector.impl";
        HashMap<String, byte[]> files = new HashMap<>();
        byte[] connectorImplFileContent = ("<connectorImplementation>\n" +
                "\n" +
                "\t<definitionId>org.bonitasoft.connector.testConnectorWithOutput</definitionId>\n" +
                "\t<definitionVersion>1.0</definitionVersion>\n" +
                "\t<implementationClassname>org.bonitasoft.engine.connectors.TestConnectorWithModifiedOutput</implementationClassname>\n" +
                "\t<implementationId>org.bonitasoft.connector.testConnectorWithModifiedOutput</implementationId>\n" +
                "\t<implementationVersion>1.0</implementationVersion>\n" +
                "\n" +
                "\t<jarDependencies>\n" +
                "\t\t<jarDependency>TestConnectorWithModifiedOutput.jar</jarDependency>\n" +
                "\t</jarDependencies>\n" +
                "</connectorImplementation>\n").getBytes();
        files.put(connectorImplFileName, connectorImplFileContent);
        files.put("classpath/jar1.jar", new byte[] { 1, 2, 3 });
        files.put("classpath/jar2.jar", new byte[] { 1, 2, 4 });
        byte[] zip = IOUtil.zip(files);
        //when
        ConnectorArchive connectorArchive = connectorService.extractConnectorImplementation(zip);
        //then
        assertThat(connectorArchive.getConnectorImplName()).isEqualTo(connectorImplFileName);
        assertThat(connectorArchive.getConnectorImplContent()).isEqualTo(connectorImplFileContent);
        assertThat(connectorArchive.getDependencies()).containsKeys("jar1.jar", "jar2.jar").hasSize(2);
        assertThat(connectorArchive.getDependencies().get("jar1.jar")).isEqualTo(new byte[] { 1, 2, 3 });
        assertThat(connectorArchive.getDependencies().get("jar2.jar")).isEqualTo(new byte[] { 1, 2, 4 });
    }

    @Test
    public void should_setConnectorImplementation_delete_and_create_dependency() throws Exception {
        //given
        byte[] zip = createConnectorArchiveZip("myConnector2.impl", "connectorId", "connectorVersion", new BarResource("jar3.jar", new byte[] { 3 }),
                new BarResource("jar4.jar", new byte[] { 4 }));
        havingConnector(processDefinition, "myConnector1.impl", "connectorId", "connectorVersion", new BarResource("jar1.jar", new byte[] { 1 }),
                new BarResource("jar2.jar", new byte[] { 2 }));
        //when
        connectorService.setConnectorImplementation(processDefinition, "connectorId", "connectorVersion", zip);
        //then
        verify(dependencyService, times(2)).deleteDependency(dependencyArgumentCaptor.capture());
        assertThat(dependencyArgumentCaptor.getAllValues()).extracting("fileName").containsOnly("jar1.jar", "jar2.jar");
        verify(dependencyService).createMappedDependency("jar3.jar", new byte[] { 3 }, "jar3.jar", processDefinition.getId(), ScopeType.PROCESS);
        verify(dependencyService).createMappedDependency("jar4.jar", new byte[] { 4 }, "jar4.jar", processDefinition.getId(), ScopeType.PROCESS);
    }

    @Test
    public void should_setConnectorImplementation_replace_jar_even_if_jar_was_not_in_jarDependencies() throws Exception {
        //given
        byte[] zip = createConnectorArchiveZip("myConnector1.impl", "connectorId", "connectorVersion", new BarResource("jar2.jar", new byte[] { 3 }));
        //a process with an inconsistent connector implementation is deployed: connector have a dependency names jar2.jar and jar1.jar but they are not declared in jarDependencies
        doReturn(Collections.singletonList(
                new SBARResource("myConnector1.impl", BARResourceType.CONNECTOR, processDefinition.getId(),
                        createConnectorImplFile("connectorId", "connectorVersion"))))
                                .when(processResourcesService)
                                .get(eq(processDefinition.getId()), eq(BARResourceType.CONNECTOR), anyInt(), anyInt());
        doReturn(new SDependencyImpl("jar2.jar", "jar2.jar", new byte[] { 2 })).when(dependencyService)
                .getDependencyOfArtifact(processDefinition.getId(), ScopeType.PROCESS, "jar2.jar");
        //when
        connectorService.setConnectorImplementation(processDefinition, "connectorId", "connectorVersion", zip);
        //then
        verify(dependencyService, never()).createMappedDependency(anyString(), any(byte[].class), anyString(), anyLong(), any(ScopeType.class));
        verify(dependencyService, never()).deleteDependency(any(SDependency.class));
        verify(dependencyService).updateDependencyOfArtifact("jar2.jar", new byte[] { 3 }, "jar2.jar", processDefinition.getId(), ScopeType.PROCESS);
        verify(technicalLoggerService).log(ConnectorServiceImpl.class, TechnicalLogSeverity.WARNING,
                "Updating a dependency of the connector connectorId in version connectorVersion of process definition 123153. " +
                        "The jar file jar2.jar was not declared in the previous connector implementation but is in the dependencies of the process. " +
                        "The jar is still updated but this can lead to inconsistencies.");
    }

    @Test
    public void should_setConnectorImplementation_update_existing_dependencies() throws Exception {
        //given
        byte[] zip = createConnectorArchiveZip("myConnector2.impl", "connectorId", "connectorVersion", new BarResource("jar2.jar", new byte[] { 3 }),
                new BarResource("jar4.jar", new byte[] { 4 }));
        havingConnector(processDefinition, "myConnector1.impl", "connectorId", "connectorVersion", new BarResource("jar1.jar", new byte[] { 1 }),
                new BarResource("jar2.jar", new byte[] { 2 }));
        //when
        connectorService.setConnectorImplementation(processDefinition, "connectorId", "connectorVersion", zip);
        //then
        verify(dependencyService, times(1)).deleteDependency(dependencyArgumentCaptor.capture());
        assertThat(dependencyArgumentCaptor.getAllValues()).extracting("fileName").containsOnly("jar1.jar");
        verify(dependencyService).createMappedDependency("jar4.jar", new byte[] { 4 }, "jar4.jar", processDefinition.getId(), ScopeType.PROCESS);
        verify(dependencyService).updateDependencyOfArtifact("jar2.jar", new byte[] { 3 }, "jar2.jar", processDefinition.getId(), ScopeType.PROCESS);
    }

    @Test
    public void should_setConnectorImplementation_delete_and_create_impl_file() throws Exception {
        //given
        byte[] zip = createConnectorArchiveZip("myConnector2.impl", "connectorId", "connectorVersion");
        havingConnector(processDefinition, "myConnector1.impl", "connectorId", "connectorVersion");
        //when
        connectorService.setConnectorImplementation(processDefinition, "connectorId", "connectorVersion", zip);
        //then
        verify(processResourcesService).remove(sBarResourceArgumentCaptor.capture());
        assertThat(sBarResourceArgumentCaptor.getValue().getName()).isEqualTo("myConnector1.impl");
        byte[] connectorImplFile = createConnectorImplFile("connectorId", "connectorVersion");
        verify(processResourcesService).add(processDefinition.getId(), "myConnector2.impl", BARResourceType.CONNECTOR,
                connectorImplFile);
    }

    @Test
    public void should_setConnectorImplementation_update_connector_implementation_file() throws Exception {
        //given
        byte[] zip = createConnectorArchiveZip("myConnector1.impl", "connectorId", "connectorVersion", new BarResource("jar1.jar", new byte[] { 1 }));
        havingConnector(processDefinition, "myConnector1.impl", "connectorId", "connectorVersion");
        //when
        connectorService.setConnectorImplementation(processDefinition, "connectorId", "connectorVersion", zip);
        //then
        verify(processResourcesService, never()).remove(any(SBARResource.class));
        verify(processResourcesService, never()).add(anyLong(), anyString(), any(BARResourceType.class), any(byte[].class));
        byte[] connectorImplFile = createConnectorImplFile("connectorId", "connectorVersion", new BarResource("jar1.jar", new byte[] { 1 }));
        verify(processResourcesService).update(sBarResourceArgumentCaptor.capture(), eq(connectorImplFile));
        assertThat(sBarResourceArgumentCaptor.getValue().getName()).isEqualTo("myConnector1.impl");
    }

    private void havingConnector(SProcessDefinitionImpl processDefinition, String implName, String connectorId, String connectorVersion, BarResource... jars)
            throws Exception {
        doReturn(Collections.singletonList(
                new SBARResource(implName, BARResourceType.CONNECTOR, processDefinition.getId(), createConnectorImplFile(connectorId, connectorVersion, jars))))
                        .when(processResourcesService)
                        .get(eq(processDefinition.getId()), eq(BARResourceType.CONNECTOR), anyInt(), anyInt());
        for (BarResource jar : jars) {
            doReturn(new SDependencyImpl(jar.getName(), jar.getName(), jar.getContent())).when(dependencyService)
                    .getDependencyOfArtifact(processDefinition.getId(), ScopeType.PROCESS, jar.getName());
        }
    }

    @Test
    public void setConnectorImplementation_should_detect_old_convention_fileName_in_db() throws Exception {
        //given
        byte[] zip = createConnectorArchiveZip("myConnector2.impl", "connectorId", "connectorVersion");
        havingConnectorWithWrongJarName(processDefinition, "myConnector1.impl", "connectorId", "connectorVersion",
                new BarResource("myJar.jar", new byte[] { 1 }));

        //when
        connectorService.setConnectorImplementation(processDefinition, "connectorId", "connectorVersion", zip);

        //then
        verify(dependencyService).getDependencyOfArtifact(123153L, ScopeType.PROCESS, "123153_myJar.jar");
    }

    private void havingConnectorWithWrongJarName(SProcessDefinitionImpl processDefinition, String implName, String connectorId, String connectorVersion, BarResource... jars)
            throws Exception {
        doReturn(Collections.singletonList(
                new SBARResource(implName, BARResourceType.CONNECTOR, processDefinition.getId(), createConnectorImplFile(connectorId, connectorVersion, jars))))
                        .when(processResourcesService)
                        .get(eq(processDefinition.getId()), eq(BARResourceType.CONNECTOR), anyInt(), anyInt());
        for (BarResource jar : jars) {
            doReturn(null).when(dependencyService).getDependencyOfArtifact(processDefinition.getId(), ScopeType.PROCESS, jar.getName());
            doReturn(new SDependencyImpl(jar.getName(), jar.getName(), jar.getContent())).when(dependencyService)
                    .getDependencyOfArtifact(processDefinition.getId(), ScopeType.PROCESS, processDefinition.getId()+ "_" + jar.getName());
        }
    }

    private byte[] createConnectorArchiveZip(String connectorImplFileName, String definitionId, final String definitionVersion, BarResource... jars)
            throws Exception {
        HashMap<String, byte[]> files = new HashMap<>();
        for (BarResource jar : jars) {
            files.put("classpath/" + jar.getName(), jar.getContent());
        }
        byte[] connectorImplFileContent = createConnectorImplFile(definitionId, definitionVersion, jars);
        files.put(connectorImplFileName, connectorImplFileContent);
        return IOUtil.zip(files);
    }

    private byte[] createConnectorImplFile(SConnectorImplementationDescriptor connector) {
        return createConnectorImplFile(connector.getDefinitionId(), connector.getDefinitionVersion(), connector.getId(), connector.getVersion(),
                connector.getImplementationClassName(), connector.getJarDependencies());
    }

    private byte[] createConnectorImplFile(String definitionId, String definitionVersion, String id, String version, String className, List<String> jars) {
        return ("<connectorImplementation>\n" +
                "\n" +
                "\t<definitionId>" + definitionId + "</definitionId>\n" +
                "\t<definitionVersion>" + definitionVersion + "</definitionVersion>\n" +
                "\t<implementationClassname>" + className + "</implementationClassname>\n" +
                "\t<implementationId>" + id + "</implementationId>\n" +
                "\t<implementationVersion>" + version + "</implementationVersion>\n" +
                "\n" +
                "\t<jarDependencies>\n" +
                getJarsXml(jars) +
                "\t</jarDependencies>\n" +
                "</connectorImplementation>\n").getBytes();
    }

    private byte[] createConnectorImplFile(String definitionId, String definitionVersion, BarResource... jars) {
        List<String> jarNames = new ArrayList<>(jars.length);
        for (BarResource jar : jars) {
            jarNames.add(jar.getName());
        }
        return createConnectorImplFile(definitionId, definitionVersion, "implId", "1.0", "TheClass", jarNames);
    }

    private String getJarsXml(List<String> jars) {
        StringBuilder result = new StringBuilder();
        for (String jar : jars) {
            result.append("\t\t<jarDependency>").append(jar).append("</jarDependency>\n");
        }
        return result.toString();
    }

    @Test
    public void should_throw_a_SConnectorException_if_a_Throwable_is_thrown_when_executing_a_connector() throws Exception {
        //given
        SConnectorImplementationDescriptor connectorImplementationDescriptor = new SConnectorImplementationDescriptor(MyTestConnector.class.getName(), "implId",
                "impplVersion", "defId", "defVersion", new ArrayList<>(Collections.<String> emptyList()));
        SConnectorInstance connectorInstance = mock(SConnectorInstance.class);

        Map<String, Object> inputParameters = Collections.<String, Object> singletonMap("key", "value");
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        when(connectorExecutor.execute(notNull(SConnector.class), eq(inputParameters), eq(contextClassLoader))).thenThrow(new NoClassDefFoundError());

        expectedException.expect(SConnectorException.class);

        connectorService.executeConnector(PROCESS_DEFINITION_ID, connectorInstance, connectorImplementationDescriptor, contextClassLoader, inputParameters);
    }
}
