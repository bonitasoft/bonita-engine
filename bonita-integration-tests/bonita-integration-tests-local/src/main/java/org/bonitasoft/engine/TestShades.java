package org.bonitasoft.engine;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.test.BPMLocalTest;
import org.junit.Test;

public class TestShades {

    private static final String ORG_BONITASOFT_ENGINE = "org.bonitasoft.engine";

    @Test
    public void testShades() throws IOException {
        String mvn = System.getProperty("path.to.mvn", "mvn");// to be overwritten in CI
        String version = BPMLocalTest.getBonitaVersion();

        // print properties for debugging purpose
        System.out.println("mvn path used: " + mvn);
        System.out.println("bonita version detected: " + version);

        Properties properties = System.getProperties();
        for (Entry<Object, Object> prop : properties.entrySet()) {
            System.out.println(prop.getKey() + ": " + prop.getValue());
        }

        String thePom = getPom(version);
        File file = new File("shadeTester");
        file.mkdir();
        String outputOfMaven;
        try {
            File file2 = new File(file, "pom.xml");
            IOUtil.writeContentToFile(thePom, file2);
            System.out.println("building " + file2.getAbsolutePath());
            System.out.println("Run mvn in " + file.getAbsolutePath());
            Process exec = Runtime.getRuntime().exec(mvn + " dependency:tree", null, file);
            InputStream inputStream = exec.getInputStream();
            exec.getOutputStream().close();
            exec.getErrorStream().close();
            outputOfMaven = IOUtil.read(inputStream);
            System.out.println(outputOfMaven);
            inputStream.close();
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

    protected String removedIgnoredBonitaDeps(String outputOfMaven) {
        outputOfMaven = outputOfMaven.replaceAll(ORG_BONITASOFT_ENGINE + ":bonita-server", "");
        outputOfMaven = outputOfMaven.replaceAll(ORG_BONITASOFT_ENGINE + ":bonita-client", "");
        outputOfMaven = outputOfMaven.replaceAll(ORG_BONITASOFT_ENGINE + ":bonita-common", "");
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
