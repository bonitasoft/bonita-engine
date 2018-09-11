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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class ConstantExpressionExecutorStrategyTest {

    private ConstantExpressionExecutorStrategy strategy;

    @Before
    public void setup() {
        strategy = new ConstantExpressionExecutorStrategy();
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.expression.impl.ConstantExpressionExecutorStrategy#evaluate(org.bonitasoft.engine.expression.model.SExpression, java.util.Map, java.util.Map)}
     * .
     * 
     * @throws SExpressionEvaluationException
     */
    @Test
    public final void evaluateDate() throws SExpressionEvaluationException {
        final SExpression sExpression = buildExpression("2013-07-18T14:49:26.86+02:00", SExpression.TYPE_CONSTANT, Date.class.getName(), null, null);

        final Date result = (Date) strategy.evaluate(sExpression, null, null, ContainerState.ACTIVE);
        assertNotNull(result);
    }

    @Test
    public final void evaluateDateWithoutTimeZone() throws SExpressionEvaluationException {
        final SExpression sExpression = buildExpression("2013-07-18T14:49:26.86", SExpression.TYPE_CONSTANT, Date.class.getName(), null, null);

        final Date result = (Date) strategy.evaluate(sExpression, null, null, ContainerState.ACTIVE);
        assertNotNull(result);
    }

    @Test
    public final void evaluateDateWithoutMilliseconds() throws SExpressionEvaluationException {
        final SExpression sExpression = buildExpression("2013-07-18T14:49:26+02:00", SExpression.TYPE_CONSTANT, Date.class.getName(), null, null);

        final Date result = (Date) strategy.evaluate(sExpression, null, null, ContainerState.ACTIVE);
        assertNotNull(result);
    }

    @Test
    public void should_evaluate_local_date() throws Exception {
        final SExpression sExpression = buildExpression("2013-07-18", SExpression.TYPE_CONSTANT,
                LocalDate.class.getName(), null, null);

        final LocalDate result = (LocalDate) strategy.evaluate(sExpression, null, null, ContainerState.ACTIVE);
        assertThat(result.getYear()).isEqualTo(2013);
        assertThat(result.getMonth()).isEqualTo(Month.JULY);
        assertThat(result.getDayOfMonth()).isEqualTo(18);
    }

    @Test
    public void should_evaluate_local_date_time() throws Exception {
        final SExpression sExpression = buildExpression("2013-07-18T12:00:00", SExpression.TYPE_CONSTANT,
                LocalDateTime.class.getName(), null, null);

        final LocalDateTime result = (LocalDateTime) strategy.evaluate(sExpression, null, null, ContainerState.ACTIVE);
        assertThat(result.getYear()).isEqualTo(2013);
        assertThat(result.getMonth()).isEqualTo(Month.JULY);
        assertThat(result.getDayOfMonth()).isEqualTo(18);
        assertThat(result.getHour()).isEqualTo(12);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getSecond()).isEqualTo(0);
    }

    @Test
    public void should_evaluate_offset_date_time() throws Exception {
        final SExpression sExpression = buildExpression("2007-12-03T10:15:30+01:00", SExpression.TYPE_CONSTANT,
                OffsetDateTime.class.getName(), null, null);

        final OffsetDateTime result = (OffsetDateTime) strategy.evaluate(sExpression, null, null,
                ContainerState.ACTIVE);
        assertThat(result.getYear()).isEqualTo(2007);
        assertThat(result.getMonth()).isEqualTo(Month.DECEMBER);
        assertThat(result.getDayOfMonth()).isEqualTo(3);
        assertThat(result.getHour()).isEqualTo(10);
        assertThat(result.getMinute()).isEqualTo(15);
        assertThat(result.getSecond()).isEqualTo(30);
        assertThat(result.getOffset()).isEqualTo(ZoneOffset.ofHours(1));
    }

    private SExpression buildExpression(final String content, final String expressionType, final String returnType, final String interpreter,
            final List<SExpression> dependencies) {
        final SExpressionImpl eb = new SExpressionImpl();
        eb.setName(content);
        eb.setContent(content);
        eb.setExpressionType(expressionType);
        eb.setInterpreter(interpreter);
        eb.setReturnType(returnType);
        eb.setDependencies(dependencies);
        return eb;
    }

}
