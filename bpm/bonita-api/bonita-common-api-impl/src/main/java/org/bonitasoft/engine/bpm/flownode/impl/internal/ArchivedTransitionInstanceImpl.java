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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import java.util.Date;

import org.bonitasoft.engine.bpm.flownode.ArchivedTransitionInstance;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class ArchivedTransitionInstanceImpl extends ArchivedFlowElementInstanceImpl implements ArchivedTransitionInstance {

    private static final long serialVersionUID = -2207862352776100380L;

    private long source;

    private long target;

    private long sourceObjectId;

    private Date archiveDate;

    private String description;

    private boolean terminal;

    private boolean stable;

    public ArchivedTransitionInstanceImpl(final String name) {
        super(name);
    }

    public void setSource(final long source) {
        this.source = source;
    }

    public void setTarget(final long target) {
        this.target = target;
    }

    @Override
    public long getSource() {
        return source;
    }

    @Override
    public long getTarget() {
        return target;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(final long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(final Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(final boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isStable() {
        return stable;
    }

    public void setStable(final boolean stable) {
        this.stable = stable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (archiveDate == null ? 0 : archiveDate.hashCode());
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (int) (source ^ source >>> 32);
        result = prime * result + (int) (sourceObjectId ^ sourceObjectId >>> 32);
        result = prime * result + (stable ? 1231 : 1237);
        result = prime * result + (int) (target ^ target >>> 32);
        result = prime * result + (terminal ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ArchivedTransitionInstanceImpl other = (ArchivedTransitionInstanceImpl) obj;
        if (archiveDate == null) {
            if (other.archiveDate != null) {
                return false;
            }
        } else if (!archiveDate.equals(other.archiveDate)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (source != other.source) {
            return false;
        }
        if (sourceObjectId != other.sourceObjectId) {
            return false;
        }
        if (stable != other.stable) {
            return false;
        }
        if (target != other.target) {
            return false;
        }
        if (terminal != other.terminal) {
            return false;
        }
        return true;
    }

}
