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
package org.bonitasoft.web.rest.model.portal.profile;

import static org.bonitasoft.web.rest.model.portal.profile.AbstractMemberItem.ATTRIBUTE_GROUP_ID;
import static org.bonitasoft.web.rest.model.portal.profile.AbstractMemberItem.ATTRIBUTE_ROLE_ID;
import static org.bonitasoft.web.rest.model.portal.profile.AbstractMemberItem.ATTRIBUTE_USER_ID;
import static org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute.TYPE.ITEM_ID;

import org.bonitasoft.web.rest.model.identity.RoleDefinition;
import org.bonitasoft.web.rest.model.identity.UserDefinition;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public abstract class AbstractMemberDefinition<E extends IItem> extends ItemDefinition<E> {

    @Override
    protected void defineAttributes() {
        createAttribute(ATTRIBUTE_GROUP_ID, ITEM_ID);
        createAttribute(ATTRIBUTE_USER_ID, ITEM_ID);
        createAttribute(ATTRIBUTE_ROLE_ID, ITEM_ID);
    }

    @Override
    protected void defineDeploys() {
        declareDeployable(ATTRIBUTE_USER_ID, UserDefinition.get());
        declareDeployable(ATTRIBUTE_GROUP_ID, UserDefinition.get());
        declareDeployable(ATTRIBUTE_ROLE_ID, RoleDefinition.get());
    }

}
