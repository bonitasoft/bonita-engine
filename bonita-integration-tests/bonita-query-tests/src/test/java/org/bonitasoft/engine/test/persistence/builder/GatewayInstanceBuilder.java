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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.model.impl.SGatewayInstanceImpl;


/**
 * @author Julien Reboul
 *
 */
public class GatewayInstanceBuilder extends FlowNodeInstanceBuilder<SGatewayInstanceImpl, GatewayInstanceBuilder> {

    private SGatewayType gatewayType;

    private String hitBys = "";

    public static GatewayInstanceBuilder aGatewayInstanceBuilder() {
        return new GatewayInstanceBuilder();
    }

    private GatewayInstanceBuilder() {
    }

    @Override
    GatewayInstanceBuilder getThisBuilder() {
        return this;
    }

    @Override
    SGatewayInstanceImpl _build() {
        return new SGatewayInstanceImpl();
    }
    
    @Override
    protected SGatewayInstanceImpl fill(SGatewayInstanceImpl persistent) {
        super.fill(persistent);
        persistent.setHitBys(hitBys);
        persistent.setGatewayType(gatewayType);
        return persistent;
    }

    public GatewayInstanceBuilder withGatewayType(final SGatewayType gatewayType) {
        this.gatewayType = gatewayType;
        return thisBuilder;
    }

    public GatewayInstanceBuilder withHitBys(final String hitBys) {
        this.hitBys = hitBys;
        return thisBuilder;
    }
}
