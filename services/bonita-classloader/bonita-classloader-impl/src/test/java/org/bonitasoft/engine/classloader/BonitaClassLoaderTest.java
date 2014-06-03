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
        final File tempDir = new File(System.getProperty("java.io.tmpdir"), "BonitaClassLoaderTest" + System.currentTimeMillis());

        // given
        assertThat(tempDir).as("tempDir:%s should not exists before bonitaClassLoader init", tempDir.getAbsolutePath()).doesNotExist();
        final BonitaClassLoader bonitaClassLoader = new BonitaClassLoader(resources, "here", 154L, tempDir.getPath(), BonitaClassLoader.class.getClassLoader());
        assertThat(tempDir).as("bonitaClassLoader tempDir:%s should exists after bonitaClassLoader creation", tempDir.getAbsolutePath()).exists();

        // when
        bonitaClassLoader.release();

        // then
        assertThat(tempDir).as("bonitaClassLoader tempDir:%s should not exists after bonitaClassLoader release", tempDir.getAbsolutePath()).doesNotExist();
    }

}
