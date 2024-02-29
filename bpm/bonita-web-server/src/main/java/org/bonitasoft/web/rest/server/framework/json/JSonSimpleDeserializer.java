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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.web.toolkit.client.common.AbstractTreeNode;
import org.bonitasoft.web.toolkit.client.common.Tree;
import org.bonitasoft.web.toolkit.client.common.TreeIndexed;
import org.bonitasoft.web.toolkit.client.common.TreeLeaf;
import org.bonitasoft.web.toolkit.client.common.json.JSonUnserializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author Séverin Moussel
 */
public class JSonSimpleDeserializer implements JSonUnserializer {

    private static JSonSimpleDeserializer INSTANCE = null;

    private static JSonSimpleDeserializer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JSonSimpleDeserializer();
        }
        return INSTANCE;
    }

    public static AbstractTreeNode<String> unserializeTree(final String json) {

        return getInstance()._unserializeTree(json);
    }

    @Override
    public AbstractTreeNode<String> _unserializeTree(final String json) {
        try {
            if (json.length() == 0) {
                return null;
            }

            return unserializeTreeNode(new JSONParser().parse(json));
        } catch (final ParseException e) {
            throw new IllegalArgumentException("Can't parse JSon", e);
        }
    }

    private AbstractTreeNode<String> unserializeTreeNode(final Object object) {
        if (object instanceof JSONObject) {
            return unserializeTreeNode((JSONObject) object);
        } else if (object instanceof JSONArray) {
            return unserializeTreeNode((JSONArray) object);
        } else if (object instanceof Boolean) {
            return new TreeLeaf<>(((Boolean) object).booleanValue() ? "1" : "0");
        }
        return new TreeLeaf<>(object.toString());
    }

    private TreeIndexed<String> unserializeTreeNode(final JSONObject object) {
        final TreeIndexed<String> result = new TreeIndexed<>();

        @SuppressWarnings("rawtypes")
        final Iterator iter = object.entrySet().iterator();
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
            final Map.Entry entry = (Entry) iter.next();
            result.addNode(entry.getKey().toString(), unserializeTreeNode(entry.getValue()));
        }
        return result;
    }

    private Tree<String> unserializeTreeNode(final JSONArray array) {
        final Tree<String> result = new Tree<>();
        final int size = array.size();
        for (int i = 0; i < size; i++) {
            result.addNode(unserializeTreeNode(array.get(i)));
        }
        return result;
    }

}
