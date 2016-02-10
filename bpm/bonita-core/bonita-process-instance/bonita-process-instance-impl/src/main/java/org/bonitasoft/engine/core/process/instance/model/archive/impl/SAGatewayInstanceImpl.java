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
package org.bonitasoft.engine.core.process.instance.model.archive.impl;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SGatewayType;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAGatewayInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Hongwen Zang
 * @author Matthieu Chaffotte
 */
public class SAGatewayInstanceImpl extends SAFlowNodeInstanceImpl implements SAGatewayInstance {

    private static final long serialVersionUID = -3255817753577461503L;

    private SGatewayType gatewayType;

    private String hitBys = "";

    public SAGatewayInstanceImpl() {
        super();
    }

    public SAGatewayInstanceImpl(final SGatewayInstance sGatewayInstance) {
        super(sGatewayInstance);
        gatewayType = sGatewayInstance.getGatewayType();
        hitBys = sGatewayInstance.getHitBys();
    }

    public void setGatewayType(final SGatewayType gatewayType) {
        this.gatewayType = gatewayType;
    }

    @Override
    public SGatewayType getGatewayType() {
        return gatewayType;
    }

    @Override
    public String getHitBys() {
        return hitBys;
    }

    public void setHitBys(final String hitBys) {
        this.hitBys = hitBys;
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.GATEWAY;
    }

    @Override
    public String getDiscriminator() {
        return SAGatewayInstance.class.getName();
    }

    @Override
    public String getKind() {
        return "gate";
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SGatewayInstance.class;
    }

}
