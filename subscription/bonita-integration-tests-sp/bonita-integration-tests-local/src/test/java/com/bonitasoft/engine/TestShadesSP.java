/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import org.bonitasoft.engine.TestShades;

public class TestShadesSP extends TestShades {

    private static final String COM_BONITASOFT_ENGINE = "com.bonitasoft.engine";

    @Override
    protected String generateDependencies(final String version) {
        String pom2 = generateDependency("bonita-server-sp", COM_BONITASOFT_ENGINE, version);
        pom2 += generateDependency("bonita-client-sp", COM_BONITASOFT_ENGINE, version);
        return pom2;
    }

    @Override
    protected String removedIgnoredBonitaDeps(final String outputOfMaven) {
        String outputOfMaven2 = outputOfMaven.replaceAll(COM_BONITASOFT_ENGINE + ":bonita-server-sp", "");
        outputOfMaven2 = outputOfMaven2.replaceAll(COM_BONITASOFT_ENGINE + ":bonita-client-sp", "");
        outputOfMaven2 = outputOfMaven2.replaceAll(COM_BONITASOFT_ENGINE + ":bonita-common-sp", "");
        return outputOfMaven2;
    }

}
