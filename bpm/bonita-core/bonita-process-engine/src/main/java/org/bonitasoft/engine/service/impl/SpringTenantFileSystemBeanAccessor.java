/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author Matthieu Chaffotte
 */
public class SpringTenantFileSystemBeanAccessor {

    private AbsoluteFileSystemXmlApplicationContext context;

    private final long tenantId;

    public SpringTenantFileSystemBeanAccessor(final long tenantId) {
        this.tenantId = tenantId;
    }

    private String[] getResources() {
        final BonitaHomeServer homeServer = BonitaHomeServer.getInstance();
        try {
            final String tenantConfFolder = homeServer.getTenantConfFolder(tenantId);
            final File serviceFolder = new File(tenantConfFolder + File.separatorChar + "services");
            if (!serviceFolder.isDirectory()) {
                throw new RuntimeException("Your bonita.home is corrupted: Folder 'services' not found on tenant " + tenantConfFolder);
            }
            final FileFilter filter = new FileFilter() {

                @Override
                public boolean accept(final File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".xml");
                }
            };

            final File[] listFiles = serviceFolder.listFiles(filter);
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
        } catch (final BonitaHomeNotSetException e) {
            throw new RuntimeException("Bonita home not set");
        }
    }

    public <T> T getService(final Class<T> serviceClass) {
        return getContext().getBean(serviceClass);
    }

    protected <T> T getService(final String name, final Class<T> serviceClass) {
        return getContext().getBean(name, serviceClass);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getService(final String name) {
        return (T) getContext().getBean(name);
    }

    protected FileSystemXmlApplicationContext getContext() {
        if (context == null) {
            initializeContext(null);
        }
        return context;
    }

    public synchronized void initializeContext(final ClassLoader classLoader) {
        if (context == null) {// synchronized null check
            // Inject the tenantId as a resolvable placeholder for the bean definitions.
            Properties properties = new Properties();
            properties.put("tenantId", String.valueOf(tenantId));
            try {
                properties.putAll(BonitaHomeServer.getInstance().getTenantProperties(tenantId));
            } catch (BonitaHomeNotSetException e) {
                throw new IllegalStateException(e);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
            ppc.setProperties(properties);
            SpringPlatformFileSystemBeanAccessor.initializeContext(classLoader);
            final FileSystemXmlApplicationContext platformContext = SpringPlatformFileSystemBeanAccessor.getContext();
            // Delay the refresh so we can set our BeanFactoryPostProcessor to be able to resolve the placeholder.
            AbsoluteFileSystemXmlApplicationContext localContext = new AbsoluteFileSystemXmlApplicationContext(getResources(), false /* refresh */,
                    platformContext);
            localContext.addBeanFactoryPostProcessor(ppc);

            localContext.refresh();
            this.context = localContext;
        }
    }

    public void destroy() {
        if (context != null) {
            context.close();
            context = null;
        }
    }

}
