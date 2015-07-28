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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import org.bonitasoft.engine.bpm.flownode.StandardLoopCharacteristics;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardLoopCharacteristicsImpl implements StandardLoopCharacteristics {

    private static final long serialVersionUID = -8405419721405699090L;
    @XmlElement(type = ExpressionImpl.class)
    private final Expression loopCondition;
    @XmlAttribute
    private final boolean testBefore;
    @XmlElement(type = ExpressionImpl.class)
    private final Expression loopMax;

    public StandardLoopCharacteristicsImpl(final Expression loopCondition, final boolean testBefore) {
        super();
        this.loopCondition = loopCondition;
        this.testBefore = testBefore;
        loopMax = null;
    }

    public StandardLoopCharacteristicsImpl(final Expression loopCondition, final boolean testBefore, final Expression loopMax) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (testBefore ? 1231 : 1237);
        result = prime * result + (loopCondition == null ? 0 : loopCondition.hashCode());
        result = prime * result + (loopMax == null ? 0 : loopMax.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StandardLoopCharacteristicsImpl other = (StandardLoopCharacteristicsImpl) obj;
        if (testBefore != other.testBefore) {
            return false;
        }
        if (loopCondition == null) {
            if (other.loopCondition != null) {
                return false;
            }
        } else if (!loopCondition.equals(other.loopCondition)) {
            return false;
        }
        if (loopMax == null) {
            if (other.loopMax != null) {
                return false;
            }
        } else if (!loopMax.equals(other.loopMax)) {
            return false;
        }
        return true;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

}
