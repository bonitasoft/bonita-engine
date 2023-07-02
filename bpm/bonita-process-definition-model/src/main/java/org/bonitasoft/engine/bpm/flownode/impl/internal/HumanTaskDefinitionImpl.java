/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bonitasoft.engine.bpm.flownode.impl.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.bpm.userfilter.impl.UserFilterDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@EqualsAndHashCode(callSuper = true)
@ToString
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class HumanTaskDefinitionImpl extends TaskDefinitionImpl implements HumanTaskDefinition {

    private static final long serialVersionUID = -7657152341382296289L;

    @Getter
    @XmlAttribute
    private final String actorName;

    @XmlElement(type = UserFilterDefinitionImpl.class, name = "userFilter")
    private UserFilterDefinition userFilterDefinition;

    @Getter
    @Setter
    @XmlElement(type = ExpressionImpl.class, name = "expectedDuration")
    private Expression expectedDuration;

    @Getter
    @Setter
    @XmlAttribute
    private String priority;

    public HumanTaskDefinitionImpl() {
        actorName = "default actor name";
    }

    public HumanTaskDefinitionImpl(final String name, final String actorName) {
        super(name);
        this.actorName = actorName;
    }

    public HumanTaskDefinitionImpl(final long id, final String name, final String actorName) {
        super(id, name);
        this.actorName = actorName;
    }

    @Override
    public UserFilterDefinition getUserFilter() {
        return userFilterDefinition;
    }

    @Override
    public void setUserFilter(final UserFilterDefinition userFilterDefinition) {
        this.userFilterDefinition = userFilterDefinition;
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
