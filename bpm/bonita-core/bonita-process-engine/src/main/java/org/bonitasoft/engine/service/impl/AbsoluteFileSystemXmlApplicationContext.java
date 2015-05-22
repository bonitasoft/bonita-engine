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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Matthieu Chaffotte
 */
public class AbsoluteFileSystemXmlApplicationContext extends FileSystemXmlApplicationContext {

    /**
     * Create a new FileSystemXmlApplicationContext with the given parent,
     * loading the definitions from the given XML files and automatically
     * refreshing the context.
     * 
     * @param configLocations
     *            array of file paths
     * @param parent
     *            the parent context
     * @throws BeansException
     *             if context creation failed
     */
    public AbsoluteFileSystemXmlApplicationContext(final String[] configLocations, final ApplicationContext parent)
            throws BeansException {
        super(configLocations, false, parent);

    }

    @Override
    protected Resource getResourceByPath(final String path) {
        return new FileSystemResource(path);
    }

}
