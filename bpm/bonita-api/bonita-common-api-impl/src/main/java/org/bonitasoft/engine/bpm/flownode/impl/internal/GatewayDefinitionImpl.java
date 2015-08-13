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

import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

/**
 * @author Feng Hui
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class GatewayDefinitionImpl extends FlowNodeDefinitionImpl implements GatewayDefinition {

    private static final long serialVersionUID = 8091472342735043092L;

    @XmlAttribute
    private final GatewayType gatewayType;

    public GatewayDefinitionImpl(final String name, final GatewayType gatewayType) {
        super(name);
        this.gatewayType = gatewayType;
    }

    public GatewayDefinitionImpl(final long id, final String name, final GatewayType gatewayType) {
        super(id, name);
        this.gatewayType = gatewayType;
    }

    public GatewayDefinitionImpl() {
        super();
        gatewayType = GatewayType.EXCLUSIVE;
    }

    @Override
    public GatewayType getGatewayType() {
        return gatewayType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GatewayDefinitionImpl that = (GatewayDefinitionImpl) o;
        return Objects.equals(gatewayType, that.gatewayType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gatewayType);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("GatewayDefinitionImpl [gatewayType=");
        builder.append(gatewayType);
        builder.append("]");
        return builder.toString();
    }

}
