/*
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */
package org.bonitasoft.engine.gradle.docker

import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerInspectContainer
import com.bmuschko.gradle.docker.tasks.container.DockerRemoveContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.extras.DockerWaitHealthyContainer
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.testing.Test

/**
 * Gradle plugin to start docker database containers and perform tests against them
 */
class DockerDatabaseContainerTasksCreator {

    private static String getDockerHost(def project) {
        def dockerHost = System.getenv('DOCKER_HOST')
        if (dockerHost?.trim()) {
            project.logger.quiet("using DOCKER_HOST: ${dockerHost}")
            return new URI(dockerHost).host
        }
        return 'localhost'
    }

    private static final String SYS_PROP_DB_URL = 'db.url'
    private static final String SYS_PROP_DB_USER = 'db.user'
    private static final String SYS_PROP_DB_PASSWORD = 'db.password'

    def static createTasks(Project project, DatabasePluginExtension extension, List vendors) {
        // required to have the environment correctly setup: see https://github.com/bmuschko/gradle-docker-plugin/issues/575#issuecomment-383704012
        if (!project.rootProject.plugins.hasPlugin('com.bmuschko.docker-remote-api')) {
            project.rootProject.plugins.apply('com.bmuschko.docker-remote-api')
        }
        project.plugins.apply('com.bmuschko.docker-remote-api')
        vendors.each { vendor ->
            if (!extension."${vendor.name}".enabled) {
                return // do not create docker tasks for disabled database configurations
            }
            def uniqueName = vendor.name.capitalize()

            DbParser.DbConnectionSettings dbConnectionSettings = new DbParser.DbConnectionSettings()
            DbParser.DbConnectionSettings bdmDbConnectionSettings = new DbParser.DbConnectionSettings()

            def pullImage = project.tasks.register("pull${uniqueName}Image", DockerPullImage) {
                description "Pull docker image for $uniqueName db vendor"
                group null // do not show task when running `gradle tasks`

                image = vendor.image

                if (vendor.registryUrlEnv != null) {
                    registryCredentials.with {
                        url = System.getenv(vendor.registryUrlEnv)
                        username = System.getenv(vendor.registryUsernameEnv)
                        password = System.getenv(vendor.registryPasswordEnv)
                    }
                }
            }

            def createContainer = project.tasks.register("create${uniqueName}Container", DockerCreateContainer) {
                description "Create a docker container for $uniqueName db vendor"
                group null // do not show task when running `gradle tasks`

                if (project.hasProperty("docker-container-alias")) {
                    containerName = project.getProperty("docker-container-alias")
                }
                hostConfig.portBindings = [":$vendor.portBinding"]
                targetImageId pullImage.get().getImage()
                if ('oracle' == vendor.name) {
                    // 1Go
                    hostConfig.shmSize = 1099511627776
                }
                hostConfig.autoRemove = true
            }

            def startContainer = project.tasks.register("start${uniqueName}Container", DockerStartContainer) {
                description "Start a docker container for $uniqueName db vendor"
                group "docker"

                targetContainerId createContainer.get().getContainerId()
            }

            def waitForContainerStartup = project.tasks.register("waitFor${uniqueName}ContainerStartup", DockerWaitHealthyContainer) {
                description "Wait for a started docker container for $vendor.name db vendor to be healthy"
                group null // do not show task when running `gradle tasks`

                targetContainerId startContainer.get().getContainerId()
                awaitStatusTimeout = 360
            }

            def inspectContainer = project.tasks.register("inspect${uniqueName}ContainerUrl", DockerInspectContainer) {
                description = "Get url of a docker container for $uniqueName db vendor"
                group = null // do not show task when running `gradle tasks`

                targetContainerId(startContainer.get().getContainerId())

                onNext {
                    it.networkSettings.ports.getBindings().each { exposedPort, bindingArr ->
                        if (exposedPort.port == vendor.portBinding) {
                            int portBinding = bindingArr.first().hostPortSpec as int
                            def dockerHost = getDockerHost(project)
                            dbConnectionSettings.dbUrl = vendor.name == "oracle" ? String.format(vendor.uriTemplate, dockerHost, portBinding) : String.format(vendor.uriTemplate, dockerHost, portBinding, "bonita")
                            dbConnectionSettings.serverName = dockerHost
                            dbConnectionSettings.portNumber = portBinding
                            bdmDbConnectionSettings.dbUrl = vendor.name == "oracle" ? String.format(vendor.uriTemplate, dockerHost, portBinding) : String.format(vendor.uriTemplate, dockerHost, portBinding, "business_data")
                            bdmDbConnectionSettings.serverName = dockerHost
                            bdmDbConnectionSettings.portNumber = portBinding
                            project.logger.quiet("db.url set to ${dbConnectionSettings.dbUrl}")
                        }
                    }
                }
            }

            def removeContainer = project.tasks.register("remove${uniqueName}Container", DockerRemoveContainer) {
                description "Remove a docker container for $uniqueName db vendor"
                group "docker"

                force = true
                removeVolumes = true
                targetContainerId createContainer.get().getContainerId()
            }

            TaskProvider databaseTestTask = project.tasks.register("${vendor.name}DatabaseTest", Test) {
                group = "Verification"
                description = "Runs slow integration test suite on $vendor.name database."
                systemProperty "bonita.version", project.version
                jvmArgs += ['--add-opens', 'java.base/java.util=ALL-UNNAMED', '--add-opens', 'java.base/java.lang=ALL-UNNAMED', '-Dfile.encoding=UTF-8']
                if (extension."${vendor.name}"?.includeTestModule) {
                    testClassesDirs += extension."${vendor.name}".includeTestModule.sourceSets.test.output.classesDirs
                    classpath += extension."${vendor.name}".includeTestModule.sourceSets.test.runtimeClasspath
                }
                classpath += project.files(project.configurations.drivers)
                if(extension."${vendor.name}"?.excludes) {
                    exclude(extension."${vendor.name}".excludes)
                }
                onlyIf { extension."${vendor.name}"?.enabled }

                doFirst {
                    String dbUrl = project.hasProperty(SYS_PROP_DB_URL) ? project.property(SYS_PROP_DB_URL) : dbConnectionSettings.dbUrl
                    def connectionSettings = DbParser.extractDbConnectionSettings(dbUrl)
                    def dbValues = [
                            "sysprop.bonita.db.vendor"    : vendor.name,
                            "sysprop.bonita.bdm.db.vendor": vendor.name,
                            "db.url"                      : dbUrl,
                            "db.user"                     : project.hasProperty('db.user') ? project.property(SYS_PROP_DB_URL) : (System.getProperty(SYS_PROP_DB_USER) ? System.getProperty(SYS_PROP_DB_USER) : 'bonita'),
                            "db.password"                 : project.hasProperty('db.password') ? project.property(SYS_PROP_DB_URL) : (System.getProperty(SYS_PROP_DB_PASSWORD) ? System.getProperty(SYS_PROP_DB_PASSWORD) : 'bpm'),
                            "bdm.db.url"                  : bdmDbConnectionSettings.dbUrl,
                            "bdm.db.user"                 : project.hasProperty('db.user') ? project.property(SYS_PROP_DB_URL) : (System.getProperty(SYS_PROP_DB_USER) ? System.getProperty(SYS_PROP_DB_USER) : 'business_data'),
                            "db.server.name"              : connectionSettings.serverName,
                            "db.server.port"              : connectionSettings.portNumber,
                            "db.database.name"            : connectionSettings.databaseName
                    ]

                    if ('oracle' == vendor.name) {
                        // fix for https://community.oracle.com/message/3701989
                        // http://www.thezonemanager.com/2015/07/whats-so-special-about-devurandom.html
                        dbValues.put('java.security.egd', 'file:/dev/./urandom')
                        // fix for ORA-01882
                        dbValues.put('user.timezone', 'UTC')
                    }
                    //  /!\ warning: do NOT use setSystemProperties, as it would erase existing system properties.
                    // rather use systemProperties to merge the new ones with the existing ones.
                    systemProperties(dbValues)
                }
            }

            TaskProvider zipReport = project.tasks.register("zip${vendor.name}DatabaseTestReport" as String, Zip) {
                archiveFileName = databaseTestTask.get().reports.html.outputLocation.get().getAsFile().name + ".zip"
                destinationDirectory = databaseTestTask.get().reports.html.outputLocation.get().getAsFile().parentFile
                from databaseTestTask.get().reports.html.outputLocation.get().getAsFile()
            }
            project.afterEvaluate {
                databaseTestTask.configure { includes = extension.includes }
                pullImage.configure { onlyIf { extension."${vendor.name}"?.enabled } }
                createContainer.configure { onlyIf { extension."${vendor.name}"?.enabled } }
                startContainer.configure { onlyIf { extension."${vendor.name}"?.enabled } }
                waitForContainerStartup.configure { onlyIf { extension."${vendor.name}"?.enabled } }
                inspectContainer.configure { onlyIf { extension."${vendor.name}"?.enabled } }
                removeContainer.configure { onlyIf { extension."${vendor.name}"?.enabled } }
            }

            if (createContainer) {
                createContainer.configure { dependsOn(pullImage) }
            }
            if (startContainer) {
                startContainer.configure {
                    dependsOn(createContainer)
                    finalizedBy(removeContainer)
                }
            }
            if (waitForContainerStartup) {
                waitForContainerStartup.configure { dependsOn(startContainer) }
            }
            if (inspectContainer) {
                inspectContainer.configure { dependsOn(waitForContainerStartup) }
            }
            if (databaseTestTask) {
                databaseTestTask.configure {
                    dependsOn(inspectContainer)
                    finalizedBy(zipReport)
                }
            }
            if (removeContainer) {
                removeContainer.configure { mustRunAfter(databaseTestTask) }
            }
        }
    }

}
