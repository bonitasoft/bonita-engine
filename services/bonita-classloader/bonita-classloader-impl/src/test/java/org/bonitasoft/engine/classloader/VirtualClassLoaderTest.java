package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.*;

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
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(resources, "here", 154L, tempDir.getPath(), BonitaClassLoader.class.getClassLoader());

        vcl.setClassLoader(bonitaClassLoader);
        URL url = vcl.getResource("au/edu/sydney/faas/applicationstudent/StudentInformation.class");
        assertThat(url).isNotNull();
        assertThat(url.toString())
                .containsIgnoringCase(
                        "!/au/edu/sydney/faas/applicationstudent/StudentInformation.class");
    }

}
