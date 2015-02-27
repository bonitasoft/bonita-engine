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
package org.bonitasoft.engine.platform.command.impl;

import org.bonitasoft.engine.platform.command.PlatformCommandDescriptor;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
public class PlatformCommandDescriptorImpl implements PlatformCommandDescriptor {

    private static final long serialVersionUID = 5126055774577086808L;

    private long id;

    private String name;

    private String description;

    private String implementation;

    public PlatformCommandDescriptorImpl() {
        super();
    }

    PlatformCommandDescriptorImpl(final PlatformCommandDescriptor command) {
        name = command.getName();
        description = command.getDescription();
        implementation = command.getImplementation();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getImplementation() {
        return implementation;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setImplementation(final String implementation) {
        this.implementation = implementation;
    }
}
