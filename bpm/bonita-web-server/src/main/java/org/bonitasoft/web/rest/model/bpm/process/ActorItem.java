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

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Haojie Yuan
 * @author Séverin Moussel
 */
public class ActorItem extends Item implements ItemHasUniqueId, ItemHasDualName {

    public ActorItem() {
        super();
    }

    public ActorItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ATTRIBUTE_DESCRIPTION = "description";

    public static final String ATTRIBUTE_PROCESS_ID = "process_id";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COUNTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String COUNTER_USERS = "users";

    public static final String COUNTER_GROUPS = "groups";

    public static final String COUNTER_ROLES = "roles";

    public static final String COUNTER_MEMBERSHIPS = "memberships";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SETTERS AND GETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Setters

    @Override
    public void setId(final String id) {
        setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public void setId(final Long id) {
        setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public void setName(final String name) {
        setAttribute(ATTRIBUTE_NAME, name);
    }

    @Override
    public void setDisplayName(final String name) {
        setAttribute(ATTRIBUTE_DISPLAY_NAME, name);
    }

    public void setDescription(final String description) {
        setAttribute(ATTRIBUTE_DESCRIPTION, description);
    }

    public void setProcessId(final String id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    public void setProcessId(final Long id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    public void setProcessId(final APIID id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    // Getters

    @Override
    public String getName() {
        return getAttributeValue(ATTRIBUTE_NAME);
    }

    @Override
    public String getDisplayName() {
        return getAttributeValue(ATTRIBUTE_DISPLAY_NAME);
    }

    public String getDescription() {
        return getAttributeValue(ATTRIBUTE_DESCRIPTION);
    }

    public APIID getProcessId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_PROCESS_ID);
    }

    // Counters

    public Long getNbSelectedUsers() {
        return getAttributeValueAsLong(COUNTER_USERS);
    }

    public Long getNbSelectedGroups() {
        return getAttributeValueAsLong(COUNTER_GROUPS);
    }

    public Long getNbSelectedRoles() {
        return getAttributeValueAsLong(COUNTER_ROLES);
    }

    public Long getNbSelectedMembershipss() {
        return getAttributeValueAsLong(COUNTER_MEMBERSHIPS);
    }

    // Deploys

    public ProcessItem getProcess() {
        return new ProcessItem(getDeploy(ATTRIBUTE_PROCESS_ID));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemDefinition getItemDefinition() {
        return ActorDefinition.get();
    }
}
