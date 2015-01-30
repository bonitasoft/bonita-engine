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

import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.SMultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.core.process.definition.model.builder.ServerModelConvertor;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 */
public class SMultiInstanceLoopCharacteristicsImpl implements SMultiInstanceLoopCharacteristics {

    private static final long serialVersionUID = 5900494662430961903L;

    private boolean isSequential;

    private SExpression loopCardinality;

    private SExpression completionCondition;

    private String loopDataInputRef;

    private String loopDataOutputRef;

    private String dataInputItemRef;

    private String dataOutputItemRef;

    public SMultiInstanceLoopCharacteristicsImpl() {
    }

    public SMultiInstanceLoopCharacteristicsImpl(final MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics) {
        isSequential = multiInstanceLoopCharacteristics.isSequential();
        loopDataInputRef = multiInstanceLoopCharacteristics.getLoopDataInputRef();
        loopDataOutputRef = multiInstanceLoopCharacteristics.getLoopDataOutputRef();
        dataInputItemRef = multiInstanceLoopCharacteristics.getDataInputItemRef();
        dataOutputItemRef = multiInstanceLoopCharacteristics.getDataOutputItemRef();
        loopCardinality = ServerModelConvertor.convertExpression(multiInstanceLoopCharacteristics.getLoopCardinality());
        completionCondition = ServerModelConvertor.convertExpression(multiInstanceLoopCharacteristics.getCompletionCondition());
    }

    @Override
    public boolean isSequential() {
        return isSequential;
    }

    @Override
    public SExpression getLoopCardinality() {
        return loopCardinality;
    }

    @Override
    public SExpression getCompletionCondition() {
        return completionCondition;
    }

    @Override
    public String getLoopDataInputRef() {
        return loopDataInputRef;
    }

    @Override
    public String getLoopDataOutputRef() {
        return loopDataOutputRef;
    }

    @Override
    public String getDataInputItemRef() {
        return dataInputItemRef;
    }

    @Override
    public String getDataOutputItemRef() {
        return dataOutputItemRef;
    }

    public void setSequential(final boolean isSequential) {
        this.isSequential = isSequential;
    }

    public void setLoopCardinality(final SExpression loopCardinality) {
        this.loopCardinality = loopCardinality;
    }

    public void setCompletionCondition(final SExpression completionCondition) {
        this.completionCondition = completionCondition;
    }

    public void setLoopDataInputRef(final String loopDataInputRef) {
        this.loopDataInputRef = loopDataInputRef;
    }

    public void setLoopDataOutputRef(final String loopDataOutputRef) {
        this.loopDataOutputRef = loopDataOutputRef;
    }

    public void setDataInputItemRef(final String dataInputItemRef) {
        this.dataInputItemRef = dataInputItemRef;
    }

    public void setDataOutputItemRef(final String dataOutputItemRef) {
        this.dataOutputItemRef = dataOutputItemRef;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (completionCondition == null ? 0 : completionCondition.hashCode());
        result = prime * result + (dataInputItemRef == null ? 0 : dataInputItemRef.hashCode());
        result = prime * result + (dataOutputItemRef == null ? 0 : dataOutputItemRef.hashCode());
        result = prime * result + (isSequential ? 1231 : 1237);
        result = prime * result + (loopCardinality == null ? 0 : loopCardinality.hashCode());
        result = prime * result + (loopDataInputRef == null ? 0 : loopDataInputRef.hashCode());
        result = prime * result + (loopDataOutputRef == null ? 0 : loopDataOutputRef.hashCode());
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
        final SMultiInstanceLoopCharacteristicsImpl other = (SMultiInstanceLoopCharacteristicsImpl) obj;
        if (completionCondition == null) {
            if (other.completionCondition != null) {
                return false;
            }
        } else if (!completionCondition.equals(other.completionCondition)) {
            return false;
        }
        if (dataInputItemRef == null) {
            if (other.dataInputItemRef != null) {
                return false;
            }
        } else if (!dataInputItemRef.equals(other.dataInputItemRef)) {
            return false;
        }
        if (dataOutputItemRef == null) {
            if (other.dataOutputItemRef != null) {
                return false;
            }
        } else if (!dataOutputItemRef.equals(other.dataOutputItemRef)) {
            return false;
        }
        if (isSequential != other.isSequential) {
            return false;
        }
        if (loopCardinality == null) {
            if (other.loopCardinality != null) {
                return false;
            }
        } else if (!loopCardinality.equals(other.loopCardinality)) {
            return false;
        }
        if (loopDataInputRef == null) {
            if (other.loopDataInputRef != null) {
                return false;
            }
        } else if (!loopDataInputRef.equals(other.loopDataInputRef)) {
            return false;
        }
        if (loopDataOutputRef == null) {
            if (other.loopDataOutputRef != null) {
                return false;
            }
        } else if (!loopDataOutputRef.equals(other.loopDataOutputRef)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("SMultiInstanceLoopCharacteristicsImpl [isSequential=");
        builder.append(isSequential);
        builder.append(", loopCardinality=");
        builder.append(loopCardinality);
        builder.append(", completionCondition=");
        builder.append(completionCondition);
        builder.append(", loopDataInputRef=");
        builder.append(loopDataInputRef);
        builder.append(", loopDataOutputRef=");
        builder.append(loopDataOutputRef);
        builder.append(", dataInputItemRef=");
        builder.append(dataInputItemRef);
        builder.append(", dataOutputItemRef=");
        builder.append(dataOutputItemRef);
        builder.append("]");
        return builder.toString();
    }

}
