/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
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

public class WithoutDAOImplementationFileFilterTest {

    private WithoutDAOImplementationFileFilter fileFilter;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() {
        fileFilter = new WithoutDAOImplementationFileFilter();
    }

    @Test
    public void should_accept_return_false() {
        String[] rejectedFiles = new String[] { "EmployeeDAOImpl.class", "EmployeeDAOImpl.java", "com.bonitasoft.EmployeeDAOImpl.class",
                "com" + File.separatorChar + "bonitasoft" + File.separatorChar + "EmployeeDAOImpl.java",
                "com" + File.separatorChar + "company" + File.separatorChar + "EmployeeDAOImpl.class",
                "com" + File.separatorChar + "company" + File.separatorChar + "EmployeeDAOImpl.java",

                "com" + File.separatorChar + "bonitasoft" + File.separatorChar + "Employee.class",
                "com" + File.separatorChar + "bonitasoft" + File.separatorChar + "Employee.java",
        };

        for (String rejectedFile : rejectedFiles) {
            File f = new File(rejectedFile);
            assertThat(fileFilter.accept(f)).isFalse();
        }

    }

    @Test
    public void should_accept_return_true() {
        String[] validFiles = new String[] { "Employee.class", "Employee.java",
                "com" + File.separatorChar + "bonitasoftextended" + File.separatorChar + "Employee.class",
                "com" + File.separatorChar + "bonitasoftextended" + File.separatorChar + "Employee.java",
                "com" + File.separatorChar + "company" + File.separatorChar + "Employee.class",
                "com" + File.separatorChar + "company" + File.separatorChar + "Employee.java" };

        for (String validFile : validFiles) {
            File f = new File(validFile);
            assertThat(fileFilter.accept(f)).isTrue();
        }

    }

}
