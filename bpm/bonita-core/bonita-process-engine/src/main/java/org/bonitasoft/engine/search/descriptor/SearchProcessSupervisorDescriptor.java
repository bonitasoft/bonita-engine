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
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisorSearchDescriptor;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilderFactory;

/**
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class SearchProcessSupervisorDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> fieldDescriptorMap;

    private final Map<Class<? extends PersistentObject>, Set<String>> supervisorAllFields;

    public SearchProcessSupervisorDescriptor() {
        // final SUserBuilder userBuilder = identityModelBuilder.getUserBuilder();
        // final GroupBuilder groupBuilder = identityModelBuilder.getGroupBuilder();
        // final RoleBuilder roleBuilder = identityModelBuilder.getRoleBuilder();
        fieldDescriptorMap = new HashMap<String, FieldDescriptor>(5);
        fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getIdKey()));
        fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.PROCESS_DEFINITION_ID,
                new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getProcessDefIdKey()));
        fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.USER_ID, new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getUserIdKey()));
        fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.GROUP_ID, new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getGroupIdKey()));
        fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.ROLE_ID, new FieldDescriptor(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getRoleIdKey()));
        // fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.USER_FISRT_NAME, new FieldDescriptor(SUser.class, userBuilder.getFirstNameKey()));
        // fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.USER_LAST_NAME, new FieldDescriptor(SUser.class, userBuilder.getLastNameKey()));
        // fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.USERNAME, new FieldDescriptor(SUser.class, userBuilder.getUserNameKey()));
        // fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.GROUP_NAME, new FieldDescriptor(SGroup.class, groupBuilder.getNameKey()));
        // fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.GROUP_PARENT_PATH, new FieldDescriptor(SGroup.class, groupBuilder.getParentPathKey()));
        // fieldDescriptorMap.put(ProcessSupervisorSearchDescriptor.ROLE_NAME, new FieldDescriptor(SRole.class, roleBuilder.getNameKey()));
        //
        supervisorAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(3);
        // final Set<String> userFields = new HashSet<String>(3);
        // userFields.add(userBuilder.getFirstNameKey());
        // userFields.add(userBuilder.getLastNameKey());
        // userFields.add(userBuilder.getUserNameKey());
        // supervisorAllFields.put(SUser.class, userFields);
        //
        // final Set<String> groupFields = new HashSet<String>(2);
        // groupFields.add(groupBuilder.getNameKey());
        // groupFields.add(groupBuilder.getParentPathKey());
        // supervisorAllFields.put(SGroup.class, groupFields);
        //
        // final Set<String> roleFields = new HashSet<String>(1);
        // roleFields.add(roleBuilder.getNameKey());
        // supervisorAllFields.put(SRole.class, roleFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return fieldDescriptorMap;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return supervisorAllFields;
    }

}
