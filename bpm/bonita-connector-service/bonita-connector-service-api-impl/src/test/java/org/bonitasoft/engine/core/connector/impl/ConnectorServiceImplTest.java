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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.connector.exception.SInvalidConnectorImplementationException;
import org.bonitasoft.engine.core.connector.parser.JarDependencies;
import org.bonitasoft.engine.core.connector.parser.SConnectorImplementationDescriptor;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.xml.Parser;
import org.bonitasoft.engine.xml.ParserFactory;
import org.bonitasoft.engine.xml.SXMLParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public class ConnectorServiceImplTest {

    /**
     * @author Emmanuel Duchastenier
     */
    protected FilenameFilter jarFilenameFilter = new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
            return name.endsWith(".jar");
        }
    };

    private ConnectorServiceImpl connectorService;

    @Mock
    private Parser parser;

    @Mock
    private CacheService cacheService;

    @Mock
    private ParserFactory parserFactory;

    @Mock
    private DependencyService dependencyService;

    @Mock
    private SConnectorImplementationDescriptor connectorImplDescriptorInCache;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {

        fixBonitaHomeIfNotSet();

        // parser = mock(Parser.class);

        //  parserFactory = mock(ParserFactory.class);
        doReturn(parser).when(parserFactory).createParser(anyList());

        //  dependencyService = mock(DependencyService.class);

        connectorService = new ConnectorServiceImpl(/* cacheService */mock(CacheService.class), mock(ConnectorExecutor.class), parserFactory,
                mock(ReadSessionAccessor.class),
                mock(ExpressionResolverService.class), mock(OperationService.class), dependencyService, null, mock(TimeTracker.class));
    }

    private void fixBonitaHomeIfNotSet() {
        //when test are run outside of maven
        System.setProperty("bonita.home", System.getProperty("bonita.home", IOUtil.createTempDirectoryInDefaultTempDirectory("bonita_home").getAbsolutePath()));
    }

    @Test(expected = SInvalidConnectorImplementationException.class)
    public void checkConnectorImplementationIsValidWithCorruptFile() throws Exception {
        connectorService.checkConnectorImplementationIsValid(new byte[] { 1, 5, 6, 87, 9, 9, 36, 1, 6, 6, 5, 3, 5, 5, 5, 64, 6, 5, 5 }, "myConnector", "1.0.0");
    }

    @Test(expected = SInvalidConnectorImplementationException.class)
    public void checkConnectorImplementationIsValidWithZipHavingNoImpl() throws Exception {
        final byte[] zip = IOUtil.zip(Collections.<String, byte[]> singletonMap("connector.notImpl", "mocked".getBytes()));
        connectorService.checkConnectorImplementationIsValid(zip, "myConnector", "1.0.0");
    }

    @Test
    public void checkConnectorImplementationIsValidWithValidZip() throws Exception {
        when(parser.getObjectFromXML(any(InputStream.class))).thenReturn(
                new SConnectorImplementationDescriptor("org.Test", "myConnector", "1.0.0", "myConnector", "1.0.0", new JarDependencies(Collections
                        .<String> emptyList())));
        final byte[] zip = IOUtil.zip(Collections.<String, byte[]> singletonMap("connector.impl", "mocked".getBytes()));
        connectorService.checkConnectorImplementationIsValid(zip, "myConnector", "1.0.0");
    }

    @Test(expected = SInvalidConnectorImplementationException.class)
    public void checkConnectorImplementationIsValidWithValidFileButWrongImpl() throws Exception {
        when(parser.getObjectFromXML(any(InputStream.class))).thenReturn(
                new SConnectorImplementationDescriptor("org.Test", "myConnector", "1.0.0", "myConnectorWrong", "1.0.0", new JarDependencies(Collections
                        .<String> emptyList())));
        final byte[] zip = IOUtil.zip(Collections.<String, byte[]> singletonMap("connector.impl", "mocked".getBytes()));
        connectorService.checkConnectorImplementationIsValid(zip, "myConnector", "1.0.0");
    }

    @Test
    public void setNewConnectorImplemCleansOldDependencies() throws Exception {
        final long tenantId = 98774L;
        final long processDefId = 17L;
        final File processDefFolder = new File(BonitaHomeServer.getInstance().getProcessesFolder(tenantId) + File.separator + processDefId);
        final File connFolder = new File(processDefFolder, "connector");
        final File classPathFolder = new File(processDefFolder, "classpath");
        connFolder.mkdirs();
        classPathFolder.mkdirs();
        try {
            final SProcessDefinition sProcessDef = mock(SProcessDefinition.class);
            when(sProcessDef.getId()).thenReturn(processDefId);
            final String connectorDefId = "org.bonitasoft.connector.BeerConnector";
            final String connectorDefVersion = "1.0.0";
            final String connectorImplId = "org.bonitasoft.connector.HoogardenConnector";
            final String connectorImplVersion = "1.0";
            final String implementationClassName = "org.bonitasoft.engine.connectors.HoogardenBeerConnector";
            final String dep1Jar = "some1.jar";
            final String hoogardenConnectorJar = "HoogardenConnector.jar";
            final SConnectorImplementationDescriptor ConnectorImplDescriptor = new SConnectorImplementationDescriptor(implementationClassName, connectorImplId,
                    connectorImplVersion, connectorDefId, connectorDefVersion, new JarDependencies(Arrays.asList(dep1Jar, hoogardenConnectorJar)));
            when(parser.getObjectFromXML(any(InputStream.class))).thenReturn(ConnectorImplDescriptor);
            when(parser.getObjectFromXML(any(File.class))).thenReturn(ConnectorImplDescriptor);
            Map<String, byte[]> zipFileMap = new HashMap<String, byte[]>(3);
            zipFileMap.put("HoogardenBeerConnector.impl", "tototo".getBytes());
            zipFileMap.put(dep1Jar, new byte[] { 12, 94, 14, 12 });
            zipFileMap.put(hoogardenConnectorJar, new byte[] { 12, 94, 14, 9, 54, 65, 98, 54, 21, 32, 65 });
            final byte[] zip1 = IOUtil.zip(zipFileMap);
            connectorService.setConnectorImplementation(sProcessDef, tenantId, connectorDefId, connectorDefVersion, zip1);
            File[] jarFiles = classPathFolder.listFiles(jarFilenameFilter);

            assertEquals(2, jarFiles.length);
            final List<File> jars = Arrays.asList(jarFiles);
            assertThat(names(jars)).as("Not all jar files have been found").contains(hoogardenConnectorJar, dep1Jar);

            zipFileMap = new HashMap<String, byte[]>(1);
            zipFileMap.put("GrimbergenBeerConnector.impl", "GrimbergenBeerConnector.impl".getBytes());
            final String newJar = "GrimbergenBeerConnector.jar";
            zipFileMap.put(newJar, new byte[] { 12, 3, 14 });
            final byte[] zip2 = IOUtil.zip(zipFileMap);
            connectorService.setConnectorImplementation(sProcessDef, tenantId, connectorDefId, connectorDefVersion, zip2);

            jarFiles = classPathFolder.listFiles(jarFilenameFilter);

            assertEquals(1, jarFiles.length);
            assertEquals(newJar, jarFiles[0].getName());
        } finally {
            final boolean folderCleaned = IOUtil.deleteDir(processDefFolder);
            if (!folderCleaned) {
                System.err.println("Folder " + processDefFolder.getName() + " could not be deleted");
            }
        }
    }

    private List<String> names(final List<File> files) {
        final ArrayList<String> names = new ArrayList<String>();
        for (final File file : files) {
            names.add(file.getName());
        }
        return names;
    }

    @Test
    public void setConnectorImplementationOverwritesExistingJars() throws Exception {
        final long tenantId = 98774L;
        final long processDefId = 17L;
        final File processDefFolder = new File(BonitaHomeServer.getInstance().getProcessesFolder(tenantId) + File.separator + processDefId);
        final File connFolder = new File(processDefFolder, "connector");
        final File classPathFolder = new File(processDefFolder, "classpath");
        connFolder.mkdirs();
        classPathFolder.mkdirs();
        try {
            final SProcessDefinition sProcessDef = mock(SProcessDefinition.class);
            when(sProcessDef.getId()).thenReturn(processDefId);
            final String connectorDefId = "org.bonitasoft.connector.BeerConnector";
            final String connectorDefVersion = "1.0.0";
            final String connectorImplId = "org.bonitasoft.connector.HoogardenConnector";
            final String connectorImplVersion = "1.0";
            final String implementationClassName = "org.bonitasoft.engine.connectors.HoogardenBeerConnector";
            final String sameConnectorJarName = "HoogardenConnector.jar";
            final SConnectorImplementationDescriptor ConnectorImplDescriptor = new SConnectorImplementationDescriptor(implementationClassName, connectorImplId,
                    connectorImplVersion, connectorDefId, connectorDefVersion, new JarDependencies(Arrays.asList(sameConnectorJarName)));
            when(parser.getObjectFromXML(any(InputStream.class))).thenReturn(ConnectorImplDescriptor);
            when(parser.getObjectFromXML(any(File.class))).thenReturn(ConnectorImplDescriptor);
            Map<String, byte[]> zipFileMap = new HashMap<String, byte[]>(3);
            final byte[] originalConnectorJarContent = new byte[] { 12, 94, 14, 9, 54, 65, 98, 54, 21, 32, 65 };
            zipFileMap.put("anyName.impl", "tototo".getBytes());
            zipFileMap.put(sameConnectorJarName, originalConnectorJarContent);
            final byte[] zip1 = IOUtil.zip(zipFileMap);
            connectorService.setConnectorImplementation(sProcessDef, tenantId, connectorDefId, connectorDefVersion, zip1);

            File[] jarFiles = classPathFolder.listFiles(jarFilenameFilter);
            assertEquals(1, jarFiles.length);
            assertTrue("Deployed connector jar is not the expected size + content",
                    Arrays.equals(org.bonitasoft.engine.commons.io.IOUtil.getAllContentFrom(jarFiles[0]), originalConnectorJarContent));

            // now let's prepare the new connector implementation to replace:
            zipFileMap = new HashMap<String, byte[]>(1);
            zipFileMap.put("GrimbergenBeerConnector.impl", "GrimbergenBeerConnector.impl".getBytes());
            final byte[] newConnectorJarContent = new byte[] { 12, 3, 14 };
            zipFileMap.put(sameConnectorJarName, newConnectorJarContent);
            final byte[] zip2 = IOUtil.zip(zipFileMap);
            connectorService.setConnectorImplementation(sProcessDef, tenantId, connectorDefId, connectorDefVersion, zip2);

            jarFiles = classPathFolder.listFiles(jarFilenameFilter);
            assertEquals(1, jarFiles.length);
            assertTrue("Replaced connector jar is not the expected size + content",
                    Arrays.equals(org.bonitasoft.engine.commons.io.IOUtil.getAllContentFrom(jarFiles[0]), newConnectorJarContent));
        } finally {
            final boolean folderCleaned = IOUtil.deleteDir(processDefFolder);
            if (!folderCleaned) {
                System.err.println("Folder " + processDefFolder.getName() + " could not be deleted");
            }
        }
    }

    @Test
    public void setConnectorImplementationDoesNotCareWhereTheJarsAre() throws Exception {
        final long tenantId = 98774L;
        final long processDefId = 17L;
        final File processDefFolder = new File(BonitaHomeServer.getInstance().getProcessesFolder(tenantId) + File.separator + processDefId);
        final File connFolder = new File(processDefFolder, "connector");
        final File classPathFolder = new File(processDefFolder, "classpath");
        connFolder.mkdirs();
        classPathFolder.mkdirs();
        try {
            final SProcessDefinition sProcessDef = mock(SProcessDefinition.class);
            when(sProcessDef.getId()).thenReturn(processDefId);
            final String connectorDefId = "org.bonitasoft.connector.BeerConnector";
            final String connectorDefVersion = "1.0.0";
            final String connectorImplId = "org.bonitasoft.connector.HoogardenConnector";
            final String connectorImplVersion = "1.0";
            final String implementationClassName = "org.bonitasoft.engine.connectors.HoogardenBeerConnector";
            final String dep1Jar = "some1.jar";
            final String hoogardenConnectorJar = "HoogardenConnector.jar";
            final SConnectorImplementationDescriptor ConnectorImplDescriptor = new SConnectorImplementationDescriptor(implementationClassName, connectorImplId,
                    connectorImplVersion, connectorDefId, connectorDefVersion, new JarDependencies(Arrays.asList(dep1Jar, hoogardenConnectorJar)));
            when(parser.getObjectFromXML(any(InputStream.class))).thenReturn(ConnectorImplDescriptor);
            when(parser.getObjectFromXML(any(File.class))).thenReturn(ConnectorImplDescriptor);
            Map<String, byte[]> zipFileMap = new HashMap<String, byte[]>(3);
            zipFileMap.put("HoogardenBeerConnector.impl", "tototo".getBytes());
            zipFileMap.put(dep1Jar, new byte[] { 12, 94, 14, 12 });
            zipFileMap.put(hoogardenConnectorJar, new byte[] { 12, 94, 14, 9, 54, 65, 98, 54, 21, 32, 65 });
            final byte[] zip1 = IOUtil.zip(zipFileMap);
            connectorService.setConnectorImplementation(sProcessDef, tenantId, connectorDefId, connectorDefVersion, zip1);
            File[] jarFiles = classPathFolder.listFiles(jarFilenameFilter);

            assertEquals(2, jarFiles.length);
            final List<File> jars = Arrays.asList(jarFiles);
            assertThat(names(jars)).as("Not all jar files have been found").contains(hoogardenConnectorJar, dep1Jar);

            zipFileMap = new HashMap<String, byte[]>(1);
            zipFileMap.put("GrimbergenBeerConnector.impl", "GrimbergenBeerConnector.impl".getBytes());
            final String newJar = "GrimbergenBeerConnector.jar";
            zipFileMap.put("dummyFolder/" + newJar, new byte[] { 12, 3, 14 });
            final byte[] zip2 = IOUtil.zip(zipFileMap);
            connectorService.setConnectorImplementation(sProcessDef, tenantId, connectorDefId, connectorDefVersion, zip2);

            jarFiles = classPathFolder.listFiles(jarFilenameFilter);

            assertEquals(1, jarFiles.length);
            assertEquals(newJar, jarFiles[0].getName());
        } finally {
            final boolean folderCleaned = IOUtil.deleteDir(processDefFolder);
            if (!folderCleaned) {
                System.err.println("Folder " + processDefFolder.getName() + " could not be deleted");
            }
        }
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

    private void checkGetConnectorImplementationUsesCache(final int givenCacheSizeToBeReturned, final int expectedNumberOfCacheStoreInvocations,
            final boolean shouldCacheContainsConnectorImplementation)
            throws BonitaHomeNotSetException, SXMLParseException,
            IOException, SConnectorException, SInvalidConnectorImplementationException, SCacheException {
        connectorService = new ConnectorServiceImpl(cacheService, mock(ConnectorExecutor.class), parserFactory,
                mock(ReadSessionAccessor.class),
                mock(ExpressionResolverService.class), mock(OperationService.class), dependencyService, null, mock(TimeTracker.class));

        final long tenantId = 98774L;
        final long processDefId = 17L;
        final File processDefFolder = new File(BonitaHomeServer.getInstance().getProcessesFolder(tenantId) + File.separator + processDefId);
        final File connFolder = new File(processDefFolder, "connector");
        final File classPathFolder = new File(processDefFolder, "classpath");

        connFolder.mkdirs();
        classPathFolder.mkdirs();
        try {
            final SProcessDefinition sProcessDef;
            sProcessDef = mock(SProcessDefinition.class);

            when(sProcessDef.getId()).thenReturn(processDefId);
            final String connectorDefId = "org.bonitasoft.connector.BeerConnector";
            final String connectorDefVersion = "1.0.0";
            final String connectorImplId = "org.bonitasoft.connector.HoogardenConnector";
            final String connectorImplVersion = "1.0";
            final String implementationClassName = "org.bonitasoft.engine.connectors.HoogardenBeerConnector";
            final String dep1Jar = "some1.jar";
            final String hoogardenConnectorJar = "HoogardenConnector.jar";
            final SConnectorImplementationDescriptor connectorImplDescriptor = new SConnectorImplementationDescriptor(implementationClassName, connectorImplId,
                    connectorImplVersion, connectorDefId, connectorDefVersion, new JarDependencies(Arrays.asList(dep1Jar, hoogardenConnectorJar)));
            when(parser.getObjectFromXML(any(InputStream.class))).thenReturn(connectorImplDescriptor);
            when(parser.getObjectFromXML(any(File.class))).thenReturn(connectorImplDescriptor);
            final Map<String, byte[]> zipFileMap = new HashMap<String, byte[]>(3);
            zipFileMap.put("HoogardenBeerConnector.impl", "tototo".getBytes());
            zipFileMap.put(dep1Jar, new byte[] { 12, 94, 14, 12 });
            zipFileMap.put(hoogardenConnectorJar, new byte[] { 12, 94, 14, 9, 54, 65, 98, 54, 21, 32, 65 });
            final byte[] zip1 = IOUtil.zip(zipFileMap);

            //setConnectorImplementation store to cache
            connectorService.setConnectorImplementation(sProcessDef, tenantId, connectorDefId, connectorDefVersion, zip1);

            //given
            doReturn(givenCacheSizeToBeReturned).when(cacheService).getCacheSize(ConnectorServiceImpl.CONNECTOR_CACHE_NAME);

            List<String> cacheContentKeys = Collections.emptyList();
            final String buildConnectorImplementationKey = connectorService
                    .buildConnectorImplementationKey(processDefId, connectorImplId, connectorImplVersion);
            if (shouldCacheContainsConnectorImplementation) {
                cacheContentKeys = Arrays.asList(buildConnectorImplementationKey);
            }
            doReturn(cacheContentKeys).when(cacheService).getKeys(ConnectorServiceImpl.CONNECTOR_CACHE_NAME);
            doReturn(connectorImplDescriptor).when(cacheService).get(ConnectorServiceImpl.CONNECTOR_CACHE_NAME, buildConnectorImplementationKey);

            //when
            connectorService.getConnectorImplementations(processDefId, tenantId, 0,
                    10, "", OrderByType.ASC);

            //then
            verify(cacheService, times(expectedNumberOfCacheStoreInvocations + 1)).store(anyString(), any(Serializable.class), any(Object.class));

        } finally {
            final boolean folderCleaned = IOUtil.deleteDir(processDefFolder);
            if (!folderCleaned) {
                System.err.println("Folder " + processDefFolder.getName() + " could not be deleted");
            }
        }
    }

}
