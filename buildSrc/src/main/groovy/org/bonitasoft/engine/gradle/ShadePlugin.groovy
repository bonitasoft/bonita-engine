package org.bonitasoft.engine.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

class ShadePlugin implements Plugin<Project> {

    private static final String PLATFORM_CONFIGURATION_NAME = "platform-runtime"

    @Override
    void apply(Project project) {
        project.plugins.apply("com.github.johnrengelman.shadow")
        project.plugins.apply("maven-publish")

        def extension = project.extensions.create("shade", ShadeExtension)

        project.jar {
            archiveClassifier = 'original'
        }

        project.afterEvaluate {
            project.shadowJar {
                archiveClassifier = "" // we replace the original jar by the shadow jar
                dependencies {
                    include({
                        if (!project.ext.has("shadedDependencies")) {
                            // "shadedDependencies" property is used for display only
                            project.ext.shadedDependencies = [] as Set
                        }
                        def allProjectsAlreadyShaded = getProjectsAlreadyShaded(project, extension)
                        if (shouldBeIncludedInShade(project, it, extension, allProjectsAlreadyShaded)) {
                            project.ext.shadedDependencies.add(it)
                            return true
                        }
                        return false
                    })
                }
                doFirst {
                    project.logger.info("Shading for project {} : ", project.path)
                    project.ext.projectsToShade.each {
                        project.logger.info(" - {}", it)
                    }
                    project.logger.info("Effective shading of project {} : ", project.path)
                    project.ext.shadedDependencies.each {
                        project.logger.info(" - {}", it)
                    }

                }
            }
            // not mandatory. Only here to have the production jars built when running `gradle build`:
            project.tasks.named("build") {
                dependsOn project.tasks.named("shadowJar")
            }

            project.javadoc {
                source {
                    getShadedProjects(project, extension, getProjectsAlreadyShaded(project, extension)).collect {
                        it.sourceSets.main.allJava
                    }
                }
                classpath = project.files({
                    getShadedProjects(project, extension, getProjectsAlreadyShaded(project, extension)).collect {
                        it.sourceSets.main.compileClasspath
                    }
                })
                options.addStringOption('Xdoclint:none', '-quiet')
                options.addBooleanOption("author", true)
                // FIXME update studio test org.bonitasoft.studio.tests.engine.TestJavaDoc.testHasJavaDoc
            }
            project.tasks.register("sourcesJar", Jar) {
                from {
                    getShadedProjects(project, extension, getProjectsAlreadyShaded(project, extension)).collect {
                        it.sourceSets.main.allJava
                    }
                }
                archiveClassifier = 'sources'
            }
            project.tasks.register("javadocJar", Jar) {
                from project.javadoc
                archiveClassifier = 'javadoc'
            }

            project.publishing.publications {
                shadow(MavenPublication) { publication ->
                    project.shadow.component(publication)
                    PomUtils.pomCommunityPublication(publication.getPom())
                    pom.withXml {
                        def allProjectsAlreadyShaded = getProjectsAlreadyShaded(project, extension)
                        Set<ResolvedDependency> inPom = getPomDependencies(project, extension, allProjectsAlreadyShaded, true)
                        project.logger.info("Include in pom:")
                        inPom.each {
                            project.logger.info(" - {}", it)
                        }
                        def rootNode = asNode()
                        Node dependencies = rootNode.children().find { Node child -> child.name() == "dependencies" }
                        inPom.each { gradleDep ->
                            Node dependency =
                                    dependencies
                                            .appendNode("dependency")
                            dependency.appendNode("groupId", gradleDep.moduleGroup)
                            dependency.appendNode("artifactId", gradleDep.moduleName)
                            dependency.appendNode("version", gradleDep.moduleVersion)
                            if (extension.libExclusions.containsKey(gradleDep.moduleName)) {
                                Node es = dependency.appendNode('exclusions')
                                List<ShadeDependency> excludes = extension.libExclusions.get(gradleDep.moduleName)
                                excludes.each {
                                    Node e = es.appendNode('exclusion')
                                    e.appendNode('groupId', it.group)
                                    e.appendNode('artifactId', it.name)
                                }
                            }
                        }
                    }
                    artifact project.sourcesJar
                    artifact project.javadocJar
                }
            }
        }
    }

    private boolean shouldBeIncludedInShade(Project project, ResolvedDependency currentDependency, ShadeExtension extension, Set<Project> allProjectsAlreadyShaded) {
        Set<Project> projectsToShade = getShadedProjects(project, extension, allProjectsAlreadyShaded)
        if (extension.includes.contains(new ShadeDependency(group: currentDependency.moduleGroup, name: currentDependency.moduleName))) {
            return true
        }
        def projectDep = getAssociatedProjectFromDependency(project, currentDependency)
        if (projectDep == null) {
            return false // dependency is not a project
        }
        return projectsToShade.contains(projectDep)
    }

    /**
     *  get the list of projects to shade
     */
    private Set<Project> getShadedProjects(Project project, ShadeExtension extension, Set<Project> allProjectsAlreadyShaded) {
        if (!project.ext.has("projectsToShade")) {
            project.ext.projectsToShade = getAllProjectsToShade(project, extension, allProjectsAlreadyShaded)
        }
        project.ext.projectsToShade
    }

    private void printTree(Project project, String indentation) {
        project.configurations.compile.resolvedConfiguration.firstLevelModuleDependencies.each {
            def dependencyAsProject = getAssociatedProjectFromDependency(project, it)
            if (dependencyAsProject) {
                println indentation + dependencyAsProject.name
                printTree(dependencyAsProject, indentation + "--")
            }
        }
    }

