/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.bonitasoft.engine.io.IOUtils;

/**
 * @author Emmanuel Duchastenier
 */
public class ClassloaderRefresher {

    /**
     * @param clientZipContent
     * @param contextClassLoader
     * @param modelClass
     * @param fsFolderToPutJars
     * @return the newly created classloader with newly loaded class, if found.
     * @throws IOException
     * @throws MalformedURLException
     */
    public ClassLoader loadClientModelInClassloader(final byte[] clientZipContent, final ClassLoader contextClassLoader, final String modelClass,
            final File fsFolderToPutJars) throws IOException, MalformedURLException {
        final Map<String, byte[]> ressources = IOUtils.unzip(clientZipContent);
        final List<URL> urls = new ArrayList<URL>();
        for (final Entry<String, byte[]> e : ressources.entrySet()) {
            final File file = new File(fsFolderToPutJars, e.getKey());
            if (file.getName().endsWith(".jar")) {
                if (file.getName().contains("model")) {
                    try {
                        contextClassLoader.loadClass(modelClass);
                    } catch (final ClassNotFoundException e1) {
                        FileUtils.writeByteArrayToFile(file, e.getValue());
                        urls.add(file.toURI().toURL());
                    }
                }
                if (file.getName().contains("dao")) {
                    try {
                        contextClassLoader.loadClass(modelClass + "DAO");
                    } catch (final ClassNotFoundException e1) {
                        FileUtils.writeByteArrayToFile(file, e.getValue());
                        urls.add(file.toURI().toURL());
                    }
                }
                if (file.getName().contains("javassist")) {
                    try {
                        contextClassLoader.loadClass("javassist.util.proxy.MethodFilter");
                    } catch (final ClassNotFoundException e1) {
                        FileUtils.writeByteArrayToFile(file, e.getValue());
                        urls.add(file.toURI().toURL());
                    }
                }
            }
        }
        ClassLoader classLoaderWithBDM = contextClassLoader;
        if (!urls.isEmpty()) {
            classLoaderWithBDM = new URLClassLoader(urls.toArray(new URL[urls.size()]), contextClassLoader);
        }
        return classLoaderWithBDM;
    }

}
