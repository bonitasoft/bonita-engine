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
package org.bonitasoft.engine.core.expression.control.api.impl;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.expression.ExpressionService;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExpressionResolverServiceImplTest {

    @Mock
    private ExpressionService expressionService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    private TimeTracker timeTracker;

    @InjectMocks
    private ExpressionResolverServiceImpl resolverService;

    @Mock
    private SExpression expression;

    @Test
    public void evaluate_should_load_class_loader_of_process_definition() throws Exception {
        final long processDefinitionId = 83L;
        final SExpressionContext context = new SExpressionContext(45L, "PROCESS", processDefinitionId);

        resolverService.evaluate(expression, context);

        verify(classLoaderService).getLocalClassLoader("PROCESS", processDefinitionId);
    }

    @Test
    public void evaluate_should_load_class_loader_of_parent_process_definition() throws Exception {
        final long parentProcessDefinitionId = 19L;
        final long processDefinitionId = 83L;
        final SExpressionContext context = new SExpressionContext(45L, "PROCESS", processDefinitionId);
        context.setParentProcessDefinitionId(parentProcessDefinitionId);

        resolverService.evaluate(expression, context);

        verify(classLoaderService).getLocalClassLoader("PROCESS", parentProcessDefinitionId);
    }

    @Test
    public void evaluate_should_not_load_class_loader_when_no_defintion_is_defined() throws Exception {
        final SExpressionContext context = new SExpressionContext();

        resolverService.evaluate(expression, context);

        verify(classLoaderService, never()).getLocalClassLoader(anyString(), anyLong());
    }

}
