/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;

/**
 * @author Romain Bioteau
 *
 */
public class PersistenceUnitBuilder {

	private Document document;
	private Set<String> classes = new HashSet<String>();

	public PersistenceUnitBuilder() throws SBusinessDataRepositoryDeploymentException{
		document = initializeDefaultPersistenceDocument();
	}

	protected Document initializeDefaultPersistenceDocument() throws SBusinessDataRepositoryDeploymentException {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setValidating(false);
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new SBusinessDataRepositoryDeploymentException(e);
		}
		InputStream is = null ;
		try{
			is = JPABusinessDataRepositoryImpl.class.getResourceAsStream("persistence.xml");
			return documentBuilder.parse(is);
		} catch (SAXException e) {
			throw new SBusinessDataRepositoryDeploymentException(e);
		} catch (IOException e) {
			throw new SBusinessDataRepositoryDeploymentException(e);
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					throw new SBusinessDataRepositoryDeploymentException(e);
				}
			}
		}
	}

	public Document done() {
		insertClasses();
		return document;
	}

	protected void insertClasses() {
		Node persistenceUnitNode = getPersistenceUnitNode();
		Node refChild = ((Element)persistenceUnitNode).getElementsByTagName("properties").item(0);
		for(String classname : classes){
			Element classNode = document.createElement("class");
			classNode.setTextContent(classname);
			persistenceUnitNode.insertBefore(classNode, refChild);
		}

	}

	private Node getPersistenceUnitNode() {
		NodeList parentElement = document.getElementsByTagName("persistence-unit");
		return parentElement.item(0);
	}

	public PersistenceUnitBuilder addClass(String classname) {
		classes.add(classname);
		return this;
	}

}
