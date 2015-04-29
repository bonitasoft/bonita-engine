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
package org.bonitasoft.engine.classloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.URL;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilder;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingBuilder;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SPlatformDependencyBuilder;
import org.bonitasoft.engine.dependency.model.builder.SPlatformDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SPlatformDependencyMappingBuilder;
import org.bonitasoft.engine.dependency.model.builder.SPlatformDependencyMappingBuilderFactory;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros, Charles Souillard, Baptiste Mesta
 */
public class ClassLoaderServiceTest extends CommonBPMServicesTest {

    private DependencyService dependencyService;

    private DependencyService platformDependencyService;

    private ClassLoaderService classLoaderService;

    private static final ScopeType TYPE1 = ScopeType.PROCESS;

    private static final ScopeType TYPE2 = ScopeType.TENANT;

    private static final long ID1 = 1;

    private static final long ID2 = 2;

    @After
    public void tearDown() throws Exception {
        classLoaderService.removeAllLocalClassLoaders(TYPE1.name());
        classLoaderService.removeAllLocalClassLoaders(TYPE2.name());

        TestUtil.closeTransactionIfOpen(getTransactionService());

        getTransactionService().begin();
        dependencyService.deleteAllDependencyMappings();
        dependencyService.deleteAllDependencies();
        platformDependencyService.deleteAllDependencyMappings();
        platformDependencyService.deleteAllDependencies();
        getTransactionService().complete();
        classLoaderService = null;
        dependencyService = null;
        platformDependencyService = null;
    }

    @Before
    public void setUp() {
        classLoaderService = getTenantAccessor().getClassLoaderService();
        dependencyService = getTenantAccessor().getDependencyService();
        platformDependencyService = getPlatformAccessor().getDependencyService();
    }

    private void initializeClassLoaderService() throws Exception {
        getTransactionService().begin();
        final long globalResourceId = createPlatformDependency("globalResource", "globalResource.jar",
                IOUtil.generateJar(GlobalClass1.class, GlobalClass2.class, SharedClass1.class));

        createPlatformDependencyMapping(globalResourceId, classLoaderService.getGlobalClassLoaderType(),
                classLoaderService.getGlobalClassLoaderId());

        final long localResource1Id = createDependency(2L, TYPE1, "LocalResource1", "LocalResource1.jar",
                IOUtil.generateJar(LocalClass1.class, LocalClass2.class));
        final long localResource2Id = createDependency(2L, TYPE1, "LocalResource2", "LocalResource2.jar",
                IOUtil.generateJar(LocalClass3.class, LocalClass4.class, SharedClass1.class));

        createDependencyMapping(localResource1Id, TYPE1, ID1);
        createDependencyMapping(localResource2Id, TYPE1, ID1);

        createDependencyMapping(localResource1Id, TYPE1, ID2);
        getTransactionService().complete();
    }

    private void addNotInPathDependencies() throws Exception {
        getTransactionService().begin();
        final URL globalFile = ClassLoaderServiceTest.class.getResource("NotInPathGlobal.jar");
        final byte[] globalFileContent = IOUtil.getAllContentFrom(globalFile);
        final long globalFileId = createPlatformDependency("NotInPathGlobal", "NotInPathGlobal.jar", globalFileContent);

        final URL sharedFile = ClassLoaderServiceTest.class.getResource("NotInPathShared.jar");
        final byte[] sharedFileContent = IOUtil.getAllContentFrom(sharedFile);
        final long sharedFileId = createPlatformDependency("NotInPathShared", "NotInPathShared.jar", sharedFileContent);

        final URL localFile = ClassLoaderServiceTest.class.getResource("NotInPathLocal.jar");
        final byte[] localFileContent = IOUtil.getAllContentFrom(localFile);
        final long localFileId = createDependency(2L, TYPE1, "NotInPathLocal", "NotInPathLocal.jar", localFileContent);

        createPlatformDependencyMapping(globalFileId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());
        createPlatformDependencyMapping(sharedFileId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());

        createDependencyMapping(localFileId, TYPE1, ID1);
        // createDependencyMapping(sharedFileId, TYPE1, ID1);
        getTransactionService().complete();
    }

    private long createDependency(final long artifactId, final ScopeType artifactType, final String name, final String fileName,
            final byte[] value) throws SDependencyException {
        final SDependencyBuilder builder = BuilderFactory.get(SDependencyBuilderFactory.class).createNewInstance(name, artifactId,
                artifactType, fileName, value);
        final SDependency dependency = builder.done();
        dependencyService.createDependency(dependency);
        return dependency.getId();
    }

