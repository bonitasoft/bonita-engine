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
package org.bonitasoft.engine.core.process.definition.model.impl;

import org.bonitasoft.engine.bpm.flownode.impl.internal.StandardLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SStandardLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Matthieu Chaffotte
 */
public class SStandardLoopCharacteristicsImpl implements SStandardLoopCharacteristics {

    private static final long serialVersionUID = 2762060718511545677L;

    private final SExpression loopCondition;

    private final boolean testBefore;

    private final SExpression loopMax;

    public SStandardLoopCharacteristicsImpl(final SExpression loopCondition, final boolean testBefore) {
        super();
        this.loopCondition = loopCondition;
        this.testBefore = testBefore;
        loopMax = null;
    }

    public SStandardLoopCharacteristicsImpl(final SExpression loopCondition, final boolean testBefore, final SExpression loopMax) {
        super();
        this.loopCondition = loopCondition;
        this.testBefore = testBefore;
        this.loopMax = loopMax;
    }

    public SStandardLoopCharacteristicsImpl(final StandardLoopCharacteristics loopCharacteristics) {
        super();
        testBefore = loopCharacteristics.isTestBefore();
        final Expression loopMaxExpression = loopCharacteristics.getLoopMax();
        loopMax = ServerModelConvertor.convertExpression(loopMaxExpression);
        final Expression condition = loopCharacteristics.getLoopCondition();
        loopCondition = ServerModelConvertor.convertExpression(condition);

    }

    @Override
    public SExpression getLoopCondition() {
        return loopCondition;
    }

    @Override
    public boolean isTestBefore() {
        return testBefore;
    }

    @Override
    public SExpression getLoopMax() {
        return loopMax;
    }

}
