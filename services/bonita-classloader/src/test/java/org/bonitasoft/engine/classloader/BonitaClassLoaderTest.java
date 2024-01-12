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
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.GLOBAL;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;
import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;
import static org.bonitasoft.engine.home.BonitaResource.resource;
import static org.bonitasoft.engine.io.IOUtil.generateJar;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.JavaMethodInvoker;
import org.bonitasoft.engine.data.instance.model.impl.XStreamFactory;
import org.bonitasoft.engine.home.BonitaResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class BonitaClassLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ClassLoader testClassLoader;
    private int idCounter = 1;

    @Before
    public void before() {
        testClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @After
    public void after() {
        Thread.currentThread().setContextClassLoader(testClassLoader);
    }

    @Test
    public void destroyShouldRemoveAllScopeFolderAndItsContent() throws IOException {
        final BonitaClassLoader bonitaClassLoader = BonitaClassLoaderFactory.createClassLoader(
                Stream.of(resource("myJar.jar", "Salut le monde".getBytes())), identifier(PROCESS, 154L),
                temporaryFolder.newFolder().toURI(), BonitaClassLoader.class.getClassLoader());
        File temporaryFolder = bonitaClassLoader.getTemporaryFolder();
        assertThat(temporaryFolder).as("bonitaClassLoader tempDir:%s should exists after bonitaClassLoader creation",
                temporaryFolder.getAbsolutePath())
                .exists();
        // when
        bonitaClassLoader.destroy();

        // then
        assertThat(temporaryFolder).as("bonitaClassLoader tempDir:%s should not exists after bonitaClassLoader release",
                temporaryFolder.getAbsolutePath())
                .doesNotExist();
    }

    @Test
    public void should_create_second_classloader_use_other_folder() throws Exception {
        //given
        File tempFolder = temporaryFolder.newFolder();
        //when
        BonitaClassLoader classLoader1 = BonitaClassLoaderFactory.createClassLoader(
                Stream.of(resource("myJar1.jar", "content".getBytes())), identifier(PROCESS, 12L), tempFolder.toURI(),
                BonitaClassLoaderTest.class.getClassLoader());
        BonitaClassLoader classLoader2 = BonitaClassLoaderFactory.createClassLoader(
                Stream.of(resource("myJar2.jar", "content".getBytes())), identifier(PROCESS, 13L), tempFolder.toURI(),
                BonitaClassLoaderTest.class.getClassLoader());
        //then
        assertThat(classLoader1.getTemporaryFolder().getAbsolutePath())
                .isNotEqualTo(classLoader2.getTemporaryFolder().getAbsolutePath());
        assertThat(classLoader1.getTemporaryFolder().getParentFile()).isEqualTo(tempFolder);
        assertThat(classLoader2.getTemporaryFolder().getParentFile()).isEqualTo(tempFolder);
    }

    @Test
    public void should_be_able_to_get_resources_inside_jars() throws Exception {
        final BonitaClassLoader bonitaClassLoader = BonitaClassLoaderFactory.createClassLoader(
                Stream.of(resource("UOSFaasApplication.jar",
                        FileUtils.readFileToByteArray(new File("src/test/resources/UOSFaasApplication.jar")))),
                identifier(PROCESS, 154L), temporaryFolder.newFolder().toURI(),
                testClassLoader);

        URL url = bonitaClassLoader.getResource("au/edu/sydney/faas/applicationstudent/StudentInformation.class");
        assertThat(url).isNotNull();
        assertThat(url.toString())
                .containsIgnoringCase(
                        "!/au/edu/sydney/faas/applicationstudent/StudentInformation.class");

        bonitaClassLoader.destroy();
    }

    @Test
    public void should_be_able_to_use_the_JavaMethodInvoker_concurrently_on_a_BonitaClassLoader() throws Exception {
        final BonitaClassLoader bonitaClassLoader = BonitaClassLoaderFactory.createClassLoader(
                Stream.of(resource("UOSFaasApplication.jar",
                        FileUtils.readFileToByteArray(new File("src/test/resources/UOSFaasApplication.jar")))),
                identifier(PROCESS, 154L), temporaryFolder.newFolder().toURI(),
                testClassLoader);

        final Object objectToInvokeJavaMethodOn = bonitaClassLoader
                .loadClass("au.edu.sydney.faas.applicationstudent.StudentRequest")
                .getConstructors()[0].newInstance();
        final Object valueToSetObjectWith = bonitaClassLoader
                .loadClass("au.edu.sydney.faas.applicationstudent.StudentInformation")
                .getConstructors()[0].newInstance();

        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setContextClassLoader(bonitaClassLoader);
            return t;
        });

        Future<Object> jmiFuture = executor.submit(() -> {
            try {
                JavaMethodInvoker jmi = new JavaMethodInvoker();
                jmi.invokeJavaMethod("au.edu.sydney.faas.applicationstudent.StudentInformation",
                        valueToSetObjectWith, objectToInvokeJavaMethodOn,
                        "setStudentInformation", "au.edu.sydney.faas.applicationstudent.StudentInformation");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        });
        jmiFuture.get();

        // To clean
        bonitaClassLoader.destroy();
    }

    @Test
    public void destroy_should_update_XStream_instance() throws Exception {
        BonitaClassLoader classLoader = BonitaClassLoaderFactory.createClassLoader(Stream.empty(), GLOBAL,
                temporaryFolder.newFolder().toURI(), testClassLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
        // retrieve the XStream instance related to this class loader
        XStream xStreamBeforeDestroy = XStreamFactory.getXStream();

        classLoader.destroy();

        // the XStream instance retrieved after destroy must have changed
        XStream xStreamAfterDestroy = XStreamFactory.getXStream();
        assertThat(xStreamAfterDestroy).isNotSameAs(xStreamBeforeDestroy);
    }

    @Test
    public void should_be_able_to_replace_class_with_same_name_in_different_classloader() throws Exception {
        BonitaClassLoader parent = createClassloader(testClassLoader);
        BonitaClassLoader c1 = createClassloader(parent, resource("jar1.jar", generateJar("Hello",
                "public class Hello{",
                "public String there(){",
                "return \"hello\";",
                "}",
                "}")));
        // Class.forName keep a reference in the classloader. We can't override that, it's an native method
        // We removed VirtualClassLoader to support that use case:
        assertThat(invoke(Class.forName("Hello", false, c1), "there")).isEqualTo("hello");
        assertThat(invoke(c1.loadClass("Hello"), "there")).isEqualTo("hello");

        BonitaClassLoader c2 = createClassloader(resource("jar2.jar", generateJar("Hello",
                "public class Hello{",
                "public String there(){",
                "return \"hello there\";",
                "}",
                "}")));

        assertThat(invoke(Class.forName("Hello", false, c2), "there")).isEqualTo("hello there");
        assertThat(invoke(c2.loadClass("Hello"), "there")).isEqualTo("hello there");
        c1.destroy();
        c2.destroy();
        parent.destroy();
    }

    @Test
    public void should_be_able_to_replace_implementation_of_parent_classloader() throws Exception {
        BonitaClassLoader parent1 = createClassloader(testClassLoader, resource("lib.jar", generateJar("ParentLib",
                "public class ParentLib {",
                "   public String getVersion(){",
                "       return \"1.0\";",
                "   }",
                "}")));

        byte[] childJar = generateJar("Child",
                "public class Child {",
                "   public String getVersion() throws Exception {",
                "       Class parentLibClass = Class.forName(\"ParentLib\");",
                "       return \"Version of the lib in parent is \" + parentLibClass.getMethod(\"getVersion\").invoke(parentLibClass.newInstance());",
                "   }",
                "}");
        BonitaClassLoader child1 = createClassloader(parent1, resource("child.jar", childJar));

        assertThat(invoke(child1.loadClass("Child"), "getVersion")).isEqualTo("Version of the lib in parent is 1.0");

        //We recreate a hierarchy of classloader. We removed VirtualClassLoader so there is no way to change a parent classloader.
        BonitaClassLoader parent2 = createClassloader(resource("lib.jar", generateJar("ParentLib",
                "public class ParentLib {",
                "   public String getVersion(){",
                "       return \"2.0\";",
                "   }",
                "}")));
        BonitaClassLoader child2 = createClassloader(parent2, resource("child.jar", childJar));
        assertThat(invoke(child2.loadClass("Child"), "getVersion")).isEqualTo("Version of the lib in parent is 2.0");
    }

    private BonitaClassLoader createClassloader(BonitaResource... resources) throws IOException {
        return createClassloader(testClassLoader, resources);
    }

    private BonitaClassLoader createClassloader(ClassLoader parent, BonitaResource... resources) throws IOException {
        return BonitaClassLoaderFactory.createClassLoader(Stream.of(resources), identifier(PROCESS, idCounter++),
                temporaryFolder.newFolder().toURI(),
                parent);
    }

    protected Object invoke(Class<?> class1, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        return class1.getMethod(name).invoke(class1.newInstance());
    }

}
