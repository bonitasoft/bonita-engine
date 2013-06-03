/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.impl;

import org.bonitasoft.engine.identity.model.SNamedElement;

/**
 * All elements that have name label and description
 * 
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class SNamedElementImpl extends SPersistentObjectImpl implements SNamedElement {

    private static final long serialVersionUID = 4982939032656177720L;

    private String name;

    private String displayName;

    private String description;

    public SNamedElementImpl() {
        super();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final SNamedElementImpl other = (SNamedElementImpl) obj;
        if (this.description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!this.description.equals(other.description)) {
            return false;
        }
        if (this.displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!this.displayName.equals(other.displayName)) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.description == null ? 0 : this.description.hashCode());
        result = prime * result + (this.displayName == null ? 0 : this.displayName.hashCode());
        result = prime * result + (this.name == null ? 0 : this.name.hashCode());
        return result;
    }

}
