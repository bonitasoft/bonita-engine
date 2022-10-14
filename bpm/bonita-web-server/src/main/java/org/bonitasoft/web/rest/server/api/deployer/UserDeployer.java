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
package org.bonitasoft.web.rest.server.api.deployer;

import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.framework.Deployer;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;

/**
 * @author Vincent Elcrin
 */
public class UserDeployer implements Deployer {

    private final DatastoreHasGet<UserItem> getter;

    private final String attribute;

    public UserDeployer(DatastoreHasGet<UserItem> getter, String attribute) {
        this.getter = getter;
        this.attribute = attribute;
    }

    @Override
    public String getDeployedAttribute() {
        return attribute;
    }

    @Override
    public void deployIn(IItem item) {
        if (isDeployable(attribute, item)) {
            item.setDeploy(attribute, getUser(getUserId(item)));
        }
    }

    private APIID getUserId(IItem item) {
        return item.getAttributeValueAsAPIID(attribute);
    }

    private UserItem getUser(APIID userId) {
        return getter.get(userId);
    }

}
