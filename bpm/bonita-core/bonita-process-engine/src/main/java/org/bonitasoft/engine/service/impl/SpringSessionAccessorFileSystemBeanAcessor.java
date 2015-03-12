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
import java.io.IOException;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author Matthieu Chaffotte
 */
public class SpringSessionAccessorFileSystemBeanAcessor {

    private static AbsoluteFileSystemXmlApplicationContext context;

    private static String[] getResources() {
        final BonitaHomeServer homeServer = BonitaHomeServer.getInstance();
        try {
            final String platformFolder = homeServer.getPlatformConfFolder();
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(platformFolder).append(File.separatorChar).append("sessionaccessor").append(File.separatorChar);
            final String file = "cfg-bonita-sessionaccessor-threadlocal.xml";
            stringBuilder.append(file);
            final File sessionAccesorFile = new File(stringBuilder.toString());
            if (!sessionAccesorFile.exists() || !sessionAccesorFile.isFile()) {
                throw new SessionAccessorNotFoundException("File: " + file + " does not exist");
            }
            final String[] resources = new String[1];
            resources[0] = sessionAccesorFile.getCanonicalPath();
            return resources;
        } catch (final BonitaHomeNotSetException e) {
            throw new SessionAccessorNotFoundException(e);
        } catch (final IOException e) {
            throw new SessionAccessorNotFoundException(e);
        }
    }

    protected static SessionAccessor getSessionAccessor() {
        return getContext().getBean(SessionAccessor.class);
    }

    protected static FileSystemXmlApplicationContext getContext() {
        if (context == null) {
            initializeContext(null);
        }
        return context;
    }

    /**
     * @param object
     */
    public static synchronized void initializeContext(final ClassLoader classLoader) {
        if (context == null) {// synchronized null check
            context = new AbsoluteFileSystemXmlApplicationContext(getResources(), false, null);
            if (classLoader != null) {
                context.setClassLoader(classLoader);
            }
            context.refresh();
        }
    }

    public static void destroy() {
        if (context != null) {
            context.close();
            context = null;
        }
    }
}
