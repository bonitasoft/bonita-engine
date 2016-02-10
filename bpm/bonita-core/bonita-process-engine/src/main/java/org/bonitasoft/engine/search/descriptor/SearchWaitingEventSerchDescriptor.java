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
package org.bonitasoft.engine.search.descriptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.flownode.WaitingEventSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingEventKeyProviderBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 */
public class SearchWaitingEventSerchDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> activityInstanceDescriptorKeys;

    public SearchWaitingEventSerchDescriptor() {
        final SWaitingEventKeyProviderBuilderFactory keyProvider = BuilderFactory.get(SWaitingEventKeyProviderBuilderFactory.class);
        activityInstanceDescriptorKeys = new HashMap<String, FieldDescriptor>(6);
        activityInstanceDescriptorKeys.put(WaitingEventSearchDescriptor.BPM_EVENT_TYPE,
                new FieldDescriptor(SWaitingEvent.class, keyProvider.getEventTypeKey()));
        activityInstanceDescriptorKeys.put(WaitingEventSearchDescriptor.FLOW_NODE_NAME,
                new FieldDescriptor(SWaitingEvent.class, keyProvider.getFlowNodeNameKey()));
        activityInstanceDescriptorKeys.put(WaitingEventSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, new FieldDescriptor(SWaitingEvent.class,
                keyProvider.getParentProcessInstanceIdKey()));
        activityInstanceDescriptorKeys.put(WaitingEventSearchDescriptor.PROCESS_DEFINITION_ID,
                new FieldDescriptor(SWaitingEvent.class, keyProvider.getProcessDefinitionIdKey()));
        activityInstanceDescriptorKeys.put(WaitingEventSearchDescriptor.PROCESS_NAME,
                new FieldDescriptor(SWaitingEvent.class, keyProvider.getProcessNameKey()));
        activityInstanceDescriptorKeys.put(WaitingEventSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, new FieldDescriptor(SWaitingEvent.class,
                keyProvider.getRootProcessInstanceIdKey()));
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return activityInstanceDescriptorKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return Collections.emptyMap();
    }

}
