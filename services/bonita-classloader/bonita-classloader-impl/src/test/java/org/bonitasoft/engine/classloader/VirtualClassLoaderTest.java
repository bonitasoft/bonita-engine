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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.JavaMethodInvoker;
import org.junit.Test;

public class VirtualClassLoaderTest {

    @Test
    public void loadClassStudentInformation_to_VirtualClassLoarder_should_be_get_as_resource() throws Exception {
        VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, Thread.currentThread().getContextClassLoader());
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
        final VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, Thread.currentThread().getContextClassLoader());
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
}
