/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import com.bonitasoft.engine.test.TestEngineSP;
import org.bonitasoft.engine.LocalServerTestsInitializer;

public class LocalServerTestsInitializerSP extends LocalServerTestsInitializer {


    private static APITestSPUtil testUtil = new APITestSPUtil();

    private static TestEngineSP _INSTANCE;

    public static void beforeAll() throws Exception {
        LocalServerTestsInitializerSP.getInstance().start();
    }

    public static TestEngineSP getInstance() {
        if (_INSTANCE == null) {
            _INSTANCE = new TestEngineSP();
        }
        return _INSTANCE;
    }

    public static void afterAll() throws Exception {
        LocalServerTestsInitializerSP.getInstance().stop();
    }



}
