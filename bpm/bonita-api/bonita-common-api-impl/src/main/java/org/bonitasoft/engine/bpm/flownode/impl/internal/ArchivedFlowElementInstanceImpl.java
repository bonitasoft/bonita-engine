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

import org.bonitasoft.engine.bpm.flownode.ArchivedFlowElementInstance;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Emmanuel Duchastenier
 */
public abstract class ArchivedFlowElementInstanceImpl extends NamedElementImpl implements ArchivedFlowElementInstance {

    private static final long serialVersionUID = -8382446613679794971L;

    private long parentContainerId;

    private long rootContainerId;

    private boolean aborting;

    private String description;

    public ArchivedFlowElementInstanceImpl(final String name) {
        super(name);
    }

    @Override
    public long getRootContainerId() {
        return rootContainerId;
    }

    public void setRootContainerId(final long rootContainerId) {
        this.rootContainerId = rootContainerId;
    }

    @Override
    public long getParentContainerId() {
        return parentContainerId;
    }

    public void setParentContainerId(final long parentContainerId) {
        this.parentContainerId = parentContainerId;
    }

    @Override
    public boolean isAborting() {
        return aborting;
    }

    public void setAborting(final boolean aborting) {
        this.aborting = aborting;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }
}
