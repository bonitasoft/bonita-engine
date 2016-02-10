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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.flownode.FlowElementInstanceSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowElementInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAFlowElementInstanceBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Emmanuel Duchastenier
 */
public class SearchArchivedFlowElementInstanceDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> flowElementInstanceDescriptorKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> flowElementInstanceDescriptorAllFields;

    public SearchArchivedFlowElementInstanceDescriptor() {
        final SAFlowElementInstanceBuilderFactory keyProvider = BuilderFactory.get(SAFlowElementInstanceBuilderFactory.class);
        flowElementInstanceDescriptorKeys = new HashMap<String, FieldDescriptor>(7);
        flowElementInstanceDescriptorKeys.put(FlowElementInstanceSearchDescriptor.NAME,
                new FieldDescriptor(SAFlowElementInstance.class, keyProvider.getNameKey()));
        flowElementInstanceDescriptorKeys.put(FlowElementInstanceSearchDescriptor.DESCRIPTION, new FieldDescriptor(SAFlowElementInstance.class,
                keyProvider.getDescriptionKey()));
        // flowElementInstanceDescriptorKeys.put(FlowElementInstanceSearchDescriptor.STATE_NAME, new FieldDescriptor(SAFlowElementInstance.class,
        // flowElementKeyProvider.getStateNameKey()));

        final Set<String> tasksInstanceFields = new HashSet<String>(2);
        tasksInstanceFields.add(keyProvider.getNameKey());
        tasksInstanceFields.add(keyProvider.getDescriptionKey());
        flowElementInstanceDescriptorAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        flowElementInstanceDescriptorAllFields.put(SAFlowElementInstance.class, tasksInstanceFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return flowElementInstanceDescriptorKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return flowElementInstanceDescriptorAllFields;
    }

}
