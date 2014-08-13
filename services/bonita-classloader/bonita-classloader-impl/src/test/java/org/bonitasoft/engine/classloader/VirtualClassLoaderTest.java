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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class VirtualClassLoaderTest {

    @Test
    public void loadClassStudentInformation_to_VirtualClassLoarder_should_be_get_as_resource() throws Exception {
        VirtualClassLoader vcl = new VirtualClassLoader("org.bonitasoft", 1L, Thread.currentThread().getContextClassLoader());
        final Map<String, byte[]> resources = new HashMap<String, byte[]>(1);
        resources.put("UOSFaasApplication.jar", FileUtils.readFileToByteArray(new File("src/test/resources/UOSFaasApplication.jar")));
        final File tempDir = new File(System.getProperty("java.io.tmpdir"), "BonitaClassLoaderTest");
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(resources, "here", 154L, tempDir.toURI(), BonitaClassLoader.class.getClassLoader());

        vcl.setClassLoader(bonitaClassLoader);
        URL url = vcl.getResource("au/edu/sydney/faas/applicationstudent/StudentInformation.class");
        assertThat(url).isNotNull();
        assertThat(url.toString())
                .containsIgnoringCase(
                        "!/au/edu/sydney/faas/applicationstudent/StudentInformation.class");
    }

}
