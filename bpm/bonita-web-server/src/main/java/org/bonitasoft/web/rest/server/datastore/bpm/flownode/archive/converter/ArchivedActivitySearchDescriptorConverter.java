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
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedActivityItem;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedFlowNodeItem;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedHumanTaskItem;

/**
 * @author Colin PUY, Vincent Elcrin
 */
public class ArchivedActivitySearchDescriptorConverter extends ArchivedFlowNodeSearchDescriptorConverter {

    private static final Map<String, String> additionalAttributes = new HashMap<>();

    static {
        additionalAttributes.put(ArchivedActivityItem.ATTRIBUTE_REACHED_STATE_DATE,
                ArchivedActivityInstanceSearchDescriptor.REACHED_STATE_DATE);
        additionalAttributes.put(ArchivedFlowNodeItem.ATTRIBUTE_CASE_ID,
                ArchivedActivityInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID);
        additionalAttributes.put(ArchivedFlowNodeItem.ATTRIBUTE_ROOT_CASE_ID,
                ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID);
        additionalAttributes.put(ArchivedFlowNodeItem.ATTRIBUTE_PARENT_CASE_ID,
                ArchivedActivityInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID);
        additionalAttributes.put(ArchivedFlowNodeItem.ATTRIBUTE_PROCESS_ID,
                ArchivedActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID);
        additionalAttributes.put(ArchivedFlowNodeItem.ATTRIBUTE_STATE,
                ArchivedActivityInstanceSearchDescriptor.STATE_NAME);
        additionalAttributes.put(ArchivedHumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID,
                ArchivedActivityInstanceSearchDescriptor.ASSIGNEE_ID);
        additionalAttributes.put(ArchivedHumanTaskItem.ATTRIBUTE_PRIORITY,
                ArchivedActivityInstanceSearchDescriptor.PRIORITY);
        additionalAttributes.put(ArchivedHumanTaskItem.ATTRIBUTE_TYPE,
                ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE);
        additionalAttributes.put(ArchivedActivityItem.ATTRIBUTE_SOURCE_OBJECT_ID,
                ArchivedActivityInstanceSearchDescriptor.SOURCE_OBJECT_ID);
        additionalAttributes.put(ArchivedActivityItem.ATTRIBUTE_ARCHIVED_DATE,
                ArchivedActivityInstanceSearchDescriptor.ARCHIVE_DATE);
    }

    public ArchivedActivitySearchDescriptorConverter() {
        extendsMapping(additionalAttributes);
    }
}