    private long createPlatformDependency(final String name, final String fileName, final byte[] value) throws SDependencyException {
        final SPlatformDependencyBuilder builder = BuilderFactory.get(SPlatformDependencyBuilderFactory.class)
                .createNewInstance(name, fileName, value);
        final SDependency dependency = builder.done();
        platformDependencyService.createDependency(dependency);
        return dependency.getId();
    }

    private void initializeClassLoaderServiceWithTwoApplications() throws Exception {
        getTransactionService().begin();
        final long globalResourceId = createPlatformDependency("globalResource", "globalResource.jar",
                IOUtil.generateJar(GlobalClass1.class, SharedClass1.class));
        createPlatformDependencyMapping(globalResourceId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());

        final long localResource1Id = createDependency(2L, TYPE1, "LocalResource11", "LocalResource1.jar", IOUtil.generateJar(LocalClass1.class));
        final long localResource2Id = createDependency(2L, TYPE1, "LocalResource12", "LocalResource2.jar", IOUtil.generateJar(LocalClass3.class));

        createDependencyMapping(localResource1Id, TYPE1, ID1);
        createDependencyMapping(localResource2Id, TYPE1, ID1);

        createDependencyMapping(localResource1Id, TYPE1, ID2);

        final long localResource1Id2 = createDependency(2L, TYPE2, "LocalResource21", "LocalResource1.jar", IOUtil.generateJar(LocalClass2.class));
        final long localResource2Id2 = createDependency(2L, TYPE2, "LocalResource22", "LocalResource2.jar", IOUtil.generateJar(LocalClass4.class));

        createDependencyMapping(localResource1Id2, TYPE2, ID1);
        createDependencyMapping(localResource2Id2, TYPE2, ID1);

        createDependencyMapping(localResource1Id2, TYPE2, ID2);
        getTransactionService().complete();
    }

    private long createPlatformDependencyMapping(final long dependencyId, final String artifactType, final long artifactId) throws SDependencyException {
        final SPlatformDependencyMappingBuilder builder = BuilderFactory.get(SPlatformDependencyMappingBuilderFactory.class).createNewInstance(dependencyId,
                artifactId, ScopeType.valueOf(artifactType));
        final SDependencyMapping dependencyMapping = builder.done();
        platformDependencyService.createDependencyMapping(dependencyMapping);
        return dependencyMapping.getId();
    }

    private long createDependencyMapping(final long dependencyId, final ScopeType artifactType, final long artifactId) throws SDependencyException {
        final SDependencyMappingBuilder builder = BuilderFactory.get(SDependencyMappingBuilderFactory.class).createNewInstance(dependencyId, artifactId,
                artifactType);
        final SDependencyMapping dependencyMapping = builder.done();
        dependencyService.createDependencyMapping(dependencyMapping);
        return dependencyMapping.getId();
    }

