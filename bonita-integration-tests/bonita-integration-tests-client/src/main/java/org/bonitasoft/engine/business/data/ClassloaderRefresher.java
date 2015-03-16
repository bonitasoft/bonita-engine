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
package org.bonitasoft.engine.business.data;

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

import org.bonitasoft.engine.io.IOUtils;

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
     * @throws java.io.IOException
     * @throws java.net.MalformedURLException
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
