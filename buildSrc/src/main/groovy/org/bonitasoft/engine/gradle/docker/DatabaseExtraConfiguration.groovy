package org.bonitasoft.engine.gradle.docker

import groovy.transform.Canonical
import org.gradle.api.Project

@Canonical
class DatabaseExtraConfiguration {
    /**
     * Include an additional project in the test classpath
     */
    Project includeTestProject
    /**
     * Excludes test class patterns (e.g. '**&#47;*Test.class') applied to this database vendor.
     * It can be combined with {@link #excludeTags}.
     */
    List<String> excludes
    /**
     * Excludes tests marked by JUnit tags (e.g. 'my-tag') applied to this database vendor.
     * It can be combined with {@link #excludes}.
     */
    List<String> excludeTags
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

    def excludeTags(String... tags) {
        this.excludeTags = []
        this.excludeTags.addAll(tags)
    }

    def excludeTag(String tag) {
        if (this.excludeTags == null) {
            this.excludeTags = []
        }
        this.excludeTags.add(tag)
    }
}
