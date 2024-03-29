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
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.events.model.SEvent;

/**
 * @author Baptiste Mesta
 * @author Guillaume Rosinosky
 */
public class ProcessInstanceFinishedHandler extends AbstractJMSUpdateHandler {

    private static final long serialVersionUID = 1L;

    private final String identifier;

    public ProcessInstanceFinishedHandler(final long messageTimeout, final String brokerURL) {
        super(messageTimeout, brokerURL);
        identifier = UUID.randomUUID().toString();
    }

    @Override
    protected Map<String, Serializable> getEvent(final SEvent sEvent) {
        final SProcessInstance instance = (SProcessInstance) sEvent.getObject();
        return PerfEventUtil.getProcessInstanceFinishedEvent(instance.getId());
    }

    @Override
    public boolean isInterested(final SEvent event) {
        final Object object = event.getObject();
        if (object instanceof SProcessInstance) {
            final SProcessInstance pi = (SProcessInstance) event.getObject();
            return pi.getStateId() == ProcessInstanceState.COMPLETED.getId();
        }
        return false;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

}
