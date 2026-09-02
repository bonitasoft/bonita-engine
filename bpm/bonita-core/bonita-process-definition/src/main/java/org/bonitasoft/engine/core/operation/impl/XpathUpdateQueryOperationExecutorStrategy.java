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
package org.bonitasoft.engine.core.operation.impl;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.xml.DocumentManager;
import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
@Component
public class XpathUpdateQueryOperationExecutorStrategy implements OperationExecutorStrategy {

    public static final String TYPE_XPATH_UPDATE_QUERY = "XPATH_UPDATE_QUERY";

    public XpathUpdateQueryOperationExecutorStrategy() {
    }

    @Override
    public String getOperationType() {
        return TYPE_XPATH_UPDATE_QUERY;
    }

    private String getStringValue(final Object variableValue) {
        if (variableValue instanceof String) {
            return (String) variableValue;
        }
        return String.valueOf(variableValue);
    }

    private boolean isSetAttribute(final String xpathExpression, final Object variableValue) {
        if (variableValue instanceof Attr) {
            return true;
        }
        final String[] segments = xpathExpression.split("/");
        return segments[segments.length - 1].startsWith("@");
    }

    @Override
    public Object computeNewValueForLeftOperand(final SOperation operation, final Object value,
            final SExpressionContext expressionContext,
            final boolean shouldPersistValue) throws SOperationExecutionException {
        try {
            final String dataInstanceName = operation.getLeftOperand().getName();
            // should be a String because the data is an xml expression
            final String dataValue = (String) expressionContext.getInputValues().get(dataInstanceName);

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try {
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // security-compliant
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // security-compliant
            } catch (IllegalArgumentException e) {
                //ignored, if not supported by the implementation
            }
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(new InputSource(new StringReader(dataValue)));
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final String xpathExpression = operation.getOperator();
            final Node node = (Node) xpath.compile(xpathExpression).evaluate(document, XPathConstants.NODE);
            if (isSetAttribute(xpathExpression, value)) {
                if (node == null) { // Create the attribute
                    final String parentPath = xpathExpression.substring(0, xpathExpression.lastIndexOf('/'));
                    final String attributeName = xpathExpression.substring(xpathExpression.lastIndexOf('/') + 2); // +1 for @
                    final Node parentNode = (Node) xpath.compile(parentPath).evaluate(document, XPathConstants.NODE);
                    if (parentNode instanceof Element element) {
                        if (value instanceof String) {
                            element.setAttribute(attributeName, getStringValue(value));
                        } else if (value instanceof Attr) {
                            element.setAttribute(((Attr) value).getName(),
                                    ((Attr) value).getTextContent());
                        }
                    }
                } else if (node instanceof Attr) { // Set an existing attribute
                    if (value instanceof Attr) {
                        node.setTextContent(((Attr) value).getTextContent());
                    } else {
                        node.setTextContent(getStringValue(value));
                    }
                } else if (node instanceof Element) { // add attribute to an element
                    final Attr attr = (Attr) value;
                    ((Element) node).setAttribute(attr.getName(), attr.getValue());
                }
            } else if (node instanceof Text) {
                node.setTextContent(getStringValue(value));
            } else if (node instanceof Element) {
                Node newNode = null;
                if (value instanceof Node) {
                    newNode = document.importNode((Node) value, true);
                } else if (value instanceof String) {
                    newNode = document.importNode(
                            DocumentManager.generateDocument(getStringValue(value)).getDocumentElement(), true);
                }

                // if (isAppend) {
                // node.appendChild(newNode);
                // } else { // replace
                final Node parentNode = node.getParentNode();
                parentNode.removeChild(node);
                parentNode.appendChild(newNode);
                // }
            } else if (node == null && xpathExpression.endsWith("/text()") && value instanceof String) {
                final String parentPath = xpathExpression.substring(0, xpathExpression.lastIndexOf('/'));
                final Node parentNode = (Node) xpath.compile(parentPath).evaluate(document, XPathConstants.NODE);
                parentNode.appendChild(document.createTextNode(getStringValue(value)));
            }
            return DocumentManager.getDocumentContent(document);
        } catch (final ParserConfigurationException | SAXException | IOException | XPathExpressionException
                | TransformerFactoryConfigurationError | TransformerException pce) {
            throw new SOperationExecutionException(pce);
        }
    }

    @Override
    public boolean shouldPersistOnNullValue() {
        return false;
    }
}
