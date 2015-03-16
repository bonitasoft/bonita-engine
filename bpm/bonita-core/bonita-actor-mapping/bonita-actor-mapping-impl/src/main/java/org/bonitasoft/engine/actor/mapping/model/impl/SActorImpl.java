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
package org.bonitasoft.engine.actor.mapping.model.impl;

import org.bonitasoft.engine.actor.mapping.model.SActor;

/**
 * @author Matthieu Chaffotte
 */
public class SActorImpl implements SActor {

    private static final long serialVersionUID = -6333033389852045788L;

    private long tenantId;

    private long id;

    private long scopeId;

    private String name;

    private String displayName;

    private String description;

    private boolean initiator;

    public SActorImpl() {
        super();
    }

    public SActorImpl(final String name, final long scopeId, final boolean initiator) {
        this.name = name;
        this.scopeId = scopeId;
        this.initiator = initiator;
    }

    public SActorImpl(final SActor actor) {
        this.name = actor.getName();
        this.scopeId = actor.getScopeId();
        this.description = actor.getDescription();
    }

    public long getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public long getScopeId() {
        return scopeId;
    }

    public void setScopeId(final long scopeId) {
        this.scopeId = scopeId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getDiscriminator() {
        return SActorImpl.class.getName();
    }

    @Override
    public boolean isInitiator() {
        return initiator;
    }

    public void setInitiator(final boolean initiator) {
        this.initiator = initiator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + (initiator ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (scopeId ^ (scopeId >>> 32));
        result = prime * result + (int) (tenantId ^ (tenantId >>> 32));
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
        final SActorImpl other = (SActorImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (initiator != other.initiator) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (scopeId != other.scopeId) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

}
