package org.bonitasoft.engine.gradle

class TestsExtension {
    String testPattern = "**/*Test.class"
    String integrationTestsPattern = "**/*IT.class"
    String integrationTestsSuite
    String slowTestsSuite
}
