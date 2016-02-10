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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.NonUniqueResultException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryBusinessDataExpressionExecutorStrategyTest {

    @Mock
    private BusinessDataRepository businessDataRepository;

    @InjectMocks
    private QueryBusinessDataExpressionExecutorStrategy strategy;

    @Mock
    private Entity entity;

    private Map<String, Object> buildContext() {
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("containerId", 784654l);
        context.put("containerType", "activity");
        return context;
    }

    @Test
    public void evaluation_result_should_not__be_pushed_in_context() {
        assertThat(strategy.mustPutEvaluatedExpressionInContext()).isFalse();
    }

    @Test
    public void evaluate_should_return_a_long_result_after_querying() throws Exception {
        final SExpressionImpl expression = new SExpressionImpl("employees", "countEmployees", null, Long.class.getName(), null, null);
        final Long result = Long.valueOf(45);
        when(businessDataRepository.findByNamedQuery("countEmployees", Long.class, Collections.<String, Serializable> emptyMap())).thenReturn(result);

        final Long count = (Long) strategy.evaluate(expression, buildContext(), null, ContainerState.ACTIVE);

        assertThat(count).isEqualTo(result);
    }

    @Test
    public void evaluate_should_return_a_double_result_after_querying() throws Exception {
        final SExpressionImpl expression = new SExpressionImpl("employees", "avgEmployees", null, Double.class.getName(), null, null);
        final Double result = Double.valueOf(2.5);
        when(businessDataRepository.findByNamedQuery("avgEmployees", Double.class, Collections.<String, Serializable> emptyMap())).thenReturn(result);

        final Double avg = (Double) strategy.evaluate(expression, buildContext(), null, ContainerState.ACTIVE);

        assertThat(avg).isEqualTo(result);
    }

    @Test
    public void evaluate_should_return_a_integer_result_after_querying() throws Exception {
        final SExpressionImpl expression = new SExpressionImpl("employees", "maxEmployees", null, Integer.class.getName(), null, null);
        final Integer result = Integer.valueOf(25);
        when(businessDataRepository.findByNamedQuery("maxEmployees", Integer.class, Collections.<String, Serializable> emptyMap())).thenReturn(result);

        final Integer max = (Integer) strategy.evaluate(expression, buildContext(), null, ContainerState.ACTIVE);

        assertThat(max).isEqualTo(result);
    }

    @Test
    public void evaluate_should_return_a_float_result_after_querying() throws Exception {
        final SExpressionImpl expression = new SExpressionImpl("employees", "minEmployees", null, Float.class.getName(), null, null);
        final Float result = Float.valueOf(1.5F);
        when(businessDataRepository.findByNamedQuery("minEmployees", Float.class, Collections.<String, Serializable> emptyMap())).thenReturn(result);

        final Float min = (Float) strategy.evaluate(expression, buildContext(), null, ContainerState.ACTIVE);

        assertThat(min).isEqualTo(result);
    }

    @Test
    public void evaluate_should_return_the_list_of_results_after_querying() throws Exception {
        final SExpression dependencyStartIndex = new SExpressionImpl("startIndex", "startIndex", null, Integer.class.getName(), null, null);
        final SExpression dependencyMaxResults = new SExpressionImpl("maxResults", "startIndex", null, Integer.class.getName(), null, null);
        final SExpressionImpl expression = new SExpressionImpl("employees", "getEmployees", null, List.class.getName(), null, Arrays.asList(
                dependencyStartIndex, dependencyMaxResults));

        final Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>(2);
        resolvedExpressions.put(dependencyStartIndex.getDiscriminant(), 0);
        resolvedExpressions.put(dependencyMaxResults.getDiscriminant(), 10);
        strategy.evaluate(expression, buildContext(), resolvedExpressions, ContainerState.ACTIVE);

        verify(businessDataRepository).findListByNamedQuery("getEmployees", Entity.class, Collections.<String, Serializable> emptyMap(), 0, 10);
    }

    @Test
    public void evaluate_should_return_a_result_after_querying_with_parameters() throws Exception {

        final SExpressionImpl dependencyFirstNameExpression = new SExpressionImpl("firstName", "John", null, String.class.getName(), null, null);
        final SExpressionImpl dependencyLastNameExpression = new SExpressionImpl("lastName", "Doe", null, String.class.getName(), null, null);
        final List<SExpression> dependencies = new ArrayList<SExpression>();
        dependencies.add(dependencyFirstNameExpression);
        dependencies.add(dependencyLastNameExpression);
        final SExpressionImpl expression = new SExpressionImpl("employees", "getEmployeeByFirstNameAndLastName", "", "com.bonitasoft.Employee", "",
                dependencies);
        final Map<Integer, Object> resolvedExpressions = new HashMap<Integer, Object>();
        resolvedExpressions.put(dependencyFirstNameExpression.getDiscriminant(), "John");
        resolvedExpressions.put(dependencyLastNameExpression.getDiscriminant(), "Doe");
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("firstName", "John");
        parameters.put("lastName", "Doe");
        when(businessDataRepository.findByNamedQuery("getEmployeeByFirstNameAndLastName", Entity.class, parameters)).thenReturn(entity);

        strategy.evaluate(expression, buildContext(), resolvedExpressions, ContainerState.ACTIVE);

        verify(businessDataRepository).findByNamedQuery("getEmployeeByFirstNameAndLastName", Entity.class, parameters);
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluate_should_throw_an_exception_after_querying() throws Exception {
        final SExpressionImpl expression = new SExpressionImpl("employees", "getEmployees", null, Entity.class.getName(), null, null);
        when(businessDataRepository.findByNamedQuery("getEmployees", Entity.class, Collections.<String, Serializable> emptyMap())).thenThrow(
                new NonUniqueResultException(new IllegalArgumentException("several")));

        strategy.evaluate(expression, buildContext(), null, ContainerState.ACTIVE);
    }

}
