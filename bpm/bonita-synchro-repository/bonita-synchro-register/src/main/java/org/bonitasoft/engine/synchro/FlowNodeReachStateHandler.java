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
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.events.model.SEvent;

/**
 * @author Elias Ricken de Medeiros
 */
public class FlowNodeReachStateHandler extends AbstractJMSUpdateHandler {

    private static final long serialVersionUID = 970281460560288990L;

    private final String identifier;

    private final int stateId;

    public FlowNodeReachStateHandler(final long tenantId, final long messageTimeout, String brokerURL, final int stateId) {
        super(tenantId, messageTimeout, brokerURL);
        this.stateId = stateId;
        identifier = UUID.randomUUID().toString();
    }

    @Override
    protected Map<String, Serializable> getEvent(final SEvent sEvent) {
        final SFlowNodeInstance flowNodeInstance = (SFlowNodeInstance) sEvent.getObject();
        return PerfEventUtil.getFlowNodeReachStateEvent(flowNodeInstance.getRootContainerId(), flowNodeInstance.getName(), stateId);
    }

    @Override
    public boolean isInterested(final SEvent event) {
        // the !isStateExecuting avoid having 2 times the same event in case of execution of e.g. connectors
        if (event.getObject() instanceof SFlowNodeInstance) {
            final SFlowNodeInstance fni = (SFlowNodeInstance) event.getObject();
            boolean interested = !fni.isStateExecuting();
            interested &= fni.getStateId() == stateId;
            return interested;
        }
        return false;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
