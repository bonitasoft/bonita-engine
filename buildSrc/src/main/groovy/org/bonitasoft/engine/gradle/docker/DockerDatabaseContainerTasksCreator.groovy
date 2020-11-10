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
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.testing.Test

/**
 * Gradle plugin to start docker database containers and perform tests against them
 */
class DockerDatabaseContainerTasksCreator {

    def static vendors = [
            [name       : 'oracle',
             repository : 'registry.rd.lan/bonitasoft/oracle-19c-ee',
             tag        : '0.0.1',
             portBinding: 1521,
             uriTemplate: 'jdbc:oracle:thin:@//%s:%s/ORCLPDB1',
            ],
            [name       : 'postgres',
             repository : 'bonitasoft/bonita-postgres',
             tag        : '11.9',
             portBinding: 5432,
             uriTemplate: 'jdbc:postgresql://%s:%s/%s',
            ],
            [name       : 'mysql',
             repository : 'bonitasoft/bonita-mysql',
             tag        : '8.0.22',
             portBinding: 3306,
             uriTemplate: 'jdbc:mysql://%s:%s/%s?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8',
            ],
            [name       : 'sqlserver',
             repository : 'registry.rd.lan/bonitasoft/sqlserver-2017',
             tag        : 'CU19-1.0.0',
             portBinding: 1433,
             uriTemplate: 'jdbc:sqlserver://%s:%s;database=%s',
            ]
    ]

    private static String getDockerHost() {
        def dockerHost = System.getenv('DOCKER_HOST')
        if (dockerHost?.trim()) {
            return new URI(dockerHost).host
        }
        return 'localhost'
    }

    private static final String SYS_PROP_DB_URL = 'db.url'
    private static final String SYS_PROP_DB_USER = 'db.user'
    private static final String SYS_PROP_DB_PASSWORD = 'db.password'

