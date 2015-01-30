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
 * @author Baptiste Mesta
 */
public class FlowNodeHandler extends AbstractUpdateHandler {

    private static final long serialVersionUID = 1L;
    private final String identifier;

    public FlowNodeHandler(final long tenantId) {
        super(tenantId);
        identifier = UUID.randomUUID().toString();
    }

    @Override
    protected Map<String, Serializable> getEvent(final SEvent sEvent) {
        final SFlowNodeInstance flowNodeInstance = (SFlowNodeInstance) sEvent.getObject();
        return EventUtil.getEventForFlowNode(flowNodeInstance);
    }

    @Override
    public boolean isInterested(final SEvent event) {
        // the !isStateExecuting avoids having 2 times the same event in case of execution of e.g. connectors
        return event.getObject() instanceof SFlowNodeInstance && !((SFlowNodeInstance) event.getObject()).isStateExecuting();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

}
