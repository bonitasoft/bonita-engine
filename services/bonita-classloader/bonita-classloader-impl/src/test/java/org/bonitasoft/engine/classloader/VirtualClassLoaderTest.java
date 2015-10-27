/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.JavaMethodInvoker;
import org.bonitasoft.engine.data.instance.model.impl.XStreamFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VirtualClassLoaderTest {

    @Mock
    private TechnicalLoggerService loggerService;

    @Before
    public void setUp() throws Exception {
        given(loggerService.isLoggable(Matchers.<Class<?>>any(), any(TechnicalLogSeverity.class))).willReturn(true);
    }

    @Test
    public void loadClassStudentInformation_to_VirtualClassLoarder_should_be_get_as_resource() throws Exception {
        VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, Thread.currentThread().getContextClassLoader(), loggerService);
        final Map<String, byte[]> resources = new HashMap<String, byte[]>(1);
        resources.put("UOSFaasApplication.jar", FileUtils.readFileToByteArray(new File("src/test/resources/UOSFaasApplication.jar")));
        final File tempDir = new File(System.getProperty("java.io.tmpdir"), "VirtualClassLoaderTest");
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(resources, "here", 154L, tempDir.toURI(), BonitaClassLoader.class.getClassLoader());

        vcl.setClassLoader(bonitaClassLoader);
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
        final VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, Thread.currentThread().getContextClassLoader(), loggerService);
        final Map<String, byte[]> resources = new HashMap<String, byte[]>(1);
        resources.put("UOSFaasApplication.jar", FileUtils.readFileToByteArray(new File("src/test/resources/UOSFaasApplication.jar")));
        final File tempDir = new File(System.getProperty("java.io.tmpdir"), "VirtualClassLoaderTest");
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(resources, "here", 154L, tempDir.toURI(), BonitaClassLoader.class.getClassLoader());

        vcl.setClassLoader(bonitaClassLoader);
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
        final VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, previousClassLoader, loggerService);
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
    public void destroy_should_call_all_classLoaderChangeHandlers() throws Exception {
        //given
        // set class loader to new VirtualClassLoader
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, contextClassLoader, loggerService);
        ClassLoaderChangeHandler handler1 = mock(ClassLoaderChangeHandler.class);
        ClassLoaderChangeHandler handler2 = mock(ClassLoaderChangeHandler.class);
        vcl.addChangeHandler(handler1);
        vcl.addChangeHandler(handler2);

        //when
        // destroy the VirtualClassLoader
        vcl.destroy();

        //then
        verify(handler1).onDestroy();
        verify(handler2).onDestroy();
    }

    @Test
    public void destroy_should_log_and_execute_following_classLoaderChangeHandlers_on_exception() throws Exception {
        //given
        // set class loader to new VirtualClassLoader
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, contextClassLoader, loggerService);
        SClassLoaderException exception = new SClassLoaderException("somme error");
        ClassLoaderChangeHandler handler1 = new ThrowErrorClassLoaderChangeHandler(exception);

        ClassLoaderChangeHandler handler2 = mock(ClassLoaderChangeHandler.class);
        vcl.addChangeHandler(handler1);
        vcl.addChangeHandler(handler2);

        //when
        // destroy the VirtualClassLoader
        vcl.destroy();

        //then
        verify(loggerService).log(Matchers.<Class<?>>any(), eq(TechnicalLogSeverity.WARNING), contains(ThrowErrorClassLoaderChangeHandler.class.getName()), eq(exception));
        verify(handler2).onDestroy();
    }
}
