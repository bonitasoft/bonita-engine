package com.bonitasoft.engine.compiler;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AssertionInfo;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.TestClass;


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
        
        String[] jars = classPathResolver.getJarsPath(TestClass.class, Assertions.class);
        
        assertThat(jars[0]).contains("junit").endsWith(".jar");
        assertThat(jars[1]).contains("assertj-core").endsWith(".jar");
    }
    
    @Test
    public void should_return_jar_only_once_for_two_classes_belonging_to_same_jar() throws Exception {
        
        String[] jars = classPathResolver.getJarsPath(AssertionInfo.class, Assertions.class);
        
        assertThat(jars).hasSize(1);
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_runtime_exception_if_jar_not_found() throws Exception {
        classPathResolver.getJarsPath(new Class[] { null });
    }
}
