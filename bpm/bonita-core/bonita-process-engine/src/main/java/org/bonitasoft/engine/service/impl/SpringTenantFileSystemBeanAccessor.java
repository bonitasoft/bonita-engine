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
import org.bonitasoft.engine.exception.MissingServiceException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * @author Matthieu Chaffotte
 * @author Charles Souillard
 */
public class SpringTenantFileSystemBeanAccessor extends SpringFileSystemBeanAccessor {
    private final long tenantId;

    public SpringTenantFileSystemBeanAccessor(final SpringFileSystemBeanAccessor parent, final long tenantId) throws IOException, BonitaHomeNotSetException {
        super(parent);
        this.tenantId = tenantId;
    }


    @Override
    protected String[] getResources() throws IOException, BonitaHomeNotSetException {
        return BonitaHomeServer.getInstance().getTenantConfigurationFiles(tenantId);
    }

    @Override
    protected Properties getProperties() throws BonitaHomeNotSetException, IOException {
        return BonitaHomeServer.getInstance().getTenantProperties(tenantId);
    }

}
