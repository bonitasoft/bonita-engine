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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.web.toolkit.client.common.json.JSonSerializer;

/**
 * @author Séverin Moussel
 * @param <VALUE_CLASS>
 */
public class Tree<VALUE_CLASS> extends AbstractTreeNode<VALUE_CLASS>
        implements Iterable<AbstractTreeNode<VALUE_CLASS>> {

    protected List<AbstractTreeNode<VALUE_CLASS>> children = new ArrayList<>();

    public Tree() {
    }

    public void addNode(final AbstractTreeNode<VALUE_CLASS> node) {
        if (node == null) {
            return;
        }
        node.setParent(this);
        this.children.add(node);
    }

    public void addValue(final VALUE_CLASS value) {
        if (value != null && !this.contains(value)) {
            this.addNode(new TreeLeaf<>(this, value));
        }
    }

    public List<VALUE_CLASS> getValues() {
        final List<VALUE_CLASS> values = new ArrayList<>();
        for (final AbstractTreeNode<VALUE_CLASS> node : this.children) {
            if (node instanceof TreeLeaf) {
                values.add(((TreeLeaf<VALUE_CLASS>) node).getValue());
            }
        }

        return values;
    }

    public AbstractTreeNode<VALUE_CLASS> get(final int index) {
        return this.children.get(index);
    }

    @Override
    public String toString() {
        return this.children.toString();
    }

    public int size() {
        return this.children.size();
    }

    public boolean contains(final VALUE_CLASS value) {
        return this.getValues().contains(value);
    }

    @Override
    public Iterator<AbstractTreeNode<VALUE_CLASS>> iterator() {
        return this.children.iterator();
    }

    @Override
    public String toJson() {
        return JSonSerializer.serializeCollection(this.children);
    }

    @Override
    public Tree<VALUE_CLASS> copy() {
        final Tree<VALUE_CLASS> result = new Tree<>();

        for (final AbstractTreeNode<VALUE_CLASS> child : this.children) {
            result.addNode(child.copy());
        }

        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Tree)) {
            return false;
        }

        final Tree<?> tree = (Tree<?>) o;

        if (tree.size() != this.size()) {
            return false;
        }

        for (final VALUE_CLASS valueThis : this.getValues()) {
            for (final Object valueOther : tree.getValues()) {
                if (!valueThis.equals(valueOther)) {
                    return false;
                }
            }
        }

        for (final Object valueOther : tree.getValues()) {
            for (final VALUE_CLASS valueThis : this.getValues()) {
                if (!valueThis.equals(valueOther)) {
                    return false;
                }

            }
        }

        return true;
    }

    public List<AbstractTreeNode<VALUE_CLASS>> getNodes() {
        return this.children;
    }
}
