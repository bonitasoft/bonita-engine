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

import org.bonitasoft.engine.bpm.flownode.FlowElementInstance;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Emmanuel Duchastenier
 */
public abstract class FlowElementInstanceImpl extends NamedElementImpl implements FlowElementInstance {

    private static final long serialVersionUID = -8382446613679794971L;

    private long parentContainerId;

    private long rootContainerId;

    private boolean aborting;

    public FlowElementInstanceImpl(final FlowElementInstance flowElementInstance) {
        super(flowElementInstance.getName());
        rootContainerId = flowElementInstance.getRootContainerId();
        parentContainerId = flowElementInstance.getRootContainerId();
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
    public String toString() {
        final StringBuilder stb = new StringBuilder(super.toString());
        stb.append("parentContainerId: ");
        stb.append(parentContainerId);
        stb.append("\n");
        stb.append("rootContainerId: ");
        stb.append(rootContainerId);
        stb.append("\n");
        stb.append("aborting: ");
        stb.append(aborting);
        stb.append("\n");
        return stb.toString();
    }
}
