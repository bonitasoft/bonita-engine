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
package org.bonitasoft.engine.expression.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Supported return types are:
 * <ul>
 * <li>java.lang.String</li>
 * <li>java.lang.Double</li>
 * <li>java.lang.Boolean</li>
 * <li>java.lang.Long</li>
 * <li>java.lang.Integer</li>
 * <li>java.lang.Float</li>
 * <li>org.w3c.dom.Node</li>
 * <li>org.w3c.dom.NodeList</li>
 * </ul>
 * 
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class XPathReadExpressionExecutorStrategy implements ExpressionExecutorStrategy {

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        if (expression.getDependencies().size() != 1 || expression.getDependencies().get(0) == null) {
            throw new SExpressionDependencyMissingException("XPathReadExpressionExecutorStrategy must have exactly one dependency");
        }

        final String expressionName = expression.getName();
        final String returnType = expression.getReturnType();
        final String messageForException = "Error evaluating expression " + expression + " with strategy XPathReadExpressionExecutorStrategy";
        try {
            final QName qname = getXPathConstants(returnType);
            if (qname == null) {
                throw new SExpressionEvaluationException("XPathReadExpressionExecutorStrategy return type not supported: " + expression.getReturnType(),
                        expressionName);
            }
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            // Check has already been done above:
            final SExpression dep = expression.getDependencies().get(0);
            final String xmlContent = (String) resolvedExpressions.get(dep.getDiscriminant());
            if (xmlContent == null || xmlContent.isEmpty()) {
                throw new SExpressionEvaluationException("The content of the xml is nul or empty: " + expression, expressionName);
            }
            final Document document = builder.parse(new InputSource(new StringReader(xmlContent)));
            final XPathFactory xpFactory = XPathFactory.newInstance();
            final XPath xpath = xpFactory.newXPath();
            final XPathExpression exp = xpath.compile(expression.getContent());
            return transType(exp.evaluate(document, qname), returnType);
        } catch (final XPathExpressionException e) {
            throw new SExpressionEvaluationException(messageForException, e, expressionName);
        } catch (final ParserConfigurationException e) {
            throw new SExpressionEvaluationException(messageForException, e, expressionName);
        } catch (final SAXException e) {
            throw new SExpressionEvaluationException(messageForException, e, expressionName);
        } catch (final IOException e) {
            throw new SExpressionEvaluationException(messageForException, e, expressionName);
        } catch (final SBonitaRuntimeException e) {
            throw new SExpressionEvaluationException(messageForException, e, expressionName);
        }
    }

    private Object transType(final Object result, final String returnType) {
        try {
            if (Boolean.class.getName().equals(returnType)) {
                return result != null && ("true".equalsIgnoreCase((String) result) || "1".equals(result));
            }
            if (Long.class.getName().equals(returnType)) {
                return Long.parseLong((String) result);
            }
            if (Double.class.getName().equals(returnType)) {
                return Double.parseDouble((String) result);
            }
            if (Float.class.getName().equals(returnType)) {
                return Float.parseFloat((String) result);
            }
            if (Integer.class.getName().equals(returnType)) {
                return Integer.parseInt((String) result);
            }
            if (String.class.getName().equals(returnType)) {
                return result;
            }
        } catch (final NumberFormatException e) {
            throw new SBonitaRuntimeException("Wrong format for " + returnType + " value was " + result, e);
        }
        return result;
    }

    private QName getXPathConstants(final String expReturnType) {
        if (String.class.getName().equals(expReturnType)) {
            return XPathConstants.STRING;
        } else if (Long.class.getName().equals(expReturnType) || Double.class.getName().equals(expReturnType) || Float.class.getName().equals(expReturnType)
                || Integer.class.getName().equals(expReturnType)) {
            return XPathConstants.STRING;
        } else if (Boolean.class.getName().equals(expReturnType)) {
            return XPathConstants.STRING;
        } else if (Node.class.getName().equals(expReturnType)) {
            return XPathConstants.NODE;
        } else if (NodeList.class.getName().equals(expReturnType)) {
            return XPathConstants.NODESET;
        }
        return null;
    }

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        if (expression == null) {
            throw new SInvalidExpressionException("The expression cannot be null.", null);
        }
        final String expressionContent = expression.getContent();
        if (expressionContent == null) {
            throw new SInvalidExpressionException("The expression content cannot be null : " + expression, expression.getName());
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_XPATH_READ;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException, SExpressionDependencyMissingException {
        final List<Object> list = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(evaluate(expression, context, resolvedExpressions, containerState));
        }
        return list;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        // false because expression can't be referenced using it's content
        return false;
    }

}
