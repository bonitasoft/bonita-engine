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

import static org.bonitasoft.web.toolkit.client.common.util.StringUtil.isBlank;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.IconDescriptor;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.rest.server.engineclient.GroupEngineClient;

/**
 * @author Colin PUY
 */
public class GroupCreatorConverter {

    private final GroupEngineClient groupEngineClient;

    public GroupCreatorConverter(GroupEngineClient groupEngineClient) {
        this.groupEngineClient = groupEngineClient;
    }

    public GroupCreator convert(GroupItem item) {
        if (item == null) {
            return null;
        }

        GroupCreator builder = new GroupCreator(item.getName());

        if (!isBlank(item.getDescription())) {
            builder.setDescription(item.getDescription());
        }

        if (!isBlank(item.getDisplayName())) {
            builder.setDisplayName(item.getDisplayName());
        }

        if (!isBlank(item.getIcon())) {
            IconDescriptor iconDescriptor = new BonitaHomeFolderAccessor().getIconFromFileSystem(item.getIcon());
            builder.setIcon(iconDescriptor.getFilename(), iconDescriptor.getContent());
        }

        if (!isBlank(item.getParentGroupId())) {
            String parentGroupPath = groupEngineClient.getPath(item.getParentGroupId());
            builder.setParentPath(parentGroupPath);
        }
        return builder;
    }

}
