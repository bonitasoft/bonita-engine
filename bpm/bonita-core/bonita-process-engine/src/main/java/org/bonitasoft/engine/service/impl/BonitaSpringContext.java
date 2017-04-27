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

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Matthieu Chaffotte
 */
public class BonitaSpringContext extends AbstractXmlApplicationContext {

    private List<Resource> resources = new ArrayList<>();


    /**
     * Create a new XmlApplicationContext with the given parent,
     * loading the definitions from the given XML files and automatically
     * refreshing the context.
     *
     * @param parent the parent context
     * @throws BeansException if context creation failed
     */
    public BonitaSpringContext(ApplicationContext parent) throws BeansException {
        super(parent);

    }

    @Override
    protected Resource[] getConfigResources() {
        return resources.toArray(new Resource[resources.size()]);
    }

    public void addClassPathResource(String location) {
        ClassPathResource classPathResource = new ClassPathResource(location);
        if (classPathResource.exists()) {
            resources.add(classPathResource);
        }
    }

    public void addByteArrayResource(BonitaConfiguration configuration) {
        resources.add(new ByteArrayResource(configuration.getResourceContent(), configuration.getResourceName()));
    }
}
