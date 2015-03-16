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
package org.bonitasoft.engine.execution.transition;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;

public class InclusiveGatewayTransitionEvaluationStrategyTest {

    private InclusiveGatewayTransitionEvaluationStrategy strategy;

    @Before
    public void setUp() throws Exception {
        strategy = new InclusiveGatewayTransitionEvaluationStrategy();
    }

    @Test
    public void shouldContinue_should_return_true_when_not_found() throws Exception {
        //when
        boolean shouldContinue = strategy.shouldContinue(false);

        //then
        assertThat(shouldContinue).isTrue();
    }

    @Test
    public void shouldContinue_should_return_true_when_found() throws Exception {
        //when
        boolean shouldContinue = strategy.shouldContinue(true);

        //then
        assertThat(shouldContinue).isTrue();
    }

}
