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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.engine.bpm.flownode.impl.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.bpm.userfilter.impl.UserFilterDefinitionImpl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Objects;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class HumanTaskDefinitionImpl extends TaskDefinitionImpl implements HumanTaskDefinition {

    private static final long serialVersionUID = -7657152341382296289L;
    @XmlAttribute
    private final String actorName;
    @XmlElement(type = UserFilterDefinitionImpl.class,name = "userFilter")
    private UserFilterDefinition userFilterDefinition;
    @XmlAttribute
    private Long expectedDuration;
    @XmlAttribute
    private String priority;

    public HumanTaskDefinitionImpl(){
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
    public String getActorName() {
        return actorName;
    }

    @Override
    public UserFilterDefinition getUserFilter() {
        return userFilterDefinition;
    }

    @Override
    public void setUserFilter(final UserFilterDefinition userFilterDefinition) {
        this.userFilterDefinition = userFilterDefinition;
    }

    public void setExpectedDuration(final long expectedDuration) {
        this.expectedDuration = expectedDuration;
    }

    public void setPriority(final String priority) {
        this.priority = priority;
    }

    @Override
    public Long getExpectedDuration() {
        return expectedDuration;
    }

    @Override
    public String getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HumanTaskDefinitionImpl that = (HumanTaskDefinitionImpl) o;
        return Objects.equals(actorName, that.actorName) &&
                Objects.equals(userFilterDefinition, that.userFilterDefinition) &&
                Objects.equals(expectedDuration, that.expectedDuration) &&
                Objects.equals(priority, that.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), actorName, userFilterDefinition, expectedDuration, priority);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("actorName", actorName)
                .append("userFilterDefinition", userFilterDefinition)
                .append("expectedDuration", expectedDuration)
                .append("priority", priority)
                .toString();
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        super.accept(visitor, modelId);
        visitor.find(this, modelId);
    }
}
