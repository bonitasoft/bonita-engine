package org.bonitasoft.engine.gradle.docker

import groovy.transform.Canonical
import org.gradle.api.Project

@Canonical
class DatabaseExtraConfiguration {
   /**
     * Include an additional module in the test classpath
     */
    Project includeTestModule
    /**
     * Excludes test class patterns
     */
    List<String> excludes
    /**
     * Enable or disable the execution of the test task for this database configuration
     */
    boolean enabled = false

    def excludes(String... excludes) {
        this.excludes = []
        this.excludes.addAll(excludes)
    }

    def exclude(String excludes) {
        if (this.excludes == null) {
            this.excludes = []
        }
        this.excludes.add(excludes)
    }

}
