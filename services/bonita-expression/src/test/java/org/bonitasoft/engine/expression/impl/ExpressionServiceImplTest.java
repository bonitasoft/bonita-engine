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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategyProvider;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("javadoc")
public class ExpressionServiceImplTest {

    @Mock
    private ExpressionExecutorStrategyProvider expressionExecutorStrategyProvider;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private ExpressionExecutorStrategy expressionExecutorStrategy;

    private ExpressionServiceImpl expressionService;

    @Before
    public void setUp() {
        when(logger.isLoggable(any(Class.class), any(TechnicalLogSeverity.class))).thenReturn(false);
        when(expressionExecutorStrategyProvider.getExpressionExecutors()).thenReturn(Arrays.asList(expressionExecutorStrategy));
    }

    @Test
    public void evaluateInvalidExpressionFailsAtValidationStep() throws Exception {
        final SExpression expression = mock(SExpression.class);
        final TimeTracker timeTracker = mock(TimeTracker.class);
        expressionService = new ExpressionServiceImpl(expressionExecutorStrategyProvider, logger, true, timeTracker);
        expressionService.evaluate(expression, Collections.<String,Object>singletonMap("processDefinitionId", 546l), new HashMap<Integer, Object>(0), ContainerState.ACTIVE);
        verify(expressionExecutorStrategy, times(1)).validate(any(SExpression.class));
    }
}
