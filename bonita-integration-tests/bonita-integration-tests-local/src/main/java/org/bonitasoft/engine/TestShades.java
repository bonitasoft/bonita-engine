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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.SystemUtils;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.test.BPMLocalIT;
import org.junit.Test;

public class TestShades {

    private static final String ORG_BONITASOFT_ENGINE = "org.bonitasoft.engine";
    protected static final String ORG_BONITASOFT_PLATFORM = "org.bonitasoft.platform";

    @Test
    public void testShades() throws IOException {
        String version = BPMLocalIT.getBonitaVersion();


        String thePom = getPom(version);
        File file = new File("shadeTester");
        file.mkdir();
        String outputOfMaven;
        try {
            File file2 = new File(file, "pom.xml");
            IOUtil.writeContentToFile(thePom, file2);
            String mvn = getMavenExecutable();
            System.out.println("mvn path used: " + mvn);
            System.out.println("bonita version detected: " + version);
            System.out.println("building " + file2.getAbsolutePath());
            System.out.println("Run mvn in " + file.getAbsolutePath());
            Process exec = Runtime.getRuntime().exec(mvn + " dependency:tree", null, file);
            outputOfMaven = getOutputOfProcess(exec);
            System.out.println(outputOfMaven);
        } finally {
            IOUtil.deleteDir(file);
        }
        assertTrue("build was not successfull", outputOfMaven.contains("BUILD SUCCESS"));
        outputOfMaven = outputOfMaven.replaceAll("\n?.*Downloading.*\n", "");
        outputOfMaven = outputOfMaven.replaceAll("\n?.*Downloaded.*\n", "");
        outputOfMaven = removedIgnoredBonitaDeps(outputOfMaven);
        if (outputOfMaven.contains("bonitasoft")) {
            String str = "bonitasoft";
            int indexOf = outputOfMaven.indexOf(str);
            String part1 = outputOfMaven.substring(indexOf - 50, indexOf - 1);
            String part2 = outputOfMaven.substring(indexOf - 1, indexOf + str.length());
            String part3 = outputOfMaven.substring(indexOf + str.length(), indexOf + str.length() + 50);
            fail("the dependency tree contains other modules than server/client/common: \"" + part1 + " =====>>>>>" + part2 + " <<<<<=====" + part3);
        }

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
        File file = new File("pom.xml");
        return file.getAbsoluteFile().getParentFile().getParentFile().getParentFile().getAbsolutePath();
    }

    private String getOutputOfProcess(Process exec) throws IOException {
        InputStream inputStream = exec.getInputStream();
        exec.getOutputStream().close();
        InputStream errorStream = exec.getErrorStream();
        System.err.println(IOUtil.read(errorStream));
        errorStream.close();

        String outputOfMaven;
        outputOfMaven = IOUtil.read(inputStream);
        inputStream.close();
        return outputOfMaven;
    }

    protected String removedIgnoredBonitaDeps(String outputOfMaven) {
        outputOfMaven = outputOfMaven.replaceAll(ORG_BONITASOFT_ENGINE + ":bonita-server", "");
        outputOfMaven = outputOfMaven.replaceAll(ORG_BONITASOFT_ENGINE + ":bonita-client", "");
        outputOfMaven = outputOfMaven.replaceAll(ORG_BONITASOFT_ENGINE + ":bonita-common", "");
        outputOfMaven = outputOfMaven.replaceAll(ORG_BONITASOFT_PLATFORM + ":platform-setup", "");
        outputOfMaven = outputOfMaven.replaceAll(ORG_BONITASOFT_PLATFORM + ":platform-resources", "");
        return outputOfMaven;
    }

    private String getPom(final String version) {
        String pom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        pom += "    xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n";
        pom += "<modelVersion>4.0.0</modelVersion>\n";
        pom += "<groupId>test</groupId>\n";
        pom += "<artifactId>shadeTester</artifactId>\n";
        pom += "<version>0.0.1-SNAPSHOT</version>\n";
        pom += "<dependencies>\n";
        pom += generateDependencies(version);
        pom += "    </dependencies>\n";
        pom += "</project>    \n";
        return pom;
    }

    protected String generateDependencies(final String version) {
        String pom2 = generateDependency("bonita-server", ORG_BONITASOFT_ENGINE, version);
        pom2 += generateDependency("bonita-client", ORG_BONITASOFT_ENGINE, version);
        return pom2;
    }

    protected String generateDependency(final String artifactId, final String groupId, final String version) {
        String dep = "<dependency>\n";
        dep += "<artifactId>" + artifactId + "</artifactId>\n";
        dep += "<groupId>" + groupId + "</groupId>\n";
        dep += "<version>" + version + "</version>\n";
        dep += "</dependency>\n";

        return dep;
    }

}
