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
package org.bonitasoft.web.rest.server.datastore.bpm.process.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.server.datastore.converter.AttributeConverter;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute.TYPE;

/**
 * @author Vincent Elcrin
 */
public class ProcessSearchDescriptorConverter implements AttributeConverter {

    protected static final Map<String, String> mapping = new HashMap<>();

    @Override
    public Map<String, TYPE> getValueTypeMapping() {
        return Collections.emptyMap();
    }

    static {
        mapping.put(ProcessItem.ATTRIBUTE_ID, ProcessDeploymentInfoSearchDescriptor.ID);
        mapping.put(ProcessItem.ATTRIBUTE_ACTIVATION_STATE, ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE);
        mapping.put(ProcessItem.ATTRIBUTE_CONFIGURATION_STATE,
                ProcessDeploymentInfoSearchDescriptor.CONFIGURATION_STATE);
        mapping.put(ProcessItem.ATTRIBUTE_DEPLOYED_BY_USER_ID, ProcessDeploymentInfoSearchDescriptor.DEPLOYED_BY);
        mapping.put(ProcessItem.ATTRIBUTE_DEPLOYMENT_DATE, ProcessDeploymentInfoSearchDescriptor.DEPLOYMENT_DATE);
        mapping.put(ProcessItem.ATTRIBUTE_DISPLAY_NAME, ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME);
        mapping.put(ProcessItem.ATTRIBUTE_LAST_UPDATE_DATE, ProcessDeploymentInfoSearchDescriptor.LAST_UPDATE_DATE);
        mapping.put(ProcessItem.ATTRIBUTE_NAME, ProcessDeploymentInfoSearchDescriptor.NAME);
        mapping.put(ProcessItem.ATTRIBUTE_VERSION, ProcessDeploymentInfoSearchDescriptor.VERSION);

        mapping.put(ProcessItem.FILTER_CATEGORY_ID, ProcessDeploymentInfoSearchDescriptor.CATEGORY_ID);
        mapping.put(ProcessItem.FILTER_RECENT_PROCESSES, ""); // code smell. Should return empty object (EmptyField)
        mapping.put(ProcessItem.FILTER_SUPERVISOR_ID, "");
        mapping.put(ProcessItem.FILTER_TEAM_MANAGER_ID, "");
        mapping.put(ProcessItem.FILTER_USER_ID, "");
        mapping.put(ProcessItem.FILTER_FOR_PENDING_OR_ASSIGNED_TASKS, "");
    }

    @Override
    public String convert(String attribute) {
        String value = mapping.get(attribute);
        if (value == null) {
            throw new RuntimeException("Can't find search descriptor corresponding to " + attribute);
        }
        return value;
    }

}
