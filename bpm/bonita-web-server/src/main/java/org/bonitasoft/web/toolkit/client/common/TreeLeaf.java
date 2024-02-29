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

import org.bonitasoft.web.toolkit.client.common.json.JSonSerializer;

/**
 * @author Séverin Moussel
 * @param <VALUE_CLASS>
 */
public class TreeLeaf<VALUE_CLASS> extends AbstractTreeNode<VALUE_CLASS> {

    protected VALUE_CLASS value;

    public TreeLeaf(final VALUE_CLASS value) {
        super();
        this.value = value;
    }

    public TreeLeaf(final AbstractTreeNode<VALUE_CLASS> parent, final VALUE_CLASS value) {
        super(parent);
        this.value = value;
    }

    public VALUE_CLASS getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value == null ? null : this.value.toString();
    }

    @Override
    public String toJson() {
        return JSonSerializer.serialize(this.value);
    }

    @Override
    public TreeLeaf<VALUE_CLASS> copy() {
        return new TreeLeaf<>(this.value);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TreeLeaf)) {
            return false;
        }

        return this.getValue().equals(((TreeLeaf<?>) o).getValue());
    }

}