    @Test
    public void testLoadGlobalClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
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
        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
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
        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
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
        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
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
        final ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
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
        final ClassLoader classLoader = classLoaderService.getGlobalClassLoader();
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.OnlyInPathClass1");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testLoadOnlyInPathClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader classLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.OnlyInPathClass1");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testLoadLocalClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader virtualGlobalClassLoader = classLoaderService.getGlobalClassLoader();

        final Class<?> clazz = virtualGlobalClassLoader.loadClass("org.bonitasoft.engine.classloader.LocalClass1");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testGlobalClassLoaderIsSingleForTwoLocalClassLoaders() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoaderP1 = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
        final Class<?> clazzP1 = localClassLoaderP1.loadClass("org.bonitasoft.engine.classloader.GlobalClass1");
        final ClassLoader classLoader = clazzP1.getClassLoader();
        checkGlobalClassLoader(classLoader);

        assertFalse(localClassLoaderP1 == classLoader);

        final ClassLoader localClassLoaderP2 = classLoaderService.getLocalClassLoader(TYPE1.name(), ID2);
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
        final ClassLoader classLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID2);
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.LocalClass3");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        // getTransactionService().complete();
    }

    @Test
    public void testLoadSharedClassUsingBadLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // Should not be found with ID2 (bad scope):
        final ClassLoader classLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID2);
        final Class<?> clazz = classLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final ClassLoader classLoader2 = clazz.getClassLoader();
        checkGlobalClassLoader(classLoader2);

        assertNotSame(classLoader, classLoader2);
    }

    @Test
    public void testRemoveLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoader1 = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
        final ClassLoader localClassLoader2 = classLoaderService.getLocalClassLoader(TYPE1.name(), ID2);

        classLoaderService.removeLocalClassLoader(TYPE1.name(), ID1);

        assertNotSameClassloader(localClassLoader1, classLoaderService.getLocalClassLoader(TYPE1.name(), ID1));

        classLoaderService.removeLocalClassLoader(TYPE1.name(), ID2);

        assertNotSameClassloader(localClassLoader2, classLoaderService.getLocalClassLoader(TYPE1.name(), ID2));
        // getTransactionService().complete();
    }

    @Test
    public void testRemoveAllLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        // getTransactionService().begin();
        final ClassLoader localClassLoader1 = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
        final ClassLoader localClassLoader2 = classLoaderService.getLocalClassLoader(TYPE1.name(), ID2);

        classLoaderService.removeAllLocalClassLoaders(TYPE1.name());

        assertNotSameClassloader(localClassLoader1, classLoaderService.getLocalClassLoader(TYPE1.name(), ID1));
        assertNotSameClassloader(localClassLoader2, classLoaderService.getLocalClassLoader(TYPE1.name(), ID2));
        // getTransactionService().complete();
    }

    @Test
    public void testAddResourcesToGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        getTransactionService().begin();
        final ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
        Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass3");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));

        final long dependencyId = createPlatformDependency("newlib", "newlib.jar", IOUtil.generateJar(GlobalClass3.class));
        createPlatformDependencyMapping(dependencyId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());
        Thread.sleep(10); // to be sure classlaoder refresh does NOT occur.
        clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass3");
        final ClassLoader classLoader2 = clazz.getClassLoader();
        checkGlobalClassLoader(classLoader2);

        assertSameClassloader(globalClassLoader, classLoader2);
        getTransactionService().complete();
    }

    @Test
    public void testAddResourcesToLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        getTransactionService().begin();
        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
        checkGlobalClassLoader(localClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass2").getClassLoader());

        final long dependencyId = createDependency(2L, TYPE1, "newlib", "newlib.jar", IOUtil.generateJar(GlobalClass2.class));
        createDependencyMapping(dependencyId, TYPE1, ID1);

        // check the refresh has been done using the service
        final ClassLoader localClassLoader2 = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
        checkLocalClassLoader(localClassLoader2.loadClass("org.bonitasoft.engine.classloader.GlobalClass2")
                .getClassLoader());

        // check the refresh has been done using the old reference
        checkLocalClassLoader(localClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass2").getClassLoader());

        assertSameClassloader(localClassLoader, classLoaderService.getLocalClassLoader(TYPE1.name(), ID1));
        getTransactionService().complete();
    }

    @Test
    public void testResetGlobalClassLoader() throws Exception {
        initializeClassLoaderService();

        getTransactionService().begin();
        final long dependencyId = createPlatformDependency("newlib", "newlib.jar", IOUtil.generateJar(GlobalClass3.class));
        final long mappingId = createPlatformDependencyMapping(dependencyId, classLoaderService.getGlobalClassLoaderType(),
                classLoaderService.getGlobalClassLoaderId());

        ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
        Class<?> clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass3");
        final ClassLoader classLoader = clazz.getClassLoader();
        checkGlobalClassLoader(classLoader);
        assertSameClassloader(globalClassLoader, classLoader);

        platformDependencyService.deleteDependencyMapping(mappingId);

        globalClassLoader = classLoaderService.getGlobalClassLoader();
        clazz = globalClassLoader.loadClass("org.bonitasoft.engine.classloader.GlobalClass3");
        assertFalse(isBonitaClassLoader(clazz.getClassLoader()));
        getTransactionService().complete();
    }

    @Test
    public void testLoadNotInPathGlobalClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
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

        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathGlobalClass1");
        checkGlobalClassLoader(clazz.getClassLoader());
        assertNotSameClassloader(localClassLoader, clazz.getClassLoader());
        assertSameClassloader(classLoaderService.getGlobalClassLoader(), clazz.getClassLoader());
        // getTransactionService().complete();
    }

    @Test
    public void testLoadNotInPathLocalClassUsingLocalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
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

        final ClassLoader classLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID2);
        // getTransactionService().complete();
        classLoader.loadClass("org.bonitasoft.classloader.test.NotInPathLocalClass1");
        fail("load class with wrong classloader");
    }

    @Test
    public void testLoadNotInPathSharedClassUsingGlobalClassLoader() throws Exception {
        initializeClassLoaderService();
        addNotInPathDependencies();
        // getTransactionService().begin();

        final ClassLoader globalClassLoader = classLoaderService.getGlobalClassLoader();
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

        final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
        final Class<?> clazz = localClassLoader.loadClass("org.bonitasoft.classloader.test.NotInPathSharedClass1");
        final ClassLoader classLoader = clazz.getClassLoader();
        checkGlobalClassLoader(classLoader);

        // getTransactionService().complete();
    }

    @Test
    public void loadResource() throws Exception {
        initializeClassLoaderService();
        getTransactionService().begin();
        final URL resourceFile = ClassLoaderServiceTest.class.getResource("resource.txt");
        final byte[] resourceFileContent = IOUtil.getAllContentFrom(resourceFile);

        final long resourceId = createPlatformDependency("resource", "resource.txt", resourceFileContent);
        createPlatformDependencyMapping(resourceId, classLoaderService.getGlobalClassLoaderType(), classLoaderService.getGlobalClassLoaderId());
        getTransactionService().complete();

        final ClassLoader virtualGlobalClassLoader = classLoaderService.getGlobalClassLoader();
        final InputStream resourceStream = virtualGlobalClassLoader.getResourceAsStream("resource.txt");
        assertEquals(resourceFileContent.length, IOUtil.getAllContentFrom(resourceStream).length);
        resourceStream.close();
    }

    @Test
    public void testDifferentsApplicationHaveDifferentGlobalClassLoader() throws Exception {
        initializeClassLoaderServiceWithTwoApplications();
        // getTransactionService().begin();
        final ClassLoader type1ClassLoader = classLoaderService.getLocalClassLoader(TYPE1.name(), ID1);
        final ClassLoader type2ClassLoader = classLoaderService.getLocalClassLoader(TYPE2.name(), ID1);

        final Class<?> bpmClazz = type1ClassLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final Class<?> casesClazz = type2ClassLoader.loadClass("org.bonitasoft.engine.classloader.SharedClass1");
        final ClassLoader type1ClassLoader2 = bpmClazz.getClassLoader();
        checkGlobalClassLoader(type1ClassLoader2);
        assertSameClassloader(classLoaderService.getGlobalClassLoader(), type1ClassLoader2);

        final ClassLoader type2ClassLoader2 = casesClazz.getClassLoader();
        checkGlobalClassLoader(type2ClassLoader2);
        assertSameClassloader(classLoaderService.getGlobalClassLoader(), type2ClassLoader2);
        // getTransactionService().complete();
    }

    private void checkGlobalClassLoader(final ClassLoader classLoader) {
        final boolean isGlobal = classLoader instanceof BonitaClassLoader
                && ((BonitaClassLoader) classLoader).getType().equals(ClassLoaderServiceImpl.GLOBAL_TYPE);
        try {
            assertTrue(isGlobal);
        } catch (final AssertionError e) {
            System.out.println("ClassLoader should be GLOBAL: " + classLoader.toString());
            throw e;
        }
    }

    private void checkLocalClassLoader(final ClassLoader classLoader) {
        final boolean isLocal = classLoader instanceof BonitaClassLoader
                && !((BonitaClassLoader) classLoader).getType().equals(ClassLoaderServiceImpl.GLOBAL_TYPE);
        try {
            assertTrue(isLocal);
        } catch (final AssertionError e) {
            System.out.println("ClassLoader should be LOCAL: " + classLoader.toString());
            throw e;
        }
    }

    private boolean isBonitaClassLoader(final ClassLoader classLoader) {
        return classLoader instanceof BonitaClassLoader;
    }

    private void assertSameClassloader(final ClassLoader classLoader1, final ClassLoader classLoader2) {
        assertNotNull(classLoader1);
        assertNotNull(classLoader2);
        final ClassLoader c1 = classLoader1 instanceof VirtualClassLoader ? ((VirtualClassLoader) classLoader1).getClassLoader() : classLoader1;
        final ClassLoader c2 = classLoader2 instanceof VirtualClassLoader ? ((VirtualClassLoader) classLoader2).getClassLoader() : classLoader2;
        assertEquals(c1, c2);
    }

    private void assertNotSameClassloader(final ClassLoader classLoader1, final ClassLoader classLoader2) {
        assertNotNull(classLoader1);
        assertNotNull(classLoader2);
        final ClassLoader c1 = classLoader1 instanceof VirtualClassLoader ? ((VirtualClassLoader) classLoader1).getClassLoader() : classLoader1;
        final ClassLoader c2 = classLoader2 instanceof VirtualClassLoader ? ((VirtualClassLoader) classLoader2).getClassLoader() : classLoader2;
        assertNotSame(c1, c2);
    }

}
