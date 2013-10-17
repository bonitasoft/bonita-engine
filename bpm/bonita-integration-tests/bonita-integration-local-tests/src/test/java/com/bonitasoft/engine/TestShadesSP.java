package com.bonitasoft.engine;

import org.bonitasoft.engine.TestShades;

public class TestShadesSP extends TestShades {

    @Override
    protected String generateDependencies(final String version) {
        String pom2 = generateDependency("bonita-server-sp", "com.bonitasoft.engine", version);
        pom2 += generateDependency("bonita-client-sp", "com.bonitasoft.engine", version);
        return pom2;
    }
}
