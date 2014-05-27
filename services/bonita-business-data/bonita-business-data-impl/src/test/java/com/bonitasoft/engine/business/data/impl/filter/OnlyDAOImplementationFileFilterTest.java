/**
 * 
 */
package com.bonitasoft.engine.business.data.impl.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Romain
 */
public class OnlyDAOImplementationFileFilterTest {

    private OnlyDAOImplementationFileFilter fileFilter;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        fileFilter = new OnlyDAOImplementationFileFilter();
    }

    @Test
    public void should_accept_return_true() throws Exception {
        File f = new File("EmployeeDAO.class");
        assertThat(fileFilter.accept(f)).isFalse();

        f = new File("EmployeeDAOImpl.class");
        assertThat(fileFilter.accept(f)).isTrue();
    }

    @Test
    public void should_accept_return_false() throws Exception {
        File f = new File("Employee.class");
        assertThat(fileFilter.accept(f)).isFalse();

        f = new File("Employee.java");
        assertThat(fileFilter.accept(f)).isFalse();
    }

}
