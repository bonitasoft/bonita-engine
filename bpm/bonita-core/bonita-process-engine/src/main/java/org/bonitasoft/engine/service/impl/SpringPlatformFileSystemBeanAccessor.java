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
package org.bonitasoft.engine.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SpringPlatformFileSystemBeanAccessor {

    private static AbsoluteFileSystemXmlApplicationContext context;

    private static String[] getResources() {
        final BonitaHomeServer homeServer = BonitaHomeServer.getInstance();
        try {
            final String platformFolder = homeServer.getPlatformConfFolder();
            final File serviceFolder = new File(platformFolder + File.separatorChar + "services");
            final File modelFolder = new File(platformFolder + File.separatorChar + "model");
            final String[] resources = ArrayUtils.addAll(getResourcesOfFolder(serviceFolder), getResourcesOfFolder(modelFolder));
            return resources;
        } catch (final BonitaHomeNotSetException e) {
            throw new RuntimeException("Bonita home not set");
        }
    }

    private static String[] getResourcesOfFolder(final File serviceFolder) {
        if (!serviceFolder.isDirectory()) {
            throw new RuntimeException("Folder '" + serviceFolder.getName() + "' not found");
        }
        final FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(final File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(".xml");
            }
        };
        /*
         * sort this to have always the same order
         */
        File[] listFiles = serviceFolder.listFiles(filter);
        List<File> listFilesCollection = Arrays.asList(listFiles);
        Collections.sort(listFilesCollection);
        listFiles = listFilesCollection.toArray(new File[listFilesCollection.size()]);
        if (listFiles.length == 0) {
            throw new RuntimeException("No file found");
        }
        final String[] resources = new String[listFiles.length];
        for (int i = 0; i < listFiles.length; i++) {
            try {
                resources[i] = listFiles[i].getCanonicalPath();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                throw new RuntimeException(e);
            }
        }
        return resources;
    }

    public static <T> T getService(final Class<T> serviceClass) {
        return getContext().getBean(serviceClass);
    }

    protected static FileSystemXmlApplicationContext getContext() {
        if (context == null) {
            initializeContext(null);
        }
        return context;
    }

    public static synchronized void initializeContext(final ClassLoader classLoader) {
        if (context == null) {// synchronized null check
            SpringSessionAccessorFileSystemBeanAcessor.initializeContext(classLoader);
            final FileSystemXmlApplicationContext sessionContext = SpringSessionAccessorFileSystemBeanAcessor.getContext();
            context = new AbsoluteFileSystemXmlApplicationContext(getResources(), true, sessionContext);
        }
    }

    protected static <T> T getService(final String name, final Class<T> serviceClass) {
        return getContext().getBean(name, serviceClass);
    }

    public static void destroy() {
        if (context != null) {
            context.close();
            context = null;
        }
    }

}
