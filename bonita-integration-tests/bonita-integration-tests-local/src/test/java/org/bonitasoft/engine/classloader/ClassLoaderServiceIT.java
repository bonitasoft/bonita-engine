/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;
import static org.bonitasoft.engine.commons.io.IOUtil.generateJar;
import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;
import static org.bonitasoft.engine.dependency.model.ScopeType.TENANT;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.CallableWithException;
import org.bonitasoft.engine.RunnableWithException;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.SPlatformDependency;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros, Charles Souillard, Baptiste Mesta
 */
@Slf4j
public class ClassLoaderServiceIT extends CommonBPMServicesTest {

    private DependencyService dependencyService;

    private DependencyService platformDependencyService;

    private ClassLoaderService classLoaderService;

    private static final long ID1 = 1;

    private static final long ID2 = 2;

    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());

        getTransactionService().begin();

        dependencyService.deleteDependencies(ID1, PROCESS);
        dependencyService.deleteDependencies(ID1, TENANT);
        dependencyService.deleteDependencies(ID2, PROCESS);
        dependencyService.deleteDependencies(ID2, TENANT);
        platformDependencyService.deleteDependencies(ClassLoaderIdentifier.GLOBAL_ID,
                ClassLoaderIdentifier.GLOBAL_TYPE);
        getTransactionService().complete();
        classLoaderService.stop();
        classLoaderService = null;
        dependencyService = null;
        platformDependencyService = null;
    }

    @Before
    public void setUp() {
        classLoaderService = getServiceAccessor().getClassLoaderService();
        dependencyService = getServiceAccessor().getDependencyService();
        platformDependencyService = getServiceAccessor().getPlatformDependencyService();
    }

    private <T> T inTx(CallableWithException<T> runnable) throws Exception {
        getTransactionService().begin();
        try {
            return runnable.call();
        } finally {
            getTransactionService().complete();
        }
    }

    private void inTx(RunnableWithException runnable) throws Exception {
        getTransactionService().begin();
        try {
            runnable.run();
        } finally {
            getTransactionService().complete();
        }
    }

    private void initializeClassLoaderService() throws Exception {
        inTx(() -> {
            createPlatformDependency("globalResource", "globalResource.jar",
                    generateJar(GlobalClass1.class, GlobalClass2.class, SharedClass1.class));
            createDependency(ID1, PROCESS, "LocalResource1", "LocalResource1.jar",
                    generateJar(LocalClass1.class, LocalClass2.class));
            createDependency(ID2, PROCESS, "LocalResource1", "LocalResource1.jar",
                    generateJar(LocalClass1.class, LocalClass2.class));
            createDependency(ID1, PROCESS, "LocalResource2", "LocalResource2.jar",
                    generateJar(LocalClass3.class, LocalClass4.class, SharedClass1.class));
        });
    }

    private void addNotInPathDependencies() throws Exception {
        inTx(() -> {
            createPlatformDependency("NotInPathGlobal", "NotInPathGlobal.jar",
                    IOUtil.getAllContentFrom(ClassLoaderServiceIT.class.getResource("NotInPathGlobal.jar")));
            createPlatformDependency("NotInPathShared", "NotInPathShared.jar",
                    IOUtil.getAllContentFrom(ClassLoaderServiceIT.class.getResource("NotInPathShared.jar")));
            createDependency(ID1, PROCESS, "NotInPathLocal", "NotInPathLocal.jar",
                    IOUtil.getAllContentFrom(ClassLoaderServiceIT.class.getResource("NotInPathLocal.jar")));
        });
    }

    private long createDependency(final long artifactId, final ScopeType artifactType, final String name,
            final String fileName,
            final byte[] value) throws SDependencyException, SClassLoaderException {
        long id = dependencyService.createMappedDependency(name, value, fileName, artifactId, artifactType).getId();
        classLoaderService.refreshClassLoaderImmediately(identifier(artifactType, artifactId));
        log.info("Created {}:{} dependency with {}", artifactType, artifactId, fileName);
        return id;
    }

    private long createPlatformDependency(final String name, final String fileName, final byte[] value)
            throws SDependencyException, SClassLoaderException {
        final SPlatformDependency dependency = new SPlatformDependency(name, fileName, value);
        platformDependencyService.createMappedDependency(name, value, fileName,
                ClassLoaderIdentifier.GLOBAL_ID,
                ClassLoaderIdentifier.GLOBAL_TYPE);
        classLoaderService.refreshClassLoaderImmediately(ClassLoaderIdentifier.GLOBAL);
        log.info("Created platform dependency with {}", fileName);
        return dependency.getId();
    }

    private void initializeClassLoaderServiceWithTwoApplications() throws Exception {
        getTransactionService().begin();
        createPlatformDependency("globalResource", "globalResource.jar",
                generateJar(GlobalClass1.class, SharedClass1.class));
        createDependency(ID1, PROCESS, "LocalResource111", "LocalResource1.jar", generateJar(LocalClass1.class));
        createDependency(ID2, PROCESS, "LocalResource211", "LocalResource1.jar", generateJar(LocalClass1.class));
        createDependency(ID1, PROCESS, "LocalResource123", "LocalResource3.jar", generateJar(LocalClass3.class));
        createDependency(ID1, TENANT, "LocalResource122", "LocalResource2.jar", generateJar(LocalClass2.class));
        createDependency(ID2, TENANT, "LocalResource222", "LocalResource2.jar", generateJar(LocalClass2.class));
        createDependency(ID1, TENANT, "LocalResource124", "LocalResource4.jar", generateJar(LocalClass4.class));
        getTransactionService().complete();
    }

    @Test
    public void testLoadGlobalClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader globalClassLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
        final Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass1");
        final ClassLoader classLoader = clazz.getClassLoader();

        // getTransactionService().complete();
        checkGlobalClassLoader(classLoader);
        assertSameClassloader(globalClassLoader, classLoader);
    }

    @Test
    public void testLoadLocalClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID1));
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.engine.classloader.LocalClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        checkLocalClassLoader(classLoader);

        assertSameClassloader(localClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadGlobalClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID1));
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        checkGlobalClassLoader(classLoader);

        assertNotSameClassloader(localClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadSharedClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID1));
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        checkLocalClassLoader(classLoader);

        assertSameClassloader(localClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadSharedClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader globalClassLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
        final Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        checkGlobalClassLoader(classLoader);

        assertSameClassloader(globalClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadOnlyInPathClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader classLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.OnlyInPathClass1");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testLoadOnlyInPathClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader classLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID1));
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.OnlyInPathClass1");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testLoadLocalClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader virtualGlobalClassLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);

        final Class<?> clazz = virtualGlobalClassLoader.loadClass("org.bonitasoft.engine.classloader.LocalClass1");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testGlobalClassLoaderIsSingleForTwoLocalClassLoaders() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoaderP1 = classLoaderService
                .getClassLoader(identifier(PROCESS, ID1));
        final Class<?> clazzP1 = localClassLoaderP1.loadClass("org.bonitasoft.engine.classloader.GlobalClass1");
        final ClassLoader classLoader = clazzP1.getClassLoader();
        checkGlobalClassLoader(classLoader);

        assertFalse(localClassLoaderP1 == classLoader);

        final ClassLoader localClassLoaderP2 = classLoaderService
                .getClassLoader(identifier(PROCESS, ID2));
        final Class<?> clazzP2 = localClassLoaderP2.loadClass("org.bonitasoft.engine.classloader.GlobalClass1");
        final ClassLoader classLoader2 = clazzP2.getClassLoader();
        checkGlobalClassLoader(classLoader2);

        assertFalse(localClassLoaderP2 == classLoader2);

        // verify if they are the same object (same reference)
        assertSame(classLoader, classLoader2);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadLocalClassUsingUsingBadLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader classLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID2));
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.LocalClass3");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testLoadSharedClassUsingBadLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // Should not be found with ID2 (bad scope):
        final ClassLoader classLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID2));
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final ClassLoader classLoader2 = clazz.getClassLoader();
        checkGlobalClassLoader(classLoader2);

        assertNotSame(classLoader, classLoader2);
    }

    @Test
    public void testRemoveLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        final ClassLoader localClassLoader1 = classLoaderService.getClassLoader(identifier(PROCESS, ID1));
        final ClassLoader localClassLoader2 = classLoaderService.getClassLoader(identifier(PROCESS, ID2));

        classLoaderService.removeLocalClassloader(identifier(PROCESS, ID1));

        assertNotSameClassloader(localClassLoader1, classLoaderService.getClassLoader(identifier(PROCESS, ID1)));

        classLoaderService.removeLocalClassloader(identifier(PROCESS, ID2));

        assertNotSameClassloader(localClassLoader2, classLoaderService.getClassLoader(identifier(PROCESS, ID2)));
    }

    @Test
    public void testAddResourcesToGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        final ClassLoader globalClassLoaderBefore = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);

        //GlobalClass 3 is in the app classloader
        assertThat(globalClassLoaderBefore.loadClass("org.bonitasoft.engine.classloader.GlobalClass3").getClassLoader())
                .isNotInstanceOf(BonitaClassLoader.class);

        inTx(() -> {
            createPlatformDependency("newlib", "newlib.jar", generateJar(GlobalClass3.class));
        });
        ClassLoader globalClassLoaderAfter = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
        final ClassLoader classLoader2 = globalClassLoaderAfter
                .loadClass("org.bonitasoft.engine.classloader.GlobalClass3").getClassLoader();
        checkGlobalClassLoader(classLoader2);

        assertThat(((BonitaClassLoader) globalClassLoaderBefore).isDestroyed()).isTrue();
        assertThat(globalClassLoaderBefore).isNotEqualTo(globalClassLoaderAfter);
        assertSameClassloader(globalClassLoaderAfter, classLoader2);
    }

    @Test
    public void testAddResourcesToLocalClassLoader() throws Exception {
        initializeClassLoaderService();

        //at first, the local classloader does contains GlobalClass2, only the root classloader contains the GlobalClass2
        ClassLoader classLoaderBefore = inTx(
                () -> classLoaderService.getClassLoader(identifier(PROCESS, ID1)));
        checkGlobalClassLoader(
                classLoaderBefore.loadClass("org.bonitasoft.engine.classloader.GlobalClass2").getClassLoader());

        //add a dependency with GlobalClass2 and re-ask for the classloader
        ClassLoader classLoaderAfter = inTx(() -> {
            createDependency(ID1, PROCESS, "newlib", "newlib.jar", generateJar(GlobalClass2.class));
            return classLoaderService.getClassLoader(identifier(PROCESS, ID1));
        });

        checkGlobalClassLoader(
                classLoaderBefore.loadClass("org.bonitasoft.engine.classloader.GlobalClass2").getClassLoader());
        checkLocalClassLoader(
                classLoaderAfter.loadClass("org.bonitasoft.engine.classloader.GlobalClass2").getClassLoader());

        assertThat(classLoaderBefore)
                .isNotEqualTo(classLoaderService.getClassLoader(identifier(PROCESS, ID1)));
        assertThat(((BonitaClassLoader) classLoaderBefore).isDestroyed()).isTrue();
        assertThat(classLoaderAfter)
                .isEqualTo(classLoaderService.getClassLoader(identifier(PROCESS, ID1)));
    }

    @Test
    public void testResetGlobalClassLoader() throws Exception {
        initializeClassLoaderService();

        inTx(() -> {
            createPlatformDependency("newlib", "newlib.jar", generateJar(GlobalClass3.class));
        });

        inTx(() -> {
            ClassLoader globalClassLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
            Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass3");
            final ClassLoader classLoader = clazz.getClassLoader();
            checkGlobalClassLoader(classLoader);
            assertSameClassloader(globalClassLoader, classLoader);
            platformDependencyService.deleteDependencies(ClassLoaderIdentifier.GLOBAL_ID,
                    ClassLoaderIdentifier.GLOBAL_TYPE);
            classLoaderService.refreshClassLoaderImmediately(ClassLoaderIdentifier.GLOBAL);

        });
        inTx(() -> {
            ClassLoader globalClassLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
            Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass3");
            // dependency is not in the bonita classloader anymore but only in the Test classloader
            assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        });

    }

    @Test
    public void testLoadNotInPathGlobalClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader globalClassLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
        final Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathGlobalClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        checkGlobalClassLoader(classLoader);

        assertSameClassloader(globalClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadNotInPathGlobalClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader localClassLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID1));
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathGlobalClass1");
        checkGlobalClassLoader(clazz.getClassLoader());
        assertNotSameClassloader(localClassLoader, clazz.getClassLoader());
        assertSameClassloader(classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL), clazz.getClassLoader());
        // getTransactionService().complete();
    }

    @Test
    public void testLoadNotInPathLocalClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader localClassLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID1));
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathLocalClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        checkLocalClassLoader(classLoader);

        assertSameClassloader(localClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test(expected = ClassNotFoundException.class)
    public void testLoadNotInPathLocalClassUsingWrongLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader classLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID2));
        // getTransactionService().complete();
        classLoader.loadClass("org.bonitasoft.classloader.test.NotInPathLocalClass1");
        fail("load class with wrong classloader");
    }

    @Test
    public void testLoadNotInPathSharedClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader globalClassLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
        final Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathSharedClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        checkGlobalClassLoader(classLoader);

        assertSameClassloader(globalClassLoader, classLoader);
        // getTransactionService().complete();
    }

    @Test
    public void testLoadNotInPathSharedClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader localClassLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID1));
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathSharedClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        checkGlobalClassLoader(classLoader);

        // getTransactionService().complete();
    }

    @Test
    public void loadResource() throws Exception {
        initializeClassLoaderService();
        getTransactionService().begin();
        final URL resourceFile = ClassLoaderServiceIT.class.getResource("resource.txt");
        final byte[] resourceFileContent = IOUtil.getAllContentFrom(resourceFile);

        createPlatformDependency("resource", "resource.txt", resourceFileContent);
        getTransactionService().complete();

        final ClassLoader virtualGlobalClassLoader = classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL);
        final InputStream resourceStream = virtualGlobalClassLoader.getResourceAsStream("resource.txt");
        assertEquals(resourceFileContent.length, IOUtil.getAllContentFrom(resourceStream).length);
        resourceStream.close();
    }

    @Test
    public void testDifferentsApplicationHaveDifferentGlobalClassLoader() throws Exception {
        initializeClassLoaderServiceWithTwoApplications();
        final ClassLoader process1Classloader = classLoaderService
                .getClassLoader(identifier(PROCESS, ID1));
        final ClassLoader tenant1Classloader = classLoaderService
                .getClassLoader(identifier(TENANT, ID1));

        final Class<?> sharedClassLoadedFromProcess1 = process1Classloader
                .loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final Class<?> sharedClassLoadedFromTenant1 = tenant1Classloader
                .loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        checkGlobalClassLoader(sharedClassLoadedFromProcess1.getClassLoader());
        assertSameClassloader(classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL),
                sharedClassLoadedFromProcess1.getClassLoader());

        checkGlobalClassLoader(sharedClassLoadedFromTenant1.getClassLoader());
        assertSameClassloader(classLoaderService.getClassLoader(ClassLoaderIdentifier.GLOBAL),
                sharedClassLoadedFromTenant1.getClassLoader());
    }

    private void checkGlobalClassLoader(final ClassLoader classLoader) {
        assertThat(((BonitaClassLoader) classLoader).getIdentifier()).isEqualTo(ClassLoaderIdentifier.GLOBAL);
    }

    private void checkLocalClassLoader(final ClassLoader classLoader) {
        assertThat(((BonitaClassLoader) classLoader).getIdentifier()).isNotEqualTo(ClassLoaderIdentifier.GLOBAL);
    }

    private boolean isBonitaClassLoader(final ClassLoader classLoader) {
        return classLoader instanceof BonitaClassLoader;
    }

    private void assertSameClassloader(final ClassLoader classLoader1, final ClassLoader classLoader2) {
        assertThat(classLoader1).isNotNull().isEqualTo(classLoader2);
    }

    private void assertNotSameClassloader(final ClassLoader classLoader1, final ClassLoader classLoader2) {
        assertThat(classLoader1).isNotNull().isNotEqualTo(classLoader2);
    }

    @Test
    public void should_getResource_point_to_existing_file_after_classloader_refresh() throws Exception {
        //given a classloader that is refreshed
        getTransactionService().begin();
        Map<String, byte[]> jarResources = Collections.singletonMap("test.xml", "<node>content</mode>".getBytes());
        byte[] jarContent = generateJar(jarResources);
        dependencyService.createMappedDependency("myResource.jar", jarContent, "myResource.jar", ID1,
                PROCESS);
        getTransactionService().complete();
        getTransactionService().begin();
        classLoaderService.refreshClassLoaderImmediately(identifier(PROCESS, ID1));
        getTransactionService().complete();
        getTransactionService().begin();
        classLoaderService.refreshClassLoaderImmediately(identifier(PROCESS, ID1));
        getTransactionService().complete();

        //when
        ClassLoader localClassLoader = classLoaderService.getClassLoader(identifier(PROCESS, ID1));
        URL resource = localClassLoader.getResource("test.xml");
        assertThat(resource).isNotNull();
        String stringUrl = resource.toExternalForm();
        URL url = new URL(stringUrl);

        //then
        assertThat(url).isNotNull();
        String contentFromUrlOfTheClassLoader = readUrl(resource);
        String contentFromUrlRecreated = readUrl(url);

        assertThat(contentFromUrlOfTheClassLoader).isEqualTo("<node>content</mode>");
        assertThat(contentFromUrlRecreated).isEqualTo("<node>content</mode>");

    }

    @Test
    public void in_case_of_rollback_classloader_should_end_in_same_state_after_calling_refreshClassloaderImmediatelyWithRollback()
            throws Exception {
        getTransactionService().begin();
        dependencyService.createMappedDependency("myResource.jar",
                IOUtil.generateJar(Collections.singletonMap("test.xml", "<node>content</mode>".getBytes())),
                "myResource.jar", ID1, PROCESS);
        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, ID1));
        getTransactionService().complete();
        assertThat(classLoaderService.getClassLoader(identifier(PROCESS, ID1)).getResource("test.xml")).isNotNull();

        getTransactionService().begin();
        dependencyService.createMappedDependency("myResource2.jar",
                IOUtil.generateJar(Collections.singletonMap("test2.xml", "<node>content</mode>".getBytes())),
                "myResource2.jar", ID1, PROCESS);
        classLoaderService.refreshClassLoaderImmediatelyWithRollback(identifier(PROCESS, ID1));
        assertThat(classLoaderService.getClassLoader(identifier(PROCESS, ID1)).getResource("test2.xml")).isNotNull();
        getTransactionService().setRollbackOnly();
        getTransactionService().complete();
        assertThat(classLoaderService.getClassLoader(identifier(PROCESS, ID1)).getResource("test.xml")).isNotNull();
        assertThat(classLoaderService.getClassLoader(identifier(PROCESS, ID1)).getResource("test2.xml")).isNull();

    }

    @Test
    public void refreshClassloaderImmediatelyWithRollback_should_update_classloader_if_transaction_is_committed()
            throws Exception {
        getTransactionService().begin();
        dependencyService.createMappedDependency("myResource.jar",
                IOUtil.generateJar(Collections.singletonMap("test.xml", "<node>content</mode>".getBytes())),
                "myResource.jar", ID1, PROCESS);
        classLoaderService.refreshClassLoaderAfterUpdate(identifier(PROCESS, ID1));
        getTransactionService().complete();
        assertThat(classLoaderService.getClassLoader(identifier(PROCESS, ID1)).getResource("test.xml")).isNotNull();

        getTransactionService().begin();
        dependencyService.createMappedDependency("myResource2.jar",
                IOUtil.generateJar(Collections.singletonMap("test2.xml", "<node>content</mode>".getBytes())),
                "myResource2.jar", ID1, PROCESS);
        classLoaderService.refreshClassLoaderImmediatelyWithRollback(identifier(PROCESS, ID1));
        assertThat(classLoaderService.getClassLoader(identifier(PROCESS, ID1)).getResource("test2.xml")).isNotNull();
        getTransactionService().complete();
        assertThat(classLoaderService.getClassLoader(identifier(PROCESS, ID1)).getResource("test.xml")).isNotNull();
        assertThat(classLoaderService.getClassLoader(identifier(PROCESS, ID1)).getResource("test2.xml")).isNotNull();

    }

    private String readUrl(URL resource) throws IOException {
        URLConnection urlConnection = resource.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null)
            content.append(inputLine);
        in.close();
        return content.toString();
    }
}
