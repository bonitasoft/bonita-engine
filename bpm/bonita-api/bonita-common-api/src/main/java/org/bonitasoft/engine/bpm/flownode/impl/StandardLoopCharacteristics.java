/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.flownode.impl;

import org.bonitasoft.engine.bpm.flownode.LoopCharacteristics;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Matthieu Chaffotte
 */
public class StandardLoopCharacteristics implements LoopCharacteristics {

    private static final long serialVersionUID = -8405419721405699090L;

    private final Expression loopCondition;

    private final boolean testBefore;

    private final Expression loopMax;

    public StandardLoopCharacteristics(final Expression loopCondition, final boolean testBefore) {
        super();
        this.loopCondition = loopCondition;
        this.testBefore = testBefore;
        loopMax = null;
    }

    public StandardLoopCharacteristics(final Expression loopCondition, final boolean testBefore, final Expression loopMax) {
        super();
        this.loopCondition = loopCondition;
        this.testBefore = testBefore;
        this.loopMax = loopMax;
    }

    public Expression getLoopCondition() {
        return loopCondition;
    }

    public boolean isTestBefore() {
        return testBefore;
    }

    public Expression getLoopMax() {
        return loopMax;
    }

}
