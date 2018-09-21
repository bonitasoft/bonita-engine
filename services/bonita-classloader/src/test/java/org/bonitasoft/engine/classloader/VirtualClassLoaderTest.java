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

import static java.util.stream.Stream.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.home.BonitaResource.resource;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.JavaMethodInvoker;
import org.bonitasoft.engine.data.instance.model.impl.XStreamFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.thoughtworks.xstream.XStream;

@RunWith(MockitoJUnitRunner.class)
public class VirtualClassLoaderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private ClassLoader testClassLoader;
    @Mock
    private ClassLoaderListener myClassLoaderListener;
    private VirtualClassLoader localClassLoader;
    private BonitaClassLoader newClassLoader;
    private File tempDir;

    @Before
    public void before() throws IOException {
        testClassLoader = Thread.currentThread().getContextClassLoader();
        localClassLoader = new VirtualClassLoader("org.bonitasoft", 1L, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(localClassLoader);

        newClassLoader = new BonitaClassLoader(empty(), "test", 125,
                File.createTempFile("test", ".tmp").toURI(), testClassLoader);
        tempDir = temporaryFolder.newFolder();
    }

    @After
    public void after() {
        Thread.currentThread().setContextClassLoader(testClassLoader);
    }

    @Test
    public void loadClassStudentInformation_to_VirtualClassLoarder_should_be_get_as_resource() throws Exception {
        VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, Thread.currentThread().getContextClassLoader());
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(Stream.of(resource("UOSFaasApplication.jar", FileUtils.readFileToByteArray(new File("src/test/resources/UOSFaasApplication.jar")))),
                "here", 154L, tempDir.toURI(), BonitaClassLoader.class.getClassLoader());

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
        final VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, Thread.currentThread().getContextClassLoader());
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(Stream.of(resource("UOSFaasApplication.jar", FileUtils.readFileToByteArray(new File("src/test/resources/UOSFaasApplication.jar")))), "here", 154L, tempDir.toURI(), BonitaClassLoader.class.getClassLoader());

        vcl.replaceClassLoader(bonitaClassLoader);
        final Object objectToInvokeJavaMethodOn = vcl.loadClass("au.edu.sydney.faas.applicationstudent.StudentRequest").getConstructors()[0].newInstance();
        final Object valueToSetObjectWith = vcl.loadClass("au.edu.sydney.faas.applicationstudent.StudentInformation").getConstructors()[0].newInstance();

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
                    jmi.invokeJavaMethod("au.edu.sydney.faas.applicationstudent.StudentInformation", valueToSetObjectWith, objectToInvokeJavaMethodOn,
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

    @Test
    public void should_replaceClassLoader_notify_listeners() throws Exception {
        //given
        localClassLoader.addListener(myClassLoaderListener);
        //when
        localClassLoader.replaceClassLoader(newClassLoader);
        //then
        verify(myClassLoaderListener).onUpdate(localClassLoader);
    }

    @Test
    public void should_replaceClassLoader_call_destroy_on_old_classloader() throws Exception {
        //given
        BonitaClassLoader classLoader1 = spy(classloader(1231L));
        BonitaClassLoader classLoader2 = classloader(53412L);
        //when
        localClassLoader.replaceClassLoader(classLoader1);
        localClassLoader.replaceClassLoader(classLoader2);
        //then
        verify(classLoader1).destroy();
    }

    @Test
    public void should_destroy_notify_listeners() throws Exception {
        //given
        localClassLoader.addListener(myClassLoaderListener);
        localClassLoader.replaceClassLoader(newClassLoader);
        //when
        localClassLoader.destroy();
        //then
        verify(myClassLoaderListener).onDestroy(localClassLoader);
    }

    @Test
    public void should_add_same_classloader_do_not_add_it_2_times() throws Exception {
        //given
        localClassLoader.addListener(myClassLoaderListener);
        localClassLoader.addListener(myClassLoaderListener);
        //when
        localClassLoader.replaceClassLoader(newClassLoader);
        //then
        verify(myClassLoaderListener, times(1)).onUpdate(localClassLoader);
    }

    @Test
    public void should_removeListener_remove_listener() throws Exception {
        //given
        localClassLoader.addListener(myClassLoaderListener);
        localClassLoader.removeListener(myClassLoaderListener);
        //when
        localClassLoader.replaceClassLoader(newClassLoader);
        //then
        verify(myClassLoaderListener, never()).onUpdate(localClassLoader);
    }

    @Test
    public void should_notifyUpdate_call_notify_on_children() throws Exception {
        //given
        VirtualClassLoader child = new VirtualClassLoader("child", 12, localClassLoader);
        ClassLoaderListener classLoaderListener = mock(ClassLoaderListener.class);
        child.addListener(classLoaderListener);
        //when
        localClassLoader.replaceClassLoader(newClassLoader);
        //then
        verify(classLoaderListener).onUpdate(child);

    }

    private BonitaClassLoader classloader(long id) {
        return new BonitaClassLoader(Stream.of(resource("test-1.jar", new byte[] { 1, 2, 3 })), "here", id, tempDir.toURI(),
                BonitaClassLoader.class.getClassLoader());
    }
}
