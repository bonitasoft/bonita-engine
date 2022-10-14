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
package org.bonitasoft.web.rest.server.engineclient;

import java.util.List;

import org.bonitasoft.engine.api.GroupAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupCreator.GroupField;
import org.bonitasoft.engine.identity.GroupNotFoundException;
import org.bonitasoft.engine.identity.GroupUpdater;
import org.bonitasoft.web.rest.model.identity.GroupDefinition;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Paul AMAR
 */
public class GroupEngineClient {

    private final GroupAPI groupAPI;

    protected GroupEngineClient(GroupAPI groupAPI) {
        this.groupAPI = groupAPI;
    }

    public Group get(Long groupId) {
        try {
            return groupAPI.getGroup(groupId);
        } catch (GroupNotFoundException e) {
            throw new APIItemNotFoundException(GroupDefinition.TOKEN, APIID.makeAPIID(groupId));
        }
    }

    public String getPath(String groupId) {
        try {
            return groupAPI.getGroup(parseId(groupId)).getPath();
        } catch (GroupNotFoundException e) {
            throw new APINotFoundException(new T_("Unable to get group path, group not found"));
        }
    }

    private long parseId(String groupId) {
        try {
            return Long.parseLong(groupId);
        } catch (NumberFormatException e) {
            throw new APIException("Illegal argument, groupId must be a number");
        }
    }

    public void delete(List<Long> groupIds) {
        try {
            groupAPI.deleteGroups(groupIds);
        } catch (DeletionException e) {
            if (e.getCause() instanceof GroupNotFoundException) {
                throw new APIItemNotFoundException(GroupDefinition.TOKEN);
            } else {
                throw new APIException(new T_("Error when deleting groups"), e);
            }
        }
    }

    public Group update(long groupId, GroupUpdater groupUpdater) {
        try {
            return groupAPI.updateGroup(groupId, groupUpdater);
        } catch (GroupNotFoundException e) {
            throw new APIItemNotFoundException(GroupDefinition.TOKEN, APIID.makeAPIID(groupId));
        } catch (UpdateException e) {
            throw new APIException(new T_("Error when updating group"), e);
        } catch (AlreadyExistsException e) {
            throw new APIForbiddenException(new T_("A group with the name %groupName% already exists",
                    new Arg("groupName", groupUpdater.getFields().get(GroupField.NAME))));
        }
    }

    public Group create(GroupCreator groupCreator) {
        try {
            return groupAPI.createGroup(groupCreator);
        } catch (AlreadyExistsException e) {
            throw new APIForbiddenException(new T_(
                    "Can't create group. Group '%groupName%' already exists",
                    new Arg("groupName", groupCreator.getFields().get(GroupField.NAME))));
        } catch (CreationException e) {
            throw new APIException(new T_("Error when creating group"), e);
        }
    }
}
