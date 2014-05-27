package com.bonitasoft.engine.business.data.impl.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class WithoutDAOImplementationFileFilterTest {

    private WithoutDAOImplementationFileFilter fileFilter;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        fileFilter = new WithoutDAOImplementationFileFilter();
    }

    @Test
    public void should_accept_return_false() throws Exception {
        File f = new File("EmployeeDAOImpl.class");
        assertThat(fileFilter.accept(f)).isFalse();

        f = new File("EmployeeDAOImpl.java");
        assertThat(fileFilter.accept(f)).isFalse();
    }

    @Test
    public void should_accept_return_true() throws Exception {
        File f = new File("Employee.class");
        assertThat(fileFilter.accept(f)).isTrue();

        f = new File("Employee.java");
        assertThat(fileFilter.accept(f)).isTrue();
    }

}
