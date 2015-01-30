/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import com.bonitasoft.engine.TestsInitializerSP;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(BonitaSuiteRunner.class)
@SuiteClasses({
        ApplicationAPIApplicationIT.class,
        ApplicationAPIApplicationPageIT.class,
        ApplicationAPIApplicationMenuIT.class,
        ApplicationAPIImportExportIT.class
})
@Initializer(TestsInitializerSP.class)
public class ApplicationAPIITs {
}
