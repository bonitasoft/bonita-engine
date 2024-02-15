package org.bonitasoft.engine.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test

/**
 * @author Emmanuel Duchastenier
 */
class HttpTestPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        def httpTests = project.extensions.create("httpTests", TestsExtension)

        TaskProvider<Test> httpIntegrationTests = project.tasks.register("httpIT", Test) {
            group = "Verification"
            description = "Runs all integration tests to remote HTTP server."
        }

        project.afterEvaluate {
            if (httpTests.integrationTestsSuite) {
                httpIntegrationTests.configure { include(httpTests.integrationTestsSuite) }
            } else {
                // to be able to run only one class with right-click in IDE
                // Still not possible for now, as *IT classes are not yet in src/test/java folder
                httpIntegrationTests.configure { include("**/*IT.class") }
            }
            // So that TestEngine start in HTTP instead of local:
            httpIntegrationTests.configure { jvmArgs("-Dorg.bonitasoft.engine.access.mode=http") }

            // Add HTTP server dependencies only for this task:
            project.configurations {
                httpTestConfig
            }
            project.dependencies {
                httpTestConfig "org.eclipse.jetty:jetty-server:${Deps.jettyVersion}"
                httpTestConfig "org.eclipse.jetty:jetty-servlet:${Deps.jettyVersion}"
            }
            httpIntegrationTests.configure { classpath += project.configurations.httpTestConfig }


            JVMModifier.setTestJVM(project, httpIntegrationTests)
            JVMModifier.setJvmArgs(project, httpIntegrationTests)
        }
    }

}
