package org.bonitasoft.engine.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class TestsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def tests = project.extensions.create("tests", TestsExtension)

        Test integrationTest = project.tasks.create("integrationTest", Test) {
            group = "Verification"
            description = "Runs all integration tests (except tests from slow suite) on H2 database."
        } as Test

        project.afterEvaluate {

            JVMModifier.setTestJVM(project, integrationTest)
            JVMModifier.setJvmArgs(project, integrationTest)
            if (tests.integrationTestsSuite) {
                integrationTest.include(tests.integrationTestsSuite)
            } else {
                integrationTest.include(tests.integrationTestsPattern)
            }
            integrationTest.systemProperty("bonita.version", project.version)

            if (tests.slowTestsSuite) {
                Test slowTest = project.tasks.create("slowTest", Test)
                slowTest.include(tests.slowTestsSuite)
                JVMModifier.setTestJVM(project, slowTest)
                JVMModifier.setJvmArgs(project, slowTest)
                slowTest.systemProperty("bonita.version", project.version)
            }

            Test testTask = project.tasks.getByName("test") as Test
            if (testTask) {
                JVMModifier.setTestJVM(project, testTask)
                JVMModifier.setJvmArgs(project, testTask)
                testTask.include(tests.testPattern)
            }
        }
    }

}
