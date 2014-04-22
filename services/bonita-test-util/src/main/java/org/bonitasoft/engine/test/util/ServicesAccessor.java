/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.test.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;

import org.bonitasoft.engine.scheduler.ServicesResolver;

/**
 * @author Matthieu Chaffotte
 */
public final class ServicesAccessor implements ServicesResolver {

    private static class ServiceAccessorHolder {

        private static final ServicesAccessor INSTANCE = new ServicesAccessor();
    }

    public static ServicesAccessor getInstance() {
        return ServiceAccessorHolder.INSTANCE;
    }

    private AbsoluteFileSystemXmlApplicationContext context;

    private ServicesAccessor() {
        super();
        try {
            context = new AbsoluteFileSystemXmlApplicationContext(getResourceList(), true, null);
        } catch (final Exception t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    protected String[] getResourceList() {
        final String serviceFolderPath = System.getProperty("bonita.services.folder");
        if (serviceFolderPath == null) {
            throw new RuntimeException("The system property 'bonita.services.folder' is not set");
        }

        final File serviceFolder = new File(serviceFolderPath);
        if (!serviceFolder.isDirectory()) {
            throw new RuntimeException("Folder '" + serviceFolderPath + "'is not a directory.");
        }

        try {
            final FileFilter fileFilter = new ServicesFileFilter();
            final File[] listFiles = serviceFolder.listFiles(fileFilter);
            if (listFiles.length == 0) {
                throw new RuntimeException("No file found");
            }
            final String[] resources = new String[listFiles.length];
            for (int i = 0; i < listFiles.length; i++) {
                resources[i] = listFiles[i].getCanonicalPath();
            }
            return resources;
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public <T> T getInstanceOf(final Class<T> clazz) {
        return context.getBean(clazz);
    }

    public <T> Map<String, T> getInstancesOf(final Class<T> clazz) {
        return context.getBeansOfType(clazz);
    }

    public <T> T getInstanceOf(final String name, final Class<T> class1) {
        return context.getBean(name, class1);
    }

    public void destroy() {
        context.destroy();
        context = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T lookup(final String serviceName) {
        return (T) context.getBean(serviceName);
    }

}
