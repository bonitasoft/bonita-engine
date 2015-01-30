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

import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.events.model.SEvent;

/**
 * @author Baptiste Mesta
 */
public class ProcessInstanceHandler extends AbstractUpdateHandler {

    private static final long serialVersionUID = 1L;
    private final String identifier;

    public ProcessInstanceHandler(final long tenantId) {
        super(tenantId);
        identifier = UUID.randomUUID().toString();
    }

    @Override
    protected Map<String, Serializable> getEvent(final SEvent sEvent) {
        final SProcessInstance instance = (SProcessInstance) sEvent.getObject();
        return EventUtil.getEventForProcess(instance);
    }

    @Override
    public boolean isInterested(final SEvent event) {
        final Object object = event.getObject();
        return object instanceof SProcessInstance;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

}
