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
    import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
    import org.springframework.context.support.FileSystemXmlApplicationContext;

    /**
     * @author Matthieu Chaffotte
     */
    public class SpringPlatformInitFileSystemBeanAcessor {

        private static AbsoluteFileSystemXmlApplicationContext context;

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
                try {
                    final BonitaHomeServer homeServer = BonitaHomeServer.getInstance();

                    final String[] resources = homeServer.getPrePlatformInitConfigurationFiles();
                    context = new AbsoluteFileSystemXmlApplicationContext(resources, null);
                    if (classLoader != null) {
                        context.setClassLoader(classLoader);
                    }
                    final PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
                    final Properties properties = BonitaHomeServer.getInstance().getPrePlatformInitProperties();
                    configurer.setProperties(properties);
                    context.addBeanFactoryPostProcessor(configurer);
                    context.refresh();
                } catch (BonitaHomeNotSetException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public static void destroy() {
            if (context != null) {
                context.close();
                context = null;
            }
        }
    }
