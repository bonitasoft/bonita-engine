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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.SExpressionType;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Emmanuel Duchastenier
 */
public class XPathReadExpressionExecutorStrategyTest {

    private XPathReadExpressionExecutorStrategy strategy;

    private final String XML_CONTENT_BOOKS;

    private final String XML_CONTENT_AUTHOR;

    /**
     * @throws IOException
     */
    public XPathReadExpressionExecutorStrategyTest() throws IOException {
        XML_CONTENT_AUTHOR = new String(IOUtil.getAllContentFrom(this.getClass().getResourceAsStream("/authors.xml")));
        XML_CONTENT_BOOKS = new String(IOUtil.getAllContentFrom(this.getClass().getResourceAsStream("/books.xml")));

    }

    @Before
    public void setup() {
        strategy = new XPathReadExpressionExecutorStrategy();
    }

    private String getXPathType() {
        return SExpressionType.TYPE_XPATH_READ.name();
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.expression.impl.XPathReadExpressionExecutorStrategy#evaluate(org.bonitasoft.engine.expression.model.SExpression, java.util.Map, java.util.Map)}
     * 
     * @throws SExpressionException
     */
    @Test
    public void evaluateXpathReturnsStringAttr() throws SExpressionException {
        final String obj = evaluate(XML_CONTENT_AUTHOR, String.class, "//article/@nom");
        assertEquals("XPath", obj);
    }

    @Test
    public void evaluateXPathExpressionWithDataAsContent() throws SExpressionException {
        final String dataName = "myVariable";
        final SExpressionImpl dep = new SExpressionImpl(dataName, dataName, SExpressionType.TYPE_VARIABLE.name(), String.class.getName(), null, null);
        final String xPathSelector = "//article/@nom";
        final SExpression expression = new SExpressionImpl("expName1", xPathSelector, getXPathType(), String.class.getName(), null,
                Arrays.<SExpression> asList(dep));

        final Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>(1);
        resolvedExpressions.put(dep.getDiscriminant(), XML_CONTENT_AUTHOR);
        final HashMap<String, Object> dependencyValues = new HashMap<String, Object>(1);
        final Object result = strategy.evaluate(expression, dependencyValues, resolvedExpressions, ContainerState.ACTIVE);
        assertEquals("XPath", result);
    }

    @Test
    public void evaluateXpathReturnsStringNode() throws SExpressionException {
        final String obj = evaluate(XML_CONTENT_BOOKS, String.class, "//book[@id='bk101']/title");
        assertEquals("XML Developer's Guide", obj);
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluateXpathInvalidReturnType() throws SExpressionException {
        final SExpressionImpl dep = new SExpressionImpl(null, XML_CONTENT_AUTHOR, SExpressionType.TYPE_CONSTANT.name(), String.class.getName(), null, null);
        final SExpression expression = new SExpressionImpl("expName1", "//article", getXPathType(), "InvalidReturnType", null, Arrays.<SExpression> asList(dep));
        final Map<Integer, Object> resolvedExpressions = getResolvedExpressionMap(expression);
        final HashMap<String, Object> dependencyValues = new HashMap<String, Object>(1);
        strategy.evaluate(expression, dependencyValues, resolvedExpressions, ContainerState.ACTIVE);
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluateXpathWrongReturnType() throws SExpressionException {
        evaluate(XML_CONTENT_BOOKS, Integer.class, "//book[@id='bk101']/title");
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluateEnptyContent() throws Exception {
        try {
            evaluate("", Integer.class, "//book[@id='bk101']/title");
        } catch (final Exception e) {
            throw e;
        }
    }

    @Test
    public void evaluateXpathReturnsBoolean() throws SExpressionException {
        final Boolean ownedText = evaluate(XML_CONTENT_BOOKS, Boolean.class, "//catalog/book[@id='bk101']/owned");
        assertTrue(ownedText);
        final Boolean ownedNumeric = evaluate(XML_CONTENT_BOOKS, Boolean.class, "//catalog/book[@id='bk102']/owned");
        assertTrue(ownedNumeric);
        final Boolean notOwnedText = evaluate(XML_CONTENT_BOOKS, Boolean.class, "//catalog/book[@id='bk103']/owned");
        assertFalse(notOwnedText);
        final Boolean notOwnedNumeric = evaluate(XML_CONTENT_BOOKS, Boolean.class, "//catalog/book[@id='bk104']/owned");
        assertFalse(notOwnedNumeric);
    }

    @Test
    public void evaluateXpathReturnsLong() throws SExpressionException {
        final Long quantity = evaluate(XML_CONTENT_BOOKS, Long.class, "//catalog/book[@id='bk101']/quantity");
        assertEquals(123456789l, quantity.longValue());
    }

    @Test
    public void evaluateXpathReturnsInteger() throws SExpressionException {
        final Integer quantity = evaluate(XML_CONTENT_BOOKS, Integer.class, "//catalog/book[@id='bk101']/quantity");
        assertEquals(123456789, quantity.intValue());
    }

    @Test
    public void evaluateXpathReturnsFloat() throws SExpressionException {
        final Float quantity = evaluate(XML_CONTENT_BOOKS, Float.class, "//catalog/book[@id='bk101']/price");
        assertEquals(Float.valueOf(44.95f), quantity);
    }

    @Test
    public void evaluateXpathReturnsDouble() throws SExpressionException {
        final Double authorCount = evaluate(XML_CONTENT_BOOKS, Double.class, "//catalog/book[@id='bk101']/price");
        assertEquals(Double.valueOf(44.95d), authorCount);
    }

    @SuppressWarnings("unchecked")
    private <T> T evaluate(final String content, final Class<T> returnType, final String selector) throws SExpressionEvaluationException,
            SExpressionDependencyMissingException {
        final SExpressionImpl dep = new SExpressionImpl(null, content, SExpressionType.TYPE_CONSTANT.name(), String.class.getName(), null, null);
        final SExpression expression = new SExpressionImpl("expName1", selector, getXPathType(), returnType.getName(), null, Arrays.<SExpression> asList(dep));
        final Map<Integer, Object> resolvedExpressions = getResolvedExpressionMap(expression);
        final HashMap<String, Object> dependencyValues = new HashMap<String, Object>(1);
        final Object result = strategy.evaluate(expression, dependencyValues, resolvedExpressions, ContainerState.ACTIVE);
        return (T) result;
    }

    @Test
    public void evaluateXpathReturnsNode() throws SExpressionException {
        final Node obj = evaluate(XML_CONTENT_AUTHOR, Node.class, "//article");
        assertEquals("article", obj.getNodeName());
    }

    @Test
    public void evaluateXpathReturnsNodeList() throws SExpressionException {
        final NodeList nodeList = evaluate(XML_CONTENT_AUTHOR, NodeList.class, "//article/auteurs/auteur/nom");
        assertEquals(2, nodeList.getLength());
        assertEquals("Dupont", nodeList.item(0).getTextContent());
        assertEquals("Dubois", nodeList.item(1).getTextContent());
    }

    @Test(expected = SExpressionDependencyMissingException.class)
    public void evaluateInvalidDependencies() throws SExpressionException {
        final SExpression expression = new SExpressionImpl("expName1", "//article/@nom", getXPathType(), String.class.getName(), null, null);
        final Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>(0);
        final HashMap<String, Object> dependencyValues = new HashMap<String, Object>(0);
        strategy.evaluate(expression, dependencyValues, resolvedExpressions, ContainerState.ACTIVE);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.expression.impl.XPathReadExpressionExecutorStrategy#validate(SExpression)}.
     */
    @Test(expected = SInvalidExpressionException.class)
    public void validateNullExpressionContent() throws Exception {
        final SExpressionImpl dep = new SExpressionImpl(null, XML_CONTENT_AUTHOR, SExpressionType.TYPE_CONSTANT.name(), String.class.getName(), null, null);
        final SExpression expression = new SExpressionImpl("expName1", null, getXPathType(), Double.class.getName(), null, Arrays.<SExpression> asList(dep));
        strategy.validate(expression);
    }

    private Map<Integer, Object> getResolvedExpressionMap(final SExpression... expressions) {
        final HashMap<Integer, Object> hashMap = new HashMap<Integer, Object>();
        for (final SExpression expression : expressions) {
            final List<SExpression> dependencies = expression.getDependencies();
            for (final SExpression dependency : dependencies) {
                hashMap.put(dependency.getDiscriminant(), dependency.getContent());
            }
        }
        return hashMap;
    }

}
