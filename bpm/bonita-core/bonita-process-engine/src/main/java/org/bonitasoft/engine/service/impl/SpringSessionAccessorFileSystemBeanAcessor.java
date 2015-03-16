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

import java.io.IOException;
import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * @author Matthieu Chaffotte
 */
public class SpringSessionAccessorFileSystemBeanAcessor {

    private static AbsoluteFileSystemXmlApplicationContext context;

    private static String[] getResources() {
        final BonitaHomeServer homeServer = BonitaHomeServer.getInstance();
        try {
            return homeServer.getPrePlatformInitConfigurationFiles();
        } catch (final BonitaHomeNotSetException e) {
            throw new RuntimeException("Bonita home not set");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static PropertiesPropertySource getPropertySource() {
       return new PropertiesPropertySource("pre-init", new Properties());
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

    public static synchronized void initializeContext(final ClassLoader classLoader) {
        if (context == null) {// synchronized null check
            context = new AbsoluteFileSystemXmlApplicationContext(getResources(), null);
            if (classLoader != null) {
                context.setClassLoader(classLoader);
            }
            context.getEnvironment().getPropertySources().addFirst(getPropertySource());
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
