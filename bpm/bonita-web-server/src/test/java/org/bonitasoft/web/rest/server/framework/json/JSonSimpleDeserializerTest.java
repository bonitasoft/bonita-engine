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
package org.bonitasoft.web.rest.server.framework.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.bonitasoft.web.toolkit.client.common.AbstractTreeNode;
import org.bonitasoft.web.toolkit.client.common.Tree;
import org.bonitasoft.web.toolkit.client.common.TreeIndexed;
import org.bonitasoft.web.toolkit.client.common.TreeLeaf;
import org.junit.Test;

/**
 * @author Séverin Moussel
 */
public class JSonSimpleDeserializerTest {

    @Test
    public void testMalformedJson() {
        try {
            JSonSimpleDeserializer.unserializeTree("[toto}");
        } catch (final Exception e) {
            return;
        }
        fail("Malformed Json must throw an exception");
    }

    @Test
    public void testEmptyInput() {
        final AbstractTreeNode<String> tree = JSonSimpleDeserializer.unserializeTree("");

        assertNull(tree);
    }

    @Test
    public void testSimpleObject() {
        final AbstractTreeNode<String> tree = JSonSimpleDeserializer
                .unserializeTree("{\"name\":\"toto\",\"path\":\"titi\"}");

        if (!(tree instanceof TreeIndexed<?>)) {
            fail("Fail to parse a simple object in JSON");
        }

        final TreeIndexed<String> tree2 = (TreeIndexed<String>) tree;
        assertEquals(tree2.getValue("name"), "toto");
        assertEquals(tree2.getValue("path"), "titi");
        assertNull(tree2.getValue("unassigned"));
    }

    @Test
    public void testEmptyObject() {
        final AbstractTreeNode<String> tree = JSonSimpleDeserializer.unserializeTree("{}");

        if (!(tree instanceof TreeIndexed<?>)) {
            fail("Fail to parse a simple object in JSON");
        }

        assertEquals("{}", tree.toJson());
    }

    @Test
    public void testSimpleArray() {
        final AbstractTreeNode<String> tree = JSonSimpleDeserializer.unserializeTree(" [\"name\",5,true]");

        if (!(tree instanceof Tree<?>)) {
            fail("Fail to parse a simple array in JSON");
        }

        final Tree<String> tree2 = (Tree<String>) tree;

        assertEquals("name", tree2.getValues().get(0));
        assertEquals("5", tree2.getValues().get(1));
        assertEquals("1", tree2.getValues().get(2));

        assertEquals("name", ((TreeLeaf<String>) tree2.get(0)).getValue());
        assertEquals("5", ((TreeLeaf<String>) tree2.get(1)).getValue());
        assertEquals("1", ((TreeLeaf<String>) tree2.get(2)).getValue());
    }

    @Test
    public void testEmptyArray() {
        final AbstractTreeNode<String> tree = JSonSimpleDeserializer.unserializeTree("[]");

        if (!(tree instanceof Tree<?>)) {
            fail("Fail to parse a simple array in JSON");
        }

        assertEquals("[]", tree.toJson());
    }

    @Test
    public void testOneElementArray() {
        final AbstractTreeNode<String> tree = JSonSimpleDeserializer.unserializeTree("[101]");

        if (!(tree instanceof Tree<?>)) {
            fail("Fail to parse a simple array in JSON");
        }

        assertEquals("[\"101\"]", tree.toJson());

        final Tree<String> tree2 = (Tree<String>) tree;

        assertEquals(tree2.getValues().get(0), "101");
    }

    @Test
    public void testObjectWithArray() {
        final AbstractTreeNode<String> tree = JSonSimpleDeserializer
                .unserializeTree("{\"name\":\"toto\",\"categories_id\":[1,2,5]}");

        if (!(tree instanceof TreeIndexed<?>)) {
            fail("Fail to parse a compound object in JSON");
        }

        final AbstractTreeNode<String> treeCat = ((TreeIndexed<String>) tree).get("categories_id");

        if (!(treeCat instanceof Tree<?>)) {
            fail("Fail to parse an array in an object in JSON");
        }

        final Tree<String> tree2 = (Tree<String>) treeCat;

        assertEquals(tree2.getValues().get(0), "1");
        assertEquals(tree2.getValues().get(1), "2");
        assertEquals(tree2.getValues().get(2), "5");
    }

}
