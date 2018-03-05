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
package org.bonitasoft.engine.expression;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("javadoc")
public class DataExpressionExecutorStrategyTest {

    @Mock
    private DataInstanceService dataService;

    @Mock
    private List<SExpression> expressionList;

    @Mock
    private SExpression expression;

    @Mock
    private ParentContainerResolver parentContainerResolver;

    @Mock
    private Iterator<SExpression> exprIterator;

    @InjectMocks
    private DataExpressionExecutorStrategy dataExpressionExecutorStrategy;

    @Test
    public void evaluateListOfEmptyDataExpressionDoesNotThrowException() throws SExpressionDependencyMissingException, SExpressionEvaluationException,
            SDataInstanceException {
        when(expressionList.size()).thenReturn(1);
        when(expressionList.iterator()).thenReturn(exprIterator);
        when(exprIterator.hasNext()).thenReturn(false);
        final HashMap<String, Object> dependencyValues = new HashMap<>(2);
        dependencyValues.put("containerId", 17L);
        dependencyValues.put("containerType", "process");
        dataExpressionExecutorStrategy.evaluate(expressionList, dependencyValues, new HashMap<Integer, Object>(0), ContainerState.ACTIVE);
    }

}
