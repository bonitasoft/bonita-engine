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

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.events.model.SEvent;

/**
 * @author Baptiste Mesta
 */
public class TaskReadyHandler extends AbstractJMSUpdateHandler {

    private static final long serialVersionUID = 1L;

    private final String identifier;

    public TaskReadyHandler(final long tenantId, final long messageTimeout, final String brokerURL) {
        super(tenantId, messageTimeout, brokerURL);
        this.identifier = UUID.randomUUID().toString();
    }

    @Override
    protected Map<String, Serializable> getEvent(final SEvent sEvent) {
        final SFlowNodeInstance flowNodeInstance = (SFlowNodeInstance) sEvent.getObject();
        return PerfEventUtil.getReadyTaskEvent(flowNodeInstance.getRootContainerId(), flowNodeInstance.getName());
    }

    @Override
    public boolean isInterested(final SEvent event) {
        // the !isStateExecuting avoid having 2 times the same event in case of execution of e.g. connectors
    	if (event.getObject() instanceof SFlowNodeInstance) {
    		final SFlowNodeInstance fni = (SFlowNodeInstance) event.getObject();
    		boolean interested = !fni.isStateExecuting();
    		interested &= fni.getStateId() == 4;
    		interested &= (fni.getType() == SFlowNodeType.USER_TASK || fni.getType() == SFlowNodeType.MANUAL_TASK);
    		return interested;
    	}
    	return false;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
