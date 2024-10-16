package org.bonitasoft.engine.gradle.docker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension

/**
 * @author Emmanuel Duchastenier
 */
class DockerDatabasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.configurations {
            drivers
        }
        driversConfiguration(project)
        def databaseIntegrationTest = project.extensions.create("databaseIntegrationTest", DatabasePluginExtension)

        DockerDatabaseContainerTasksCreator.createTasks(project, databaseIntegrationTest)

        project.afterEvaluate {
            if (!databaseIntegrationTest.includes) {
                println "No databaseIntegrationTest.include found. No tests to run!"
            }
        }
    }

    def driversConfiguration(project) {
        project.dependencies {
            // the following jdbc drivers are available for integration tests
            def versionCatalog = project.extensions.getByType(VersionCatalogsExtension.class).named("libs")
            drivers(versionCatalog.findLibrary("mysql").get())
            drivers(versionCatalog.findLibrary("oracle").get())
            drivers(versionCatalog.findLibrary("postgresql").get())
            drivers(versionCatalog.findLibrary("msSqlServer").get())
        }
    }
}
