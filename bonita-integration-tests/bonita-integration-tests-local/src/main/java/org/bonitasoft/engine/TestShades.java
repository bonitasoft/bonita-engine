/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.test.BPMLocalIT;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestShades {

    private static final Logger LOG = LoggerFactory.getLogger(TestShades.class);

    private static final String ORG_BONITASOFT_ENGINE = "org.bonitasoft.engine";
    protected static final String ORG_BONITASOFT_PLATFORM = "org.bonitasoft.platform";

    @Test
    public void testShades() throws Exception {
        String mvn = getMavenExecutable();
        LOG.info("Used mvn path: {}", mvn);

        String version = BPMLocalIT.getBonitaVersion();
        LOG.info("Detected Bonita version: {}", version);

        File workingDirectory = new File("target", "shadeTester");
        workingDirectory.delete();
        FileUtils.forceMkdir(workingDirectory);
        File pomFile = new File(workingDirectory, "pom.xml");
        LOG.info("Building {}", pomFile.getAbsolutePath());
        IOUtil.writeContentToFile(generatePomContent(version), pomFile);

        String localRepository = System.getProperty("localRepository");
        String jvmProperties = "";
        if (localRepository != null) {
            LOG.info("Using overridden localRepository: {}", localRepository);
            jvmProperties += " -Dmaven.repo.local=" + localRepository;
        }

        LOG.info("Download the dependency plugin");
        executeCommand(mvn + " dependency:help" + jvmProperties, workingDirectory, 1);

        LOG.info("Run the dependency tree - offline"); // they are supposed to have been built before running the test
        final String mvnCommand = mvn + " --offline dependency:tree --legacy-local-repository" + jvmProperties;
        String mavenOutput = executeCommand(mvnCommand, workingDirectory, 1);
        LOG.debug("Maven output: {}", mavenOutput);

        assertThat(mavenOutput).as("Maven output").contains("BUILD SUCCESS");
        mavenOutput = mavenOutput.replaceAll("\n?.*Downloading.*\n", "");
        mavenOutput = mavenOutput.replaceAll("\n?.*Downloaded.*\n", "");
        mavenOutput = removedIgnoredBonitaDeps(mavenOutput);
        if (mavenOutput.contains("bonitasoft")) {
            String str = "bonitasoft";
            int indexOf = mavenOutput.indexOf(str);
            String part1 = mavenOutput.substring(indexOf - 50, indexOf - 1);
            String part2 = mavenOutput.substring(indexOf - 1, indexOf + str.length());
            String part3 = mavenOutput.substring(indexOf + str.length(), indexOf + str.length() + 50);
            fail("the dependency tree contains other modules than server/client/common: \"" + part1 + " =====>>>>>" + part2 + " <<<<<=====" + part3);
        }
    }

    private static String executeCommand(String command, File workingDirectory, long timeoutInMinutes)
            throws InterruptedException, IOException {
        LOG.info("Running command {} in {}", command, workingDirectory.getAbsolutePath());

        Process process = Runtime.getRuntime().exec(command, null, workingDirectory);
        InputStream processInputStream = process.getInputStream();
        StreamGobbler outputGobbler = new StreamGobbler(processInputStream, "OUTPUT");
        outputGobbler.start();
        InputStream processErrorStream = process.getErrorStream();
        StreamGobbler errorGobbler = new StreamGobbler(processErrorStream, "ERROR");
        errorGobbler.start();

        LOG.info("Waiting at most {} minutes", timeoutInMinutes);
        boolean exitNormally = process.waitFor(timeoutInMinutes, TimeUnit.MINUTES);
        LOG.info("Exit normally?: {}", exitNormally);

        IOUtils.closeQuietly(processInputStream);
        IOUtils.closeQuietly(processErrorStream);

        outputGobbler.join();
        errorGobbler.join();
        return outputGobbler.getOutputAsString();
    }

    private String getMavenExecutable() throws IOException {
        String pathToRoot = getPathToRoot();
        if (SystemUtils.IS_OS_WINDOWS) {
            return "cmd /c " + pathToRoot + File.separator + "mvnw.cmd";
        } else {
            return pathToRoot + File.separator + "mvnw";
        }
    }

    protected String getPathToRoot() {
        File currentPom = new File("pom.xml");
        return currentPom.getAbsoluteFile().getParentFile().getParentFile().getParentFile().getAbsolutePath();
    }

    protected String removedIgnoredBonitaDeps(final String mavenOutput) {
        String filteredOutput = mavenOutput.replaceAll(ORG_BONITASOFT_ENGINE + ":bonita-server", "");
        filteredOutput = filteredOutput.replaceAll(ORG_BONITASOFT_ENGINE + ":bonita-client", "");
        filteredOutput = filteredOutput.replaceAll(ORG_BONITASOFT_ENGINE + ":bonita-common", "");
        filteredOutput = filteredOutput.replaceAll(ORG_BONITASOFT_PLATFORM + ":platform-setup", "");
        filteredOutput = filteredOutput.replaceAll(ORG_BONITASOFT_PLATFORM + ":platform-resources", "");
        return filteredOutput;
    }

    private String generatePomContent(final String version) {
        String pom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        pom += "    xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n";
        pom += "<modelVersion>4.0.0</modelVersion>\n";
        pom += "<groupId>test</groupId>\n";
        pom += "<artifactId>shadeTester</artifactId>\n";
        pom += "<version>0.0.1-SNAPSHOT</version>\n";
        pom += "<build>\n<pluginManagement>\n<plugins>\n<plugin>\n";
        pom += "<groupId>org.apache.maven.plugins</groupId>\n";
        pom += "<artifactId>maven-dependency-plugin</artifactId>\n";
        pom += "<version>2.8</version>\n";
        pom += "</plugin>\n</plugins>\n</pluginManagement>\n</build>";
        pom += "<dependencies>\n";
        pom += generateDependencies(version);
        pom += "</dependencies>\n";
        pom += "</project>\n";
        return pom;
    }

    protected String generateDependencies(final String version) {
        String dependencies = generateDependency("bonita-server", ORG_BONITASOFT_ENGINE, version);
        dependencies += generateDependency("bonita-client", ORG_BONITASOFT_ENGINE, version);
        return dependencies;
    }

    protected String generateDependency(final String artifactId, final String groupId, final String version) {
        String dep = "<dependency>\n";
        dep += "<artifactId>" + artifactId + "</artifactId>\n";
        dep += "<groupId>" + groupId + "</groupId>\n";
        dep += "<version>" + version + "</version>\n";
        dep += "</dependency>\n";

        return dep;
    }

    private static class StreamGobbler extends Thread {

        private static final Logger LOG = LoggerFactory.getLogger(StreamGobbler.class);

        private InputStream is;
        private String type;

        private String ouput;

        public StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try (StringWriter outputWriter = new StringWriter()) {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    LOG.info("{} > {}", type, line);
                    outputWriter.append(line);
                }
                outputWriter.flush();
                ouput = outputWriter.toString();
            } catch (Exception e) {
                LOG.error(type + " >", e);
            }
        }

        public String getOutputAsString() {
            return ouput;
        }

    }

}
