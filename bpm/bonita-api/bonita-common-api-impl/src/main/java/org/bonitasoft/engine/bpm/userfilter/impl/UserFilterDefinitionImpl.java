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
package org.bonitasoft.engine.bpm.userfilter.impl;

import org.bonitasoft.engine.bpm.flownode.impl.internal.MapAdapterExpression;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.expression.Expression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * @author Baptiste Mesta
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserFilterDefinitionImpl extends NamedElementImpl implements UserFilterDefinition {

    private static final long serialVersionUID = -6045216424839658552L;
@XmlAttribute(name = "userFilterId")
    private final String filterId;
@XmlAttribute
    private final String version;
@XmlJavaTypeAdapter(MapAdapterExpression.class)
@XmlElement(name = "inputs")
    private final Map<String, Expression> inputs = new HashMap<>();

    public UserFilterDefinitionImpl(final String name, final String filterId, final String version) {
        super(name);
        this.filterId = filterId;
        this.version = version;
    }

    public UserFilterDefinitionImpl(){
        super();
        this.filterId = "default Id";
        this.version = "default version";
    }
    @Override
    public String getUserFilterId() {
        return filterId;
    }

    @Override
    public Map<String, Expression> getInputs() {
        return inputs;
    }

    public void addInput(final String name, final Expression expression) {
        inputs.put(name, expression);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserFilterDefinitionImpl that = (UserFilterDefinitionImpl) o;
        return Objects.equals(filterId, that.filterId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(inputs, that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), filterId, version, inputs);
    }

    @Override
    public String toString() {
        final int maxLen = 5;
        final StringBuilder builder = new StringBuilder();
        builder.append("UserFilterDefinitionImpl [filterId=");
        builder.append(filterId);
        builder.append(", version=");
        builder.append(version);
        builder.append(", inputs=");
        builder.append(inputs != null ? toString(inputs.entrySet(), maxLen) : null);
        builder.append("]");
        return builder.toString();
    }

    private String toString(final Collection<?> collection, final int maxLen) {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (final Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void accept(ModelFinderVisitor visitor, long modelId) {
        visitor.find(this, modelId);
    }

}
