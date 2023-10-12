/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive.converter;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedActivityItem;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedFlowNodeItem;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedHumanTaskItem;

/**
 * @author Vincent Elcrin
 */
public class ArchivedHumanTaskSearchDescriptorConverter extends ArchivedActivitySearchDescriptorConverter {

    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put(ArchivedFlowNodeItem.ATTRIBUTE_CASE_ID,
                ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID);
        mapping.put(ArchivedFlowNodeItem.ATTRIBUTE_ROOT_CASE_ID,
                ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID);
        mapping.put(ArchivedFlowNodeItem.ATTRIBUTE_PARENT_CASE_ID,
                ArchivedActivityInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID);
        mapping.put(ArchivedFlowNodeItem.ATTRIBUTE_PROCESS_ID,
                ArchivedHumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID);
        mapping.put(ArchivedFlowNodeItem.ATTRIBUTE_STATE, ArchivedHumanTaskInstanceSearchDescriptor.STATE_NAME);
        mapping.put(ArchivedActivityItem.ATTRIBUTE_REACHED_STATE_DATE,
                ArchivedHumanTaskInstanceSearchDescriptor.REACHED_STATE_DATE);
        // FIXME Add this filter in the engine
        // mapping.put(ArchivedFlowNodeItem.ATTRIBUTE_TYPE, ArchivedHumanTaskInstanceSearchDescriptor.FLOW_NODE_TYPE);
        mapping.put(ArchivedHumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID,
                ArchivedHumanTaskInstanceSearchDescriptor.ASSIGNEE_ID);
        mapping.put(ArchivedHumanTaskItem.ATTRIBUTE_PRIORITY, ArchivedHumanTaskInstanceSearchDescriptor.PRIORITY);
        mapping.put(ArchivedHumanTaskItem.ATTRIBUTE_ARCHIVED_DATE,
                ArchivedHumanTaskInstanceSearchDescriptor.ARCHIVE_DATE);
        mapping.put(ArchivedHumanTaskItem.ATTRIBUTE_SOURCE_OBJECT_ID,
                ArchivedHumanTaskInstanceSearchDescriptor.ORIGINAL_HUMAN_TASK_ID);
        mapping.put(ArchivedHumanTaskItem.FILTER_USER_ID, "");

    }

    public ArchivedHumanTaskSearchDescriptorConverter() {
        extendsMapping(mapping);
    }
}