    def static createTasks(Project project, DatabasePluginExtension extension) {
        // required to have the environment correctly setup: see https://github.com/bmuschko/gradle-docker-plugin/issues/575#issuecomment-383704012
        if (!project.rootProject.plugins.hasPlugin('com.bmuschko.docker-remote-api')) {
            project.rootProject.plugins.apply('com.bmuschko.docker-remote-api')
        }
        project.plugins.apply('com.bmuschko.docker-remote-api')
        vendors.each { vendor ->
            def uniqueName = "${vendor.name.capitalize()}"

            DbParser.DbConnectionSettings dbConnectionSettings = new DbParser.DbConnectionSettings()
            DbParser.DbConnectionSettings bdmDbConnectionSettings = new DbParser.DbConnectionSettings()
            Task inspectContainer
            Task removeContainer

            def pullImage = createTaskInRootProject(project, "pull${uniqueName}Image", DockerPullImage) {
                description "Pull docker image for $uniqueName db vendor"
                group null // do not show task when running `gradle tasks`

                repository = vendor.repository
                tag = vendor.tag
            }

            def createContainer = createTaskInRootProject(project, "create${uniqueName}Container", DockerCreateContainer) {
                description "Create a docker container for $uniqueName db vendor"
                group null // do not show task when running `gradle tasks`

                if (project.hasProperty("docker-container-alias")) {
                    containerName = project.getProperty("docker-container-alias")
                }
                portBindings = [":$vendor.portBinding"]
                targetImageId pullImage.getImageId()
                if (vendor.name == 'oracle') {
                    // 1Go
                    shmSize = 1099511627776
                }
            }

            def startContainer = createTaskInRootProject(project, "start${uniqueName}Container", DockerStartContainer) {
                description "Start a docker container for $uniqueName db vendor"
                group "docker"

                targetContainerId createContainer.getContainerId()
            }

            def waitForContainerStartup = createTaskInRootProject(project, "waitFor${uniqueName}ContainerStartup", DockerWaitHealthyContainer) {
                description "Wait for a started docker container for $vendor.name db vendor to be healthy"
                group null // do not show task when running `gradle tasks`

                targetContainerId startContainer.getContainerId()
                awaitStatusTimeout = 360
            }

            inspectContainer = project.tasks.create("inspect${uniqueName}ContainerUrl", DockerInspectContainer) {
                description "Get url of a docker container for $uniqueName db vendor"
                group null // do not show task when running `gradle tasks`

                targetContainerId startContainer.getContainerId()

                onNext {
                    it.networkSettings.ports.getBindings().each { exposedPort, bindingArr ->
                        if (exposedPort.port == vendor.portBinding) {
                            int portBinding = bindingArr.first().hostPortSpec as int
                            def dockerHost = getDockerHost()
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

            removeContainer = createTaskInRootProject(project, "remove${uniqueName}Container", DockerRemoveContainer) {
                description "Remove a docker container for $uniqueName db vendor"
                group "docker"

                force = true
                removeVolumes = true
                targetContainerId createContainer.getContainerId()
            }

            Task databaseTestTask = project.tasks.create("${vendor.name}DatabaseTest", Test) {
                group "Verification"
                description "Runs slow integration test suite on $vendor.name database."
                systemProperty "bonita.version", project.version
                classpath += project.files(project.configurations.drivers)

                doFirst {
                    String dbUrl = project.hasProperty(SYS_PROP_DB_URL) ? project.property(SYS_PROP_DB_URL) : dbConnectionSettings.dbUrl
                    def connectionSettings = DbParser.extractDbConnectionSettings(dbUrl)
                    def dbValues = [
                            "sysprop.bonita.db.vendor"    : vendor.name,
                            "sysprop.bonita.bdm.db.vendor": vendor.name,
                            "db.url"          : dbUrl,
                            "db.user"         : project.hasProperty('db.user') ? project.property(SYS_PROP_DB_URL) : (System.getProperty(SYS_PROP_DB_USER) ? System.getProperty(SYS_PROP_DB_USER) : 'bonita'),
                            "db.password"     : project.hasProperty('db.password') ? project.property(SYS_PROP_DB_URL) : (System.getProperty(SYS_PROP_DB_PASSWORD) ? System.getProperty(SYS_PROP_DB_PASSWORD) : 'bpm'),
                            "bdm.db.url"      : bdmDbConnectionSettings.dbUrl,
                            "bdm.db.user"      : project.hasProperty('db.user') ? project.property(SYS_PROP_DB_URL) : (System.getProperty(SYS_PROP_DB_USER) ? System.getProperty(SYS_PROP_DB_USER) : 'business_data'),
                            "db.server.name"  : connectionSettings.serverName,
                            "db.server.port"  : connectionSettings.portNumber,
                            "db.database.name": connectionSettings.databaseName
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

            Task zipReport = project.tasks.create("zip${vendor.name}DatabaseTestReport" as String, Zip) {
                archiveFileName = databaseTestTask.reports.html.destination.name + ".zip"
                destinationDirectory = databaseTestTask.reports.html.destination.parentFile
                from databaseTestTask.reports.html.destination
            }
            project.afterEvaluate {
                databaseTestTask.includes = extension.includes
            }


            createContainer.dependsOn pullImage
            startContainer.dependsOn createContainer
            waitForContainerStartup.dependsOn startContainer
            inspectContainer.dependsOn waitForContainerStartup
            databaseTestTask.dependsOn inspectContainer

            startContainer.finalizedBy removeContainer
            databaseTestTask.finalizedBy zipReport
            removeContainer.mustRunAfter databaseTestTask
        }
    }

    static Task createTaskInRootProject(Project project, String taskName, Class<? extends Task> taskType, Closure configuration) {
        def task = project.rootProject.tasks.findByPath(taskName)
        if (!task) {
            task = project.rootProject.tasks.create(taskName, taskType, configuration)
        }
        return task
    }
}
