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

import org.bonitasoft.engine.bpm.flownode.EventTriggerInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstanceSearchDescriptor;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public class SearchEventTriggerInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> eventTriggerInstanceDescriptorKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> eventTriggerInstanceDescriptorAllFields;

    public SearchEventTriggerInstanceDescriptor() {
        eventTriggerInstanceDescriptorKeys = new HashMap<String, FieldDescriptor>(6);
        eventTriggerInstanceDescriptorKeys.put(EventTriggerInstanceSearchDescriptor.EVENT_INSTANCE_ID, new FieldDescriptor(SEventTriggerInstance.class,
                "eventInstanceId"));
        eventTriggerInstanceDescriptorKeys.put(TimerEventTriggerInstanceSearchDescriptor.EVENT_INSTANCE_NAME, new FieldDescriptor(SEventInstance.class,
                "name"));
        eventTriggerInstanceDescriptorKeys.put(TimerEventTriggerInstanceSearchDescriptor.EXECUTION_DATE, new FieldDescriptor(SEventTriggerInstance.class,
                "executionDate"));

        eventTriggerInstanceDescriptorAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        eventTriggerInstanceDescriptorAllFields.put(SEventInstance.class, Collections.singleton("name"));
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return eventTriggerInstanceDescriptorKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return eventTriggerInstanceDescriptorAllFields;
    }

}
