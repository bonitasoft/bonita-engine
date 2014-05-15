/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
