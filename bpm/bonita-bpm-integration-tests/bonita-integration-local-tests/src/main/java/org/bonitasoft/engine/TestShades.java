package org.bonitasoft.engine;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bonitasoft.engine.io.IOUtil;
import org.junit.Test;

public class TestShades {

    @Test
    public void testShades() throws IOException {
        String version = System.getProperty("bonita.version");// works in maven
        if (version == null) {
            // when running tests in eclipse get it from the pom.xml
            File file = new File("pom.xml");
            String pomContent = IOUtil.read(file);
            Pattern pattern = Pattern.compile("<version>(.*)</version>");
            Matcher matcher = pattern.matcher(pomContent);
            matcher.find();
            version = matcher.group(1);
        }
        String thePom = getPom(version);
        File file = new File("shadeTester");
        file.mkdir();
        String outputOfMaven;
        try {
            File file2 = new File(file, "pom.xml");
            IOUtil.write(file2, thePom);
            System.out.println("building " + file2.getAbsolutePath());
            Process exec = Runtime.getRuntime().exec("mvn dependency:tree", new String[] {}, file);
            InputStream inputStream = exec.getInputStream();
            exec.getOutputStream().close();
            exec.getErrorStream().close();
            outputOfMaven = IOUtil.read(inputStream);
            System.out.println(outputOfMaven);
        } finally {
            IOUtil.deleteDir(file);
        }
        assertTrue("build was not successfull", outputOfMaven.contains("BUILD SUCCESS"));
        outputOfMaven = outputOfMaven.replaceAll("bonitasoft.engine:bonita-server", "");
        outputOfMaven = outputOfMaven.replaceAll("bonitasoft.engine:bonita-client", "");
        outputOfMaven = outputOfMaven.replaceAll("bonitasoft.engine:bonita-common", "");
        if (outputOfMaven.contains("bonitasoft")) {
            String str = "bonitasoft";
            int indexOf = outputOfMaven.indexOf(str);
            String part1 = outputOfMaven.substring(indexOf - 50, indexOf - 1);
            String part2 = outputOfMaven.substring(indexOf - 1, indexOf + str.length());
            String part3 = outputOfMaven.substring(indexOf + str.length(), indexOf + str.length() + 50);
            fail("the dependency tree contains other modules than server/client/common: \"" + part1 + " =====>>>>>" + part2 + " <<<<<=====" + part3);
        }

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
        String pom2 = generateDependency("bonita-server", "org.bonitasoft.engine", version);
        pom2 += generateDependency("bonita-client", "org.bonitasoft.engine", version);
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
