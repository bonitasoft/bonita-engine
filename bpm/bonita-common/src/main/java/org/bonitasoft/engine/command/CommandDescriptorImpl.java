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
package org.bonitasoft.engine.command;


/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
public class CommandDescriptorImpl implements CommandDescriptor {

    private static final long serialVersionUID = -8798112164975720185L;

    private long id;

    private String name;

    private String description;

    private String implementation;

    private boolean system;

    public CommandDescriptorImpl() {
        super();
    }

    public CommandDescriptorImpl(final String name, final String description, final String implementation) {
        this.name = name;
        this.description = description;
        this.implementation = implementation;
    }

    CommandDescriptorImpl(final CommandDescriptor command) {
        name = command.getName();
        description = command.getDescription();
        implementation = command.getImplementation();
        system = command.isSystemCommand();
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

    @Override
    public boolean isSystemCommand() {
        return system;
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

    public void setSystem(final boolean system) {
        this.system = system;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (implementation == null ? 0 : implementation.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (system ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CommandDescriptorImpl other = (CommandDescriptorImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (implementation == null) {
            if (other.implementation != null) {
                return false;
            }
        } else if (!implementation.equals(other.implementation)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (system != other.system) {
            return false;
        }
        return true;
    }

}