    /**
     *  get the list of project that are already shaded by other shade
     *  e.g. bonita-common-util is already shaded by bonita-common
     */
    private Set<Project> getProjectsAlreadyShaded(Project rootProject, ShadeExtension extension) {
        if (!rootProject.ext.has("projectsAlreadyShaded")) { // add property "projectsAlreadyShaded" to act like a cache
            rootProject.ext.projectsAlreadyShaded = getAllProjectsAlreadyShaded(rootProject, extension)
        }
        rootProject.ext.projectsAlreadyShaded
    }

    /**
     * get the Project (object) from the ResolvedDependency (object)
     */
    private Project getAssociatedProjectFromDependency(Project project, ResolvedDependency dependency) {
        def artifacts = dependency.getModuleArtifacts()
        if (artifacts.isEmpty()) {
            //it happens when a dependency is a bom pulled from gradle's artifacts metadata (variant)
            return null
        }
        def identifier = artifacts.first().id.componentIdentifier
        if (!(identifier instanceof ProjectComponentIdentifier)) {
            return null
        }
        return project.project(identifier.projectPath)
    }

    private boolean isAShadeProject(Project it) {
        it.plugins.find { it instanceof ShadePlugin }
    }

    private Set<Project> getAllProjectsToShade(Project project, ShadeExtension extension, Set<Project> allProjectsAlreadyShaded) {
        Set allProjects = []
        project.configurations.runtimeClasspath.resolvedConfiguration.firstLevelModuleDependencies.forEach {
            def projectDependency = getAssociatedProjectFromDependency(project, it)
            if (projectDependency) { // is an engine project (service)
                if (!isAShadeProject(projectDependency) && !extension.excludes.contains(projectDependency) && !allProjectsAlreadyShaded.contains(projectDependency)) {
                    allProjects.add(projectDependency)
                    allProjects.addAll(getAllProjectsToShade(projectDependency, extension, allProjectsAlreadyShaded))
                }
            }
        }
        allProjects
    }

    private Set<Project> getAllProjectsAlreadyShaded(Project project, ShadeExtension extension) {
        Set allProjects = []
        // Take all declared compilation dependencies:
        project.configurations.runtimeClasspath.resolvedConfiguration.firstLevelModuleDependencies.forEach {
            def projectDependency = getAssociatedProjectFromDependency(project, it)
            if (projectDependency) {
                if (isAShadeProject(projectDependency)) { // this dependency is a shade project
                    //all dependencies of this shade project are already shaded, let's add them all to the list:
                    allProjects.addAll(getAllProjectsToShade(projectDependency, extension, [] as Set))
                } else {
                    allProjects.addAll(getAllProjectsAlreadyShaded(projectDependency, extension))
                }
            }
        }
        allProjects
    }

    private Set<ResolvedDependency> getPomDependencies(Project project, ShadeExtension extension, Set<Project> allProjectsAlreadyShaded, boolean isRootProject) {
        Set<ResolvedDependency> allDependencies = []
        def allScopes = project.configurations.runtimeClasspath.resolvedConfiguration.firstLevelModuleDependencies
        allScopes.forEach {
            Project projectDependency = getAssociatedProjectFromDependency(project, it)
            if (projectDependency) {
                if (allProjectsAlreadyShaded.contains(projectDependency)) {
                    return // no need to go further
                }
                if (extension.excludes.contains(projectDependency)) {
                    //excluded from shade, add this project but NOT its dependencies:
                    allDependencies.add(it)
                } else if (isAShadeProject(projectDependency)) {
                    // the project is a shaded project (e.g. bonita-common-sp in bonita-server-sp)
                    // only add it if it is not a shade pulled by transitivity:
                    if (isRootProject) {
                        allDependencies.add(it)
                    } else {
                        project.logger.info(" Shade POM generation: ignoring {}, as it is pulled as transitive shade project", projectDependency.name)
                    }
                } else {
                    // do not add it: project is shaded inside this project
                    allDependencies.addAll(getPomDependencies(projectDependency, extension, allProjectsAlreadyShaded, false))
                }
            } else {
                // also add transitive dependencies of third-party libs:
                allDependencies.addAll(getTransitiveThirdPartyDependencies(it, "", project, extension))
            }
        }
        // remove all dependencies that are in the configuration "platform-runtime". it's used by gradle to resolve versions (like a bom)
        // those dependencies should be imported in the dependency management instead. It is manually done right now (see `bonita-engine` bom)
        allDependencies.findAll { it.configuration != PLATFORM_CONFIGURATION_NAME }
    }

    /**
     * Returns a Set of the passed ResolvedDependency itself + all its children, recursively
     * @param indent indentation string, for display purposes
     */
    private Set<ResolvedDependency> getTransitiveThirdPartyDependencies(ResolvedDependency current, String indent, Project project, ShadeExtension extension) {
        def res = [] as Set
        project.logger.debug(" Shade POM generation: adding ${indent}${current.name}")
        // if the external dependency is shaded, do not add it in the pom, but add its dependencies
        if (!extension.includes.contains(new ShadeDependency(group: current.moduleGroup, name: current.moduleName))) {
            res.add(current)
        }
        def thirdPartyExclusion = extension.libExclusions.get(current.moduleName)
        current.getChildren().forEach { child ->
            if (!thirdPartyExclusion || !thirdPartyExclusion.contains(new ShadeDependency(group: child.moduleGroup, name: child.moduleName))) {
                res.addAll(getTransitiveThirdPartyDependencies(child, indent + "  ", project, extension))
            }
        }
        return res
    }

}
