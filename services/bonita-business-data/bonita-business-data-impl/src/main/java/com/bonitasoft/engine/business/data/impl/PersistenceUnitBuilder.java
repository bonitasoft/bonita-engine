/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

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

    protected Document initializeDefaultPersistenceDocument() throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(false);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final InputStream is = JPABusinessDataRepositoryImpl.class.getResourceAsStream("persistence.xml");
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
