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
package org.bonitasoft.engine.io.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Baptiste Mesta
 */
public class XMLNode {

    private final String name;

    private final Map<String, String> attributes;

    private final List<XMLNode> childNodes;

    private String content;

    public XMLNode(final String name) {
        this.name = name;
        attributes = new HashMap<String, String>();
        childNodes = new ArrayList<XMLNode>();
    }

    public void addAttribute(final String name, final String value) {
        attributes.put(name, value);
    }

    public void addChild(final XMLNode node) {
        if (node != null) {
            childNodes.add(node);
        }
    }

    public void addChild(final String name, final String content) {
        if (name != null && content != null) {
            final XMLNode node = new XMLNode(name);
            node.setContent(content);
            childNodes.add(node);
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public List<XMLNode> getChildNodes() {
        return childNodes;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}
