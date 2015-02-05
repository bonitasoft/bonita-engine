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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.SExpressionType;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class PatternExpressionExecutorStrategyTest {

    private PatternExpressionExecutorStrategy strategy;

    @Before
    public void setup() {
        strategy = new PatternExpressionExecutorStrategy();
    }

    private void patternTest(final String expressionContent, final String result, final List<String> dependencyNames, final List<Integer> dependencyContent)
            throws SExpressionDependencyMissingException {
        final SExpressionImpl expression = new SExpressionImpl("pattern", expressionContent, SExpressionType.TYPE_PATTERN.name(), String.class.getName(), null,
                getIntegerExpressions(dependencyNames, dependencyContent));
        final Map<Integer, Object> resolvedExpressions = getResolvedExpressionMap(expression);
        final HashMap<String, Object> dependencyValues = new HashMap<String, Object>(1);
        assertEquals(result, strategy.evaluate(expression, dependencyValues, resolvedExpressions, ContainerState.ACTIVE));
    }

    @Test
    public void patternTestWithMultipleExpressions() throws SExpressionDependencyMissingException {
        final SExpression expression1 = new SExpressionImpl("pattern1", "${bla} ${bla} test", SExpressionType.TYPE_PATTERN.name(), String.class.getName(),
                null, getIntegerExpressions(Arrays.asList("bla"), Arrays.asList(12)));
        final SExpression expression2 = new SExpressionImpl("pattern1", "${bla} ${bli} test", SExpressionType.TYPE_PATTERN.name(), String.class.getName(),
                null, getIntegerExpressions(Arrays.asList("bla", "bli"), Arrays.asList(12, 13)));
        final Map<Integer, Object> resolvedExpressions = getResolvedExpressionMap(expression1, expression2);
        final HashMap<String, Object> dependencyValues = new HashMap<String, Object>(1);
        final List<Object> evaluate = strategy.evaluate(Arrays.asList(expression1, expression2), dependencyValues, resolvedExpressions, ContainerState.ACTIVE);
        assertArrayEquals(new Object[] { "12 12 test", "12 13 test" }, evaluate.toArray());
    }

    @Test(expected = SExpressionDependencyMissingException.class)
    public void patternTestWithMissingValue() throws SExpressionDependencyMissingException {
        final SExpression expression1 = new SExpressionImpl("pattern1", "${bla} ${bla} test", SExpressionType.TYPE_PATTERN.name(), String.class.getName(),
                null, getIntegerExpressions(Arrays.asList("bla"), Arrays.asList(12)));
        final Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>(1);
        final HashMap<String, Object> dependencyValues = new HashMap<String, Object>(1);
        strategy.evaluate(Arrays.asList(expression1), dependencyValues, resolvedExpressions, ContainerState.ACTIVE);
    }

    /**
     * @param expression
     * @return
     */
    private Map<Integer, Object> getResolvedExpressionMap(final SExpression... expressions) {
        final HashMap<Integer, Object> hashMap = new HashMap<Integer, Object>();
        for (final SExpression expression : expressions) {
            final List<SExpression> dependencies = expression.getDependencies();
            for (final SExpression sExpression : dependencies) {
                hashMap.put(sExpression.getDiscriminant(), Integer.valueOf(sExpression.getContent()));
            }
        }
        return hashMap;
    }

    /**
     * @param dependencyNames
     * @param dependencyContent
     * @return
     */
    private List<SExpression> getIntegerExpressions(final List<String> dependencyNames, final List<Integer> dependencyContent) {
        final ArrayList<SExpression> arrayList = new ArrayList<SExpression>(dependencyNames.size());
        final Iterator<Integer> contentIterator = dependencyContent.iterator();
        for (final Iterator<String> nameIterator = dependencyNames.iterator(); nameIterator.hasNext();) {
            arrayList.add(getIntegerExpression(nameIterator.next(), contentIterator.next()));
        }
        return arrayList;
    }

    private SExpression getIntegerExpression(final String name, final Integer content) {
        return new SExpressionImpl(name, String.valueOf(content), SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
    }

    @Test
    public void testPatternWithEscape() throws Exception {
        patternTest("ahah bla ${bla}", "ahah bla 12", Arrays.asList("bla"), Arrays.asList(12));
    }

    @Test
    public void testPatternWithNotInDependencies() throws Exception {
        patternTest("ahah bla bla", "ahah bla bla", Arrays.asList("blo"), Arrays.asList(12));
    }

    @Test
    public void testPatternWithSpaces() throws Exception {
        patternTest("${bla} ${blablabla}  ${bla} ${bla}a bla${bla} bla", "12 ${blablabla}  12 12a bla12 bla", Arrays.asList("bla"), Arrays.asList(12));
    }

    @Test
    public void testPatternWithSpacesAndLineBreak() throws Exception {
        patternTest("${bla} ${bla}${bla}${bla}  ${bla} ${bla}a ${bla}\n${bla} ${bla}", "12 121212  12 12a 12\n12 12", Arrays.asList("bla"), Arrays.asList(12));
    }

    @Test
    public void testPatternMultipleReplacements() throws Exception {
        patternTest("${bla} ${blo} ${blu}${bla}${bla}", "1 2 311", Arrays.asList("bla", "blo", "blu"), Arrays.asList(1, 2, 3));
    }

}
