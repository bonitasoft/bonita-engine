package com.bonitasoft.engine.bdm.client;

import static com.bonitasoft.engine.io.IOUtils.createDirectoryIfNotExists;
import static org.apache.commons.io.FilenameUtils.getName;

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

    private List<URL> getJavaFileURLs(String packageName) throws IOException {
        String pattern = "/" + packageName.replace(".", "/") + "/**/*.java";
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(pattern);
        return getURLs(resources);
    }

    private List<URL> getURLs(Resource[] resources) throws IOException {
        List<URL> classNames = new ArrayList<URL>();
        for (Resource resource : resources) {
            classNames.add(resource.getURL());
        }
        return classNames;
    }

    private void addJavaFileToDirectory(URL javaFile, String originalPackage, File destDirectory) throws IOException {
        File packageDirectory = createPackageDirectory(javaFile, originalPackage, destDirectory);
        File destinationFile = new File(packageDirectory, getName(javaFile.toString()));
        FileUtils.copyURLToFile(javaFile, destinationFile);
    }

    private File createPackageDirectory(URL javaFile, String originalPackage, File destDirectory) {
        File packageDirectory = new File(destDirectory, packageOf(javaFile.toString(), originalPackage));
        return createDirectoryIfNotExists(packageDirectory);
    }

    private String packageOf(final String javaFile, String originalPackage) {
        String firstChar = StringUtils.substringBefore(originalPackage, ".");
        String className = javaFile.substring(javaFile.indexOf(firstChar));
        return className.substring(0, className.lastIndexOf("/"));
    }
}
