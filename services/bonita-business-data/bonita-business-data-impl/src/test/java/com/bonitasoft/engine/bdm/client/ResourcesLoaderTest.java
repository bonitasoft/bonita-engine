/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.bonitasoft.engine.io.IOUtils;


public class ResourcesLoaderTest {

    @Test
    public void should_copy_files_from_package_in_classpath_to_destination_directory() throws Exception {
        String packageName = "com.bonitasoft.engine.package";
        File directory = IOUtils.createTempDirectory("bdmtest");
        
        new ResourcesLoader().copyJavaFilesToDirectory(packageName, directory);
        
        assertThat(new File(directory, "com/bonitasoft/engine/package/Test.java")).exists();
        assertThat(new File(directory, "com/bonitasoft/engine/package/subfolder/Other.java")).exists();
        FileUtils.deleteDirectory(directory);
    }
}
