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

        DockerDatabaseContainerTasksCreator.createTasks(project, databaseIntegrationTest, getVendors())

        project.afterEvaluate {
            if (!databaseIntegrationTest.includes) {
                println "No databaseIntegrationTest.include found. No tests to run!"
            }
        }
    }

    def driversConfiguration(project) {
        project.dependencies {
            // the following jdbc drivers are available for integration tests
            drivers(project.extensions.getByType(VersionCatalogsExtension.class).named("libs")
                    .findLibrary("postgresql").get())
        }
    }

    List getVendors() {
        return [
                [name       : 'postgres',
                 image      : 'bonitasoft/bonita-postgres:16.4',
                 portBinding: 5432,
                 uriTemplate: 'jdbc:postgresql://%s:%s/%s',
                ]
        ]
    }

}
