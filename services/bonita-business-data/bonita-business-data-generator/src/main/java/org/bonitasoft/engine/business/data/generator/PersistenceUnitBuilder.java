/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.generator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 */
public class PersistenceUnitBuilder {

    private final Document document;

    private final Set<String> classes = new HashSet<String>();

    public PersistenceUnitBuilder() throws ParserConfigurationException, SAXException, IOException {
        document = initializeDefaultPersistenceDocument();
    }

    protected Document initializeDefaultPersistenceDocument()
            throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        try {
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // security-compliant
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // security-compliant
        } catch (IllegalArgumentException e) {
            //ignored, if not supported by the implementation
        }
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final InputStream is = PersistenceUnitBuilder.class.getResourceAsStream("persistence.xml");
        try {
            return documentBuilder.parse(is);
        } finally {
            is.close();
        }
    }

    public Document done() {
        insertClasses();
        return document;
    }

    protected void insertClasses() {
        final Node persistenceUnitNode = getPersistenceUnitNode();
        final Node refChild = ((Element) persistenceUnitNode).getElementsByTagName("properties").item(0);
        for (final String classname : classes) {
            final Element classNode = document.createElement("class");
            classNode.setTextContent(classname);
            persistenceUnitNode.insertBefore(classNode, refChild);
        }

    }

    private Node getPersistenceUnitNode() {
        final NodeList parentElement = document.getElementsByTagName("persistence-unit");
        return parentElement.item(0);
    }

    public PersistenceUnitBuilder addClass(final String classname) {
        classes.add(classname);
        return this;
    }

}
