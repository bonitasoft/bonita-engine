package org.bonitasoft.engine.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

/**
 * @author Emmanuel Duchastenier
 */
class JVMModifier {

    public static final String TEST_JVM = "test.jvm"

    private static void setJvmArgs(Project project, Test task) {
        task.jvmArgs("-Dorg.bonitasoft.h2.database.dir=./build/h2databasedir")
        def property = project.property('org.gradle.jvmargs')
        if (property) {
            task.jvmArgs property.toString().split(" ")
        }
        def sysProperty = System.getProperty("org.gradle.jvmargs")
        if (sysProperty) {
            task.jvmArgs sysProperty.split(" ")
        }
    }

    private static void setTestJVM(Project project, Test task) {
        if (project.hasProperty(TEST_JVM)) {
            def alternateJvm = project.property(TEST_JVM)
            project.logger.info("Parameter '$TEST_JVM' detected. ${project.name} will use alternate JVM '$alternateJvm' to run $task")
            task.executable = alternateJvm
        }
    }
}
