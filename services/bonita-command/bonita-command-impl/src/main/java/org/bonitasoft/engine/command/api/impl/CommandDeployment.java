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
package org.bonitasoft.engine.command.api.impl;

/**
 * @author Elias Ricken de Medeiros
 */
public class CommandDeployment {

    private String name;

    private String description;

    private String implementation;

    public CommandDeployment() {
    }

    public CommandDeployment(final String name, final String description, final String implementation) {
        this.name = name;
        this.description = description;
        this.implementation = implementation;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImplementation() {
        return implementation;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CommandDeployment))
            return false;

        final CommandDeployment that = (CommandDeployment) o;

        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (implementation != null ? !implementation.equals(that.implementation) : that.implementation != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (implementation != null ? implementation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CommandDeployment{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", implementation='" + implementation + '\'' +
                '}';
    }
}
