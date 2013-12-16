package org.bonitasoft.engine.commons.io;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;

public class IOUtilTest {

    @Test
    public void testGetClassNameList() throws Exception {
        // given:
        byte[] jarContent = IOUtil.getAllContentFrom(new File(IOUtilTest.class.getResource("bdr-jar.bak").getFile()));

        // when:
        List<String> classNameList = IOUtil.getClassNameList(jarContent);

        // then:
        assertThat(classNameList).containsOnly("org.bonita.pojo.Employee");
    }
}
