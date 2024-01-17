package org.bonitasoft.engine.gradle

import org.gradle.api.Project

class ShadeExtension {

    List<ShadeDependency> includes = []
    List<Project> excludes = []
    Map<String, List<ShadeDependency>> libExclusions = [:] as Map

    def include(Map<String, String> artifact) {
        includes.add(new ShadeDependency(artifact))
    }

    def exclude(Project project) {
        excludes.add(project)
    }

    def excludeLibs(String refereeLib, ShadeDependency... libsToExclude) {
        libExclusions.put(refereeLib, Arrays.asList(libsToExclude))
    }
}
