package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class BonitaClassLoaderTest {

    @Test
    public void releaseShouldRemoveAllScopeFolderAndItsContent() {
        final Map<String, byte[]> resources = new HashMap<String, byte[]>(1);
        resources.put("myJar.jar", "Salut le monde".getBytes());
        final File tempDir = new File(System.getProperty("java.io.tmpdir"), "BonitaClassLoaderTest");
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(resources, "here", 154L, tempDir.getPath(), BonitaClassLoader.class.getClassLoader());

        bonitaClassLoader.release();

        assertThat(tempDir).doesNotExist();

    }

}
