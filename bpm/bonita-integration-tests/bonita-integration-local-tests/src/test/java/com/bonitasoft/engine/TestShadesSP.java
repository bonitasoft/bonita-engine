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
    protected String removedIgnoredBonitaDeps(String outputOfMaven) {
        outputOfMaven = outputOfMaven.replaceAll(COM_BONITASOFT_ENGINE + ":bonita-server-sp", "");
        outputOfMaven = outputOfMaven.replaceAll(COM_BONITASOFT_ENGINE + ":bonita-client-sp", "");
        outputOfMaven = outputOfMaven.replaceAll(COM_BONITASOFT_ENGINE + ":bonita-common-sp", "");
        return outputOfMaven;
    }

}
