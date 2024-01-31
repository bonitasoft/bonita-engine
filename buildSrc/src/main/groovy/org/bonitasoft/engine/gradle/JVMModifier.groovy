package org.bonitasoft.engine.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

/**
 * @author Emmanuel Duchastenier
 */
class JVMModifier {

    public static final String TEST_JVM_VERSION = "test.jvm.version"

    static void setJvmArgs(Project project, TaskProvider<Test> task) {
        ArrayList<String> jvmArgs = ["-Dorg.bonitasoft.h2.database.dir=./build/h2databasedir"]
        def property = project.property('org.gradle.jvmargs')
        if (property) {
            jvmArgs.addAll property.toString().split(" ")
        }
        def sysProperty = System.getProperty("org.gradle.jvmargs")
        if (sysProperty) {
            jvmArgs.addAll sysProperty.split(" ")
        }
        System.getProperties().each { p ->
            if (p.key.contains('sysprop.bonita') || p.key.startsWith('bonita.runtime')) {
                jvmArgs.add("-D${p.key}=${p.value}")
            }
        }
        project.logger.info("jvmArgs: $jvmArgs")
        task.configure { it.jvmArgs(jvmArgs) }
    }

    static void setTestJVM(Project project, TaskProvider<Test> task) {
        if (project.hasProperty(TEST_JVM_VERSION)) {
            def alternateJvm = project.property(TEST_JVM_VERSION)
            project.logger.info("Parameter '$TEST_JVM_VERSION' detected...")

            // to work around error "Toolchain from `executable` property does not match toolchain from `javaLauncher` property",
            // when upgrading to Gradle 8:
            JavaToolchainService service = project.getExtensions().getByType(JavaToolchainService.class);
            task.configure { javaLauncher.set(service.launcherFor { languageVersion = JavaLanguageVersion.of(alternateJvm as int) }) }
            project.logger.info("${project.name} will use alternate JVM '$alternateJvm' (${task.configure { javaLauncher.get().executablePath.asFile.absolutePath }}) to run $task")
        }
    }
}
