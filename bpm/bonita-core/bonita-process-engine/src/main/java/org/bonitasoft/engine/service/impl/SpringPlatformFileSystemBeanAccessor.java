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
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SpringPlatformFileSystemBeanAccessor {

    private static AbsoluteFileSystemXmlApplicationContext context;

    private static String[] getResources() {
        final BonitaHomeServer homeServer = BonitaHomeServer.getInstance();
        try {
            return homeServer.getPlatformConfigurationFiles();
        } catch (final BonitaHomeNotSetException e) {
            throw new RuntimeException("Bonita home not set");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static PropertiesPropertySource getPropertySource() {
        try {
            final Properties props = BonitaHomeServer.getInstance().getPlatformProperties();
            return new PropertiesPropertySource("platform", props);
        } catch (final BonitaHomeNotSetException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
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
            context = new AbsoluteFileSystemXmlApplicationContext(getResources(), sessionContext);
            context.getEnvironment().getPropertySources().addFirst(getPropertySource());
            context.refresh();

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
