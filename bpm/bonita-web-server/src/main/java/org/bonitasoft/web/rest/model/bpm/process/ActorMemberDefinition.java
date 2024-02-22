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
package org.bonitasoft.web.rest.model.bpm.process;

import org.bonitasoft.web.rest.model.portal.profile.AbstractMemberDefinition;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Haojie Yuan
 * @author Séverin Moussel
 */
public class ActorMemberDefinition extends AbstractMemberDefinition {

    /**
     * Singleton
     */
    public static ActorMemberDefinition get() {
        return (ActorMemberDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "actormember";

    /**
     * the URL of users resource
     */
    private static final String API_URL = "../API/bpm/actorMember";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {

        // FIXME : Remove after engine has removed the unique ID.
        setPrimaryKeys(ActorMemberItem.ATTRIBUTE_ID);

        // FIXME : Uncomment after engine has removed the unique ID.
        // setPrimaryKeys(
        // ATTRIBUTE_ACTOR_ID,
        // ATTRIBUTE_USER_ID,
        // ATTRIBUTE_ROLE_ID,
        // ATTRIBUTE_GROUP_ID);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(ActorMemberItem.ATTRIBUTE_ACTOR_ID, ItemAttribute.TYPE.ITEM_ID);
        super.defineAttributes();
    }

    @Override
    protected void defineDeploys() {
        declareDeployable(ActorMemberItem.ATTRIBUTE_ACTOR_ID, ActorDefinition.get());
        super.defineDeploys();
    }

    @Override
    public IItem _createItem() {
        return new ActorMemberItem();
    }
}
