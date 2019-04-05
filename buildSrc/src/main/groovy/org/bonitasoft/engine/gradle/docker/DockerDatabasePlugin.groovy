package org.bonitasoft.engine.gradle.docker

import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * @author Emmanuel Duchastenier
 */
class DockerDatabasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.configurations {
            drivers
        }

        defineJdbcDriversConfiguration(project)

        def databaseIntegrationTest = project.extensions.create("databaseIntegrationTest", DatabasePluginExtension)

        DockerDatabaseContainerTasksCreator.createTasks(project, databaseIntegrationTest)

        project.afterEvaluate {
            if (!databaseIntegrationTest.includes) {
                println "No databaseIntegrationTest.include found. No tests to run!"
            }
        }
    }

    def defineJdbcDriversConfiguration(Project project) {
        project.dependencies {
            // the following jdbc drivers are available for integration tests
            drivers JdbcDriverDependencies.mysql
            drivers JdbcDriverDependencies.oracle
            drivers JdbcDriverDependencies.postgres
            drivers JdbcDriverDependencies.sqlserver
        }
    }
}
