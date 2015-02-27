/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
    public void setUp() {
        fileFilter = new OnlyDAOImplementationFileFilter();
    }

    @Test
    public void should_accept_return_true() {
        File f = new File("EmployeeDAO.class");
        assertThat(fileFilter.accept(f)).isFalse();

        f = new File("EmployeeDAOImpl.class");
        assertThat(fileFilter.accept(f)).isTrue();
    }

    @Test
    public void should_accept_return_false() {
        File f = new File("Employee.class");
        assertThat(fileFilter.accept(f)).isFalse();

        f = new File("Employee.java");
        assertThat(fileFilter.accept(f)).isFalse();
    }

}
