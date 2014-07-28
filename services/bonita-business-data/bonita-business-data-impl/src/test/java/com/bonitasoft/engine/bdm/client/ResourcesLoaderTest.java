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
