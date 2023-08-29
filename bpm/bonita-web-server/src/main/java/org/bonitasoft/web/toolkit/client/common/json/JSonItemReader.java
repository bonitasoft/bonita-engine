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
package org.bonitasoft.web.toolkit.client.common.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.web.toolkit.client.common.AbstractTreeNode;
import org.bonitasoft.web.toolkit.client.common.Tree;
import org.bonitasoft.web.toolkit.client.common.TreeIndexed;
import org.bonitasoft.web.toolkit.client.common.TreeLeaf;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * This class is just a set of functions used to read the JSon returned by the server side
 *
 * @author Séverin Moussel
 */
public class JSonItemReader {

    public static final boolean APPLY_VALIDATORS = true;

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UNSERIALIZER
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static JSonUnserializer UNSERIALIZER = null;

    /**
     * @param unserializer
     *        the unserializer to set
     */
    public static void setUnserializer(final JSonUnserializer unserializer) {
        UNSERIALIZER = unserializer;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // PARSING
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Parse a Map on a JSon String
     *
     * @param json
     */
    public static Map<String, String> parseMap(final String json) {
        final AbstractTreeNode<String> tree = UNSERIALIZER._unserializeTree(json);

        if (!(tree instanceof TreeIndexed<?>)) {
            return new HashMap<>();
        }

        return parseMap((TreeIndexed<String>) tree);
    }

    /**
     * Parse a Map on a Tree
     *
     * @param tree
     */
    private static Map<String, String> parseMap(final TreeIndexed<String> tree) {
        final Map<String, String> result = new HashMap<>();

        for (final Entry<String, String> entry : tree.getValues().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Parse an item based on a JSon String
     *
     * @param json
     * @param itemDefinition
     */
    public static <E extends IItem> E parseItem(final String json, final ItemDefinition<E> itemDefinition) {
        AbstractTreeNode<String> tree = UNSERIALIZER._unserializeTree(json);

        if (tree instanceof Tree<?>) {
            tree = ((Tree<String>) tree).get(0);

            if (!(tree instanceof TreeIndexed<?>)) {
                return itemDefinition.createItem();
            }
        }

        return parseItem((TreeIndexed<String>) tree, itemDefinition);
    }

    /**
     * Parse an item based on a TreeIndexed
     */
    private static <E extends IItem> E parseItem(final TreeIndexed<String> tree,
            final ItemDefinition<E> itemDefinition) {
        return parseItem(tree, itemDefinition, APPLY_VALIDATORS);
    }

    /**
     * Parse an item based on a TreeIndexed
     *
     * @param tree
     * @param itemDefinition
     */
    private static <E extends IItem> E parseItem(final TreeIndexed<String> tree, final ItemDefinition<E> itemDefinition,
            final boolean applyValidators) {
        final E item = itemDefinition.createItem();

        item.setApplyValidators(applyValidators);

        for (final Entry<String, AbstractTreeNode<String>> entry : tree.getNodes().entrySet()) {
            // primitive type
            if (entry.getValue() instanceof TreeLeaf<?>) {
                item.setAttribute(entry.getKey(), ((TreeLeaf<String>) entry.getValue()).getValue());
                // json object
            } else if (entry.getValue() instanceof TreeIndexed<?>) {
                item.setDeploy(
                        entry.getKey(),
                        parseItem(
                                (TreeIndexed<String>) entry.getValue(),
                                itemDefinition.getDeployDefinition(entry.getKey()))

                );
                // json list - set directly json in attribute value
            } else if (entry.getValue() instanceof Tree<?>) {
                item.setAttribute(entry.getKey(), entry.getValue().toJson());
            }
        }

        return item;
    }

}
