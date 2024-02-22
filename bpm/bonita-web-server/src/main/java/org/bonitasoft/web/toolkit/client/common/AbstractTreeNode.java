/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.toolkit.client.common;

import org.bonitasoft.web.toolkit.client.common.json.JsonSerializable;

/**
 * @author Séverin Moussel
 * @param <VALUE_CLASS>
 */
public abstract class AbstractTreeNode<VALUE_CLASS> implements JsonSerializable {

    protected AbstractTreeNode<VALUE_CLASS> parent = null;

    public AbstractTreeNode() {
    }

    public AbstractTreeNode(final AbstractTreeNode<VALUE_CLASS> parent) {
        this.parent = parent;
    }

    public AbstractTreeNode<VALUE_CLASS> getParent() {
        return this.parent;
    }

    public void setParent(final AbstractTreeNode<VALUE_CLASS> parent) {
        this.parent = parent;
    }

    public abstract AbstractTreeNode<VALUE_CLASS> copy();

}
