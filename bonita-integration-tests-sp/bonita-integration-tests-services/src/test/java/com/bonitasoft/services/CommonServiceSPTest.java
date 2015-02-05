/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.services;

import org.bonitasoft.engine.CommonServiceTest;
import org.bonitasoft.engine.TestsInitializerService;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.junit.runner.RunWith;

/**
 * @author Baptiste Mesta
 */
@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializerService.class)
public class CommonServiceSPTest extends CommonServiceTest {

    private static ServicesBuilder servicesBuilder;

    static {
        servicesBuilder = new ServicesBuilder();
    }

    public static ServicesBuilder getServicesBuilder() {
        return servicesBuilder;
    }

}
