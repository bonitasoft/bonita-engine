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

/**
 * @author Séverin Moussel
 * @param <VALUE_CLASS>
 */
public class TreeNode<VALUE_CLASS> extends Tree<VALUE_CLASS> {

    public TreeNode() {
        super();
    }

    @Override
    public TreeNode<VALUE_CLASS> copy() {
        final TreeNode<VALUE_CLASS> result = new TreeNode<>();

        for (final AbstractTreeNode<VALUE_CLASS> child : this.children) {
            result.addNode(child.copy());
        }

        return result;
    }

}
