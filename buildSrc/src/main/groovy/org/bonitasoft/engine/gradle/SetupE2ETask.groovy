package org.bonitasoft.engine.gradle

import org.gradle.api.tasks.*
import org.gradle.internal.os.OperatingSystem

import static org.gradle.api.tasks.PathSensitivity.NONE

@CacheableTask
class SetupE2ETask extends Exec {

    @PathSensitive(NONE)
    @InputFile
    File scriptFile

    @OutputFile
    File outputFile = project.layout.buildDirectory.file("setup-e2e-output.log").get().asFile

    SetupE2ETask() {
        onlyIf { OperatingSystem.current().isLinux() }
    }

    @TaskAction
    @Override
    void exec() {
        commandLine 'sh', scriptFile.path
        // Ensure the parent directory of the output file exists
        outputFile.parentFile.mkdirs()
        // Redirect standard output to a file
        standardOutput = new FileOutputStream(outputFile)
        super.exec()
    }

}