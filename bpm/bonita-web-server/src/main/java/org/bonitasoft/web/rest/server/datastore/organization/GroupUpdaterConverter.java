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
package org.bonitasoft.web.rest.server.datastore.organization;

import java.util.Map;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.IconDescriptor;
import org.bonitasoft.engine.identity.GroupUpdater;
import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.rest.server.engineclient.GroupEngineClient;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;

/**
 * @author Colin PUY
 */
public class GroupUpdaterConverter {

    private final GroupEngineClient groupEngineClient;

    public GroupUpdaterConverter(GroupEngineClient groupEngineClient) {
        this.groupEngineClient = groupEngineClient;
    }

    public GroupUpdater convert(Map<String, String> attributes) {
        GroupUpdater updater = new GroupUpdater();
        if (attributes.containsKey(GroupItem.ATTRIBUTE_DESCRIPTION)) {
            updater.updateDescription(attributes.get(GroupItem.ATTRIBUTE_DESCRIPTION));
        }
        if (!MapUtil.isBlank(attributes, GroupItem.ATTRIBUTE_ICON)) {
            IconDescriptor iconDescriptor = getBonitaHomeFolderAccessor()
                    .getIconFromFileSystem(attributes.get(GroupItem.ATTRIBUTE_ICON));
            updater.updateIcon(iconDescriptor.getFilename(), iconDescriptor.getContent());
        }
        if (!MapUtil.isBlank(attributes, GroupItem.ATTRIBUTE_NAME)) {
            updater.updateName(attributes.get(GroupItem.ATTRIBUTE_NAME));
        }
        if (attributes.containsKey(GroupItem.ATTRIBUTE_DISPLAY_NAME)) {
            updater.updateDisplayName(attributes.get(GroupItem.ATTRIBUTE_DISPLAY_NAME));
        }
        if (attributes.containsKey(GroupItem.ATTRIBUTE_PARENT_GROUP_ID)) {
            String parentGroupPath = getParentGroupPath(attributes.get(GroupItem.ATTRIBUTE_PARENT_GROUP_ID));
            updater.updateParentPath(parentGroupPath);
        }
        return updater;
    }

    BonitaHomeFolderAccessor getBonitaHomeFolderAccessor() {
        return new BonitaHomeFolderAccessor();
    }

    private String getParentGroupPath(String groupId) {
        if (groupId.isEmpty()) {
            return "";
        } else {
            return groupEngineClient.getPath(groupId);
        }
    }
}
