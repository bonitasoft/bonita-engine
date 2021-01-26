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

import static java.util.stream.Stream.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bonitasoft.engine.home.BonitaResource.resource;
import static org.bonitasoft.engine.io.IOUtil.generateJar;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
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
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VirtualClassLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private ClassLoader testClassLoader;
    @Mock
    private SingleClassLoaderListener mySingleClassLoaderListener;
    private VirtualClassLoader localClassLoader;
    private BonitaClassLoader newClassLoader;
    private int idCounter = 1;

    @Before
    public void before() throws IOException {
        testClassLoader = Thread.currentThread().getContextClassLoader();
        localClassLoader = new VirtualClassLoader("org.bonitasoft", 1L, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(localClassLoader);

        newClassLoader = new BonitaClassLoader(empty(), "test", 125,
                File.createTempFile("test", ".tmp").toURI(), testClassLoader);
    }

    @After
    public void after() {
        Thread.currentThread().setContextClassLoader(testClassLoader);
    }

    @Test
    public void loadClassStudentInformation_to_VirtualClassLoarder_should_be_get_as_resource() throws Exception {
        VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L,
                Thread.currentThread().getContextClassLoader());
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(
                Stream.of(resource("UOSFaasApplication.jar",
                        FileUtils.readFileToByteArray(new File("src/test/resources/UOSFaasApplication.jar")))),
                "here", 154L, temporaryFolder.newFolder().toURI(), BonitaClassLoader.class.getClassLoader());

        vcl.replaceClassLoader(bonitaClassLoader);
        URL url = vcl.getResource("au/edu/sydney/faas/applicationstudent/StudentInformation.class");
        assertThat(url).isNotNull();
        assertThat(url.toString())
                .containsIgnoringCase(
                        "!/au/edu/sydney/faas/applicationstudent/StudentInformation.class");

        // To clean
        bonitaClassLoader.destroy();
    }

    /**
     * BS-7152 : test the loading of class when calling the JavaMethodInvoker
     *
     * @throws Exception
     */
    @Test
    public void loadStudentInformation_toVirtualClassLoader_should_be_usable_via_JavaMethodInvoker() throws Exception {
        final VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L,
                Thread.currentThread().getContextClassLoader());
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(
                Stream.of(resource("UOSFaasApplication.jar",
                        FileUtils.readFileToByteArray(new File("src/test/resources/UOSFaasApplication.jar")))),
                "here", 154L, temporaryFolder.newFolder().toURI(), BonitaClassLoader.class.getClassLoader());

        vcl.replaceClassLoader(bonitaClassLoader);
        final Object objectToInvokeJavaMethodOn = vcl.loadClass("au.edu.sydney.faas.applicationstudent.StudentRequest")
                .getConstructors()[0].newInstance();
        final Object valueToSetObjectWith = vcl.loadClass("au.edu.sydney.faas.applicationstudent.StudentInformation")
                .getConstructors()[0].newInstance();

        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setContextClassLoader(vcl);
                return t;
            }
        });

        Future<Object> jmiFuture = executor.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                try {
                    JavaMethodInvoker jmi = new JavaMethodInvoker();
                    jmi.invokeJavaMethod("au.edu.sydney.faas.applicationstudent.StudentInformation",
                            valueToSetObjectWith, objectToInvokeJavaMethodOn,
                            "setStudentInformation", "au.edu.sydney.faas.applicationstudent.StudentInformation");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
        jmiFuture.get();

        // To clean
        bonitaClassLoader.destroy();
    }

    @Test
    public void destroy_should_update_XStream_instance() throws Exception {
        //given
        // set class loader to new VirtualClassLoader
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        final VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, previousClassLoader);
        Thread.currentThread().setContextClassLoader(vcl);

        // retrieve the XStream instance related to this class loader
        XStream xStreamBeforeDestroy = XStreamFactory.getXStream();

        //when
        // destroy the VirtualClassLoader
        vcl.destroy();

        //then
        // the XStream instance retrieved after destroy must have changed
        XStream xStreamAfterDestroy = XStreamFactory.getXStream();
        assertThat(xStreamAfterDestroy).isNotSameAs(xStreamBeforeDestroy);

        //clean up
        Thread.currentThread().setContextClassLoader(previousClassLoader);
    }

    private BonitaClassLoader createClassloader(BonitaResource... resources) throws IOException {
        return createClassloader(testClassLoader, resources);
    }

    private BonitaClassLoader createClassloader(ClassLoader parent, BonitaResource... resources) throws IOException {
        return new BonitaClassLoader(Stream.of(resources), "someType", idCounter++,
                temporaryFolder.newFolder().toURI(),
                parent);
    }

    @Test
    public void should_be_able_to_replace_class_with_same_name_in_different_classloader() throws Exception {
        VirtualClassLoader mainClassLoader = new VirtualClassLoader("type1", 1, testClassLoader);
        mainClassLoader.replaceClassLoader(createClassloader(resource("jar1.jar", generateJar("Hello",
                "public class Hello{",
                "public String there(){",
                "return \"hello\";",
                "}",
                "}"))));
        // Class.forName keep a reference in the classloader. We can't override that, it's an native method
        // That is one reason why we should remove virtual classloaders
        //        assertThat(invoke(Class.forName("Hello", false, mainClassLoader), "there")).isEqualTo("hello");
        assertThat(invoke(mainClassLoader.loadClass("Hello"), "there")).isEqualTo("hello");

        mainClassLoader.replaceClassLoader(createClassloader(resource("jar2.jar", generateJar("Hello",
                "public class Hello{",
                "public String there(){",
                "return \"hello there\";",
                "}",
                "}"))));

        //        assertThat(invoke(Class.forName("Hello", false, mainClassLoader), "there")).isEqualTo("hello there");
        assertThat(invoke(mainClassLoader.loadClass("Hello"), "there")).isEqualTo("hello there");
    }

    @Test
    public void should_be_able_to_replace_implementation_of_parent_classloader() throws Exception {
        VirtualClassLoader mainClassLoader = new VirtualClassLoader("type1", 1, testClassLoader);

        mainClassLoader.replaceClassLoader(createClassloader(resource("lib.jar", generateJar("ParentLib",
                "public class ParentLib {",
                "   public String getVersion(){",
                "       return \"1.0\";",
                "   }",
                "}"))));
        VirtualClassLoader childClassLoader = new VirtualClassLoader("child", 2, mainClassLoader);
        byte[] childJar = generateJar("Child",
                "public class Child {",
                "   public String getVersion() throws Exception {",
                "       Class parentLibClass = Class.forName(\"ParentLib\");",
                // When we remove the Class.forName to load the ParentLib class, it works. Class.forName is native and keep the class loaded in the child.
                //                "       Class parentLibClass = Child.class.getClassLoader().loadClass(\"ParentLib\");",
                "       return \"Version of the lib in parent is \" + parentLibClass.getMethod(\"getVersion\").invoke(parentLibClass.newInstance());",
                "   }",
                "}");
        childClassLoader.replaceClassLoader(createClassloader(mainClassLoader, resource("child.jar", childJar)));

        assertThat(invoke(childClassLoader.loadClass("Child"), "getVersion"))
                .isEqualTo("Version of the lib in parent is 1.0");

        mainClassLoader.replaceClassLoader(createClassloader(resource("lib.jar", generateJar("ParentLib",
                "public class ParentLib {",
                "   public String getVersion(){",
                "       return \"2.0\";",
                "   }",
                "}"))));
        assertThatThrownBy(() -> childClassLoader.loadClass("Child")).isInstanceOf(ClassNotFoundException.class)
                .hasMessageContaining("Child"); // Because all children classloaders have been invalidated

        childClassLoader.replaceClassLoader(createClassloader(mainClassLoader, resource("child.jar", childJar)));
        assertThat(invoke(childClassLoader.loadClass("Child"), "getVersion"))
                .isEqualTo("Version of the lib in parent is 2.0");
    }

    protected Object invoke(Class<?> class1, String name)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        return class1.getMethod(name).invoke(class1.newInstance());
    }
}
