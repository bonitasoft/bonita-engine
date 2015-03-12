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
package org.bonitasoft.engine.identity;

import java.io.Serializable;

/**
 * represents a helper for creating a {@link CustomUserInfoDefinition}.
 *
 * @author Vincent Elcrin
 * @see CustomUserInfoDefinition
 * @since 6.3.1
 */
public class CustomUserInfoDefinitionCreator implements Serializable {

    private static final long serialVersionUID = 6929368716340973445L;

    private String name;

    private String description;

    /**
     * creates a new {@link CustomUserInfoDefinitionCreator} with the specified name
     *
     * @param name the name to set
     */
    public CustomUserInfoDefinitionCreator(final String name) {
        this.name = name;
    }

    /**
     * creates a new {@link CustomUserInfoDefinitionCreator} with the specified name and description
     *
     * @param name the name to set
     * @param description the description to set
     */
    public CustomUserInfoDefinitionCreator(final String name, final String description) {
        this(name);
        this.description = description;
    }

    /**
     * @return the {@link CustomUserInfoDefinitionCreator}'s name to create
     */
    public String getName() {
        return name;
    }

    /**
     * @return the {@link CustomUserInfoDefinitionCreator}'s description to create
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param name the {@link CustomUserInfoDefinitionCreator}'s name to create
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @param description the {@link CustomUserInfoDefinitionCreator}'s name to create
     */
    public void setDescription(final String description) {
        this.description = description;
    }
}
