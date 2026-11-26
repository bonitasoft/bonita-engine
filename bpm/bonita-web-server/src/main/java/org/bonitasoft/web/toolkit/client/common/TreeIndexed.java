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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.web.toolkit.client.common.json.JSonSerializer;

/**
 * @author Séverin Moussel
 * @param <VALUE_CLASS>
 */
public class TreeIndexed<VALUE_CLASS> extends AbstractTreeNode<VALUE_CLASS> {

    protected Map<String, AbstractTreeNode<VALUE_CLASS>> children = new HashMap<>();

    public TreeIndexed() {
    }

    public TreeIndexed(final AbstractTreeNode<VALUE_CLASS> parent, final Map<String, VALUE_CLASS> map) {
        this(parent);
        this.addValues(map);
    }

    public TreeIndexed(final AbstractTreeNode<VALUE_CLASS> parent) {
        super(parent);
    }

    public void addNode(final String key, final AbstractTreeNode<VALUE_CLASS> node) {
        if (node == null) {
            return;
        }

        node.setParent(this);

        this.children.put(key, node);
    }

    public void addValue(final String key, final VALUE_CLASS value) {
        if (value != null) {
            this.addNode(key, new TreeLeaf<>(this, value));
        }
    }

    public void addValues(final Map<String, VALUE_CLASS> values) {
        if (values != null) {
            for (final String key : values.keySet()) {
                this.addValue(key, values.get(key));
            }
        }

    }

    public LinkedHashMap<String, VALUE_CLASS> getValues() {
        final LinkedHashMap<String, VALUE_CLASS> values = new LinkedHashMap<>();
        for (final String key : this.children.keySet()) {
            final AbstractTreeNode<VALUE_CLASS> node = this.children.get(key);
            if (node instanceof TreeLeaf) {
                values.put(key, ((TreeLeaf<VALUE_CLASS>) node).getValue());
            }
        }

        return values;
    }

    public AbstractTreeNode<VALUE_CLASS> get(final String key) {
        return this.children.get(key);
    }

    public Set<String> keySet() {
        return this.children.keySet();
    }

    public VALUE_CLASS getValue(final String key) {
        final AbstractTreeNode<VALUE_CLASS> node = this.children.get(key);
        if (node instanceof TreeLeaf<?>) {
            return ((TreeLeaf<VALUE_CLASS>) node).getValue();
        }
        return null;
    }

    @Override
    public String toString() {
        return this.children.toString();
    }

    public int size() {
        return this.children.size();
    }

    @Override
    public String toJson() {
        return JSonSerializer.serializeMap(this.children);
    }

    @Override
    public TreeIndexed<VALUE_CLASS> copy() {
        final TreeIndexed<VALUE_CLASS> result = new TreeIndexed<>();

        for (final String key : this.children.keySet()) {
            result.addNode(key, this.children.get(key).copy());
        }

        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TreeIndexed<?> tree)) {
            return false;
        }

        if (tree.size() != this.size()) {
            return false;
        }

        for (final String key : this.keySet()) {
            if (!this.get(key).equals(tree.get(key))) {
                return false;
            }
        }

        for (final String key : tree.keySet()) {
            if (!tree.get(key).equals(this.get(key))) {
                return false;
            }
        }

        return true;
    }

    public Map<String, AbstractTreeNode<VALUE_CLASS>> getNodes() {
        return this.children;
    }

}
