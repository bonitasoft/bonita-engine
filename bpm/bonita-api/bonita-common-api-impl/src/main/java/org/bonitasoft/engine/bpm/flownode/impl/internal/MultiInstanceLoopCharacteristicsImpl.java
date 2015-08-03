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

import org.bonitasoft.engine.bpm.flownode.LoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Baptiste Mesta
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiInstanceLoopCharacteristicsImpl implements MultiInstanceLoopCharacteristics, LoopCharacteristics {

    private static final long serialVersionUID = 22281767220832906L;
    @XmlAttribute
    private boolean isSequential;
    @XmlElement(type = ExpressionImpl.class)
    private Expression loopCardinality;
    @XmlElement(type = ExpressionImpl.class)
    private Expression completionCondition;
    @XmlAttribute
    private String loopDataInputRef;
    @XmlAttribute
    private String loopDataOutputRef;
    @XmlAttribute
    private String dataInputItemRef;
    @XmlAttribute
    private String dataOutputItemRef;

    public MultiInstanceLoopCharacteristicsImpl(final boolean isSequential, final Expression loopCardinality) {
        this.isSequential = isSequential;
        this.loopCardinality = loopCardinality;
    }

    public MultiInstanceLoopCharacteristicsImpl(final boolean isSequential, final String loopDataInputRef) {
        this.isSequential = isSequential;
        this.loopDataInputRef = loopDataInputRef;
    }

    public MultiInstanceLoopCharacteristicsImpl() {
    }

    public boolean isSequential() {
        return isSequential;
    }

    public void setSequential(final boolean isSequential) {
        this.isSequential = isSequential;
    }

    public Expression getLoopCardinality() {
        return loopCardinality;
    }

    public void setLoopCardinality(final Expression loopCardinality) {
        this.loopCardinality = loopCardinality;
    }

    public Expression getCompletionCondition() {
        return completionCondition;
    }

    public void setCompletionCondition(final Expression completionCondition) {
        this.completionCondition = completionCondition;
    }

    public String getLoopDataInputRef() {
        return loopDataInputRef;
    }

    public void setLoopDataInputRef(final String loopDataInputRef) {
        this.loopDataInputRef = loopDataInputRef;
    }

    public String getLoopDataOutputRef() {
        return loopDataOutputRef;
    }

    public void setLoopDataOutputRef(final String loopDataOutputRef) {
        this.loopDataOutputRef = loopDataOutputRef;
    }

    public String getDataInputItemRef() {
        return dataInputItemRef;
    }

    public void setDataInputItemRef(final String dataInputItemRef) {
        this.dataInputItemRef = dataInputItemRef;
    }

    public String getDataOutputItemRef() {
        return dataOutputItemRef;
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
        final MultiInstanceLoopCharacteristicsImpl other = (MultiInstanceLoopCharacteristicsImpl) obj;
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
        builder.append("MultiInstanceLoopCharacteristics [isSequential=");
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

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

}
