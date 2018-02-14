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
package org.bonitasoft.engine.business.data.generator.client;

import static org.apache.commons.io.FilenameUtils.getName;
import static org.bonitasoft.engine.io.IOUtils.createDirectoryIfNotExists;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ResourcesLoader {

    public void copyJavaFilesToDirectory(String packageName, File directory) throws IOException {
        List<URL> javaFileURLs = getJavaFileURLs(packageName);
        for (URL url : javaFileURLs) {
            addJavaFileToDirectory(url, packageName, directory);
        }
    }

    private static List<URL> getJavaFileURLs(String packageName) throws IOException {
        String pattern = "/" + packageName.replace(".", "/") + "/**/*.java";
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(pattern);
        return getURLs(resources);
    }

    private static List<URL> getURLs(Resource[] resources) throws IOException {
        List<URL> classNames = new ArrayList<>();
        for (Resource resource : resources) {
            classNames.add(resource.getURL());
        }
        return classNames;
    }

    private static void addJavaFileToDirectory(URL javaFile, String originalPackage, File destDirectory) throws IOException {
        File packageDirectory = createPackageDirectory(javaFile, originalPackage, destDirectory);
        File destinationFile = new File(packageDirectory, getName(javaFile.toString()));
        FileUtils.copyURLToFile(javaFile, destinationFile);
    }

    private static File createPackageDirectory(URL javaFile, String originalPackage, File destDirectory) {
        File packageDirectory = new File(destDirectory, packageOf(javaFile.toString(), originalPackage));
        return createDirectoryIfNotExists(packageDirectory);
    }

    //@VisibleForTesting
    static String packageOf(final String javaFile, String originalPackage) {
        String firstChar = StringUtils.substringBefore(originalPackage, ".");
        String className = javaFile.substring(javaFile.indexOf(firstChar));
        if (className.indexOf("!") != -1) {//support osgi classpath url
            className = className.substring(className.indexOf("!") + 2, className.length());
        }
        return className.substring(0, className.lastIndexOf("/"));
    }
}
