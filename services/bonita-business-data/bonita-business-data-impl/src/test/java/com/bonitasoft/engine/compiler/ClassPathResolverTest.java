package com.bonitasoft.engine.compiler;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;


public class ClassPathResolverTest {

    private ClassPathResolver classPathResolver;

    @Before
    public void setUp() {
        classPathResolver = new ClassPathResolver();
    }
    
    @Test
    public void should_find_jar_in_classpath_according_to_given_class() throws Exception {
        
        String[] jars = classPathResolver.getJarsPath(Assertions.class);
        
        assertThat(jars[0]).contains("assertj-core").endsWith(".jar");
    }

    @Test
    public void should_find_multiple_jar_for_multiple_given_classes() throws Exception {
        
        String[] jars = classPathResolver.getJarsPath(String.class, Assertions.class);
        
        assertThat(jars[0]).endsWith("rt.jar");
        assertThat(jars[1]).contains("assertj-core").endsWith(".jar");
    }
    
    @Test
    public void should_return_jar_only_once_for_two_classes_belonging_to_same_jar() throws Exception {
        
        String[] jars = classPathResolver.getJarsPath(AssertionInfo.class, Assertions.class);
        
        assertThat(jars).hasSize(1);
    }

}
