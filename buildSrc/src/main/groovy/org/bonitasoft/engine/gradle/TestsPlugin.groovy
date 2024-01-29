package org.bonitasoft.engine.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

class TestsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def tests = project.extensions.create("tests", TestsExtension)

        TaskProvider<Test> integrationTest = project.tasks.register("integrationTest", Test) {
            group = "Verification"
            description = "Runs all integration tests (except tests from slow suite) on H2 database."
        }

        project.afterEvaluate {
            JVMModifier.setTestJVM(project, integrationTest)
            JVMModifier.setJvmArgs(project, integrationTest)
            if (tests.integrationTestsSuite) {
                integrationTest.configure { include(tests.integrationTestsSuite) }
            } else {
                integrationTest.configure { include(tests.integrationTestsPattern) }
            }
            integrationTest.configure { systemProperty("bonita.version", project.version) }

            if (tests.slowTestsSuite) {
                TaskProvider<Test> slowTest = project.tasks.register("slowTest", Test)
                slowTest.configure { include(tests.slowTestsSuite) }
                JVMModifier.setTestJVM(project, slowTest)
                JVMModifier.setJvmArgs(project, slowTest)
                slowTest.configure { systemProperty("bonita.version", project.version) }
            }

            TaskProvider<Test> testTask = project.tasks.named("test")
            if (testTask) {
                JVMModifier.setTestJVM(project, testTask)
                JVMModifier.setJvmArgs(project, testTask)
                testTask.configure { include(tests.testPattern) }
            }
        }
    }

}
