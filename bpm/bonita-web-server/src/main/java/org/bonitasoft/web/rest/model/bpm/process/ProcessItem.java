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

import java.util.Date;

import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.toolkit.client.common.util.StringUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasLastUpdateDate;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Vincent Elcrin
 * @author Celine Souchet
 */
public class ProcessItem extends Item implements ItemHasUniqueId, ItemHasLastUpdateDate, ItemHasDualName {

    public ProcessItem() {
        super();
    }

    public ProcessItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES NAMES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ATTRIBUTE_VERSION = "version";

    public static final String ATTRIBUTE_DESCRIPTION = "description";

    public static final String ATTRIBUTE_DEPLOYMENT_DATE = "deploymentDate";

    public static final String ATTRIBUTE_DEPLOYED_BY_USER_ID = "deployedBy";

    public static final String ATTRIBUTE_ACTIVATION_STATE = "activationState";

    public static final String ATTRIBUTE_CONFIGURATION_STATE = "configurationState";

    public static final String ATTRIBUTE_DISPLAY_DESCRIPTION = "displayDescription";

    public static final String ATTRIBUTE_ACTOR_INITIATOR_ID = "actorinitiatorid";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES VALUES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String VALUE_ACTIVATION_STATE_DISABLED = "DISABLED";

    public static final String VALUE_ACTIVATION_STATE_ENABLED = "ENABLED";

    public static final String VALUE_CONFIGURATION_STATE_UNRESOLVED = "UNRESOLVED";

    public static final String VALUE_CONFIGURATION_STATE_RESOLVED = "RESOLVED";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String FILTER_TEAM_MANAGER_ID = "team_manager_id";

    public static final String FILTER_SUPERVISOR_ID = "supervisor_id";

    public static final String FILTER_USER_ID = "user_id";

    public static final String FILTER_RECENT_PROCESSES = "recentProcesses";

    public static final String FILTER_CATEGORY_ID = "categoryId";

    public static final String FILTER_FOR_PENDING_OR_ASSIGNED_TASKS = "forPendingOrAssignedTask";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COUNTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String COUNTER_FAILED_CASES = "failedCases";

    public static final String COUNTER_OPEN_CASES = "openCases";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // GETTERS

    @Override
    public String getName() {
        return this.getAttributeValue(ATTRIBUTE_NAME);
    }

    public String getVersion() {
        return this.getAttributeValue(ATTRIBUTE_VERSION);
    }

    public String ensureName() {
        if (StringUtil.isBlank(getDisplayName())) {
            return getName();
        }
        return getDisplayName();
    }

    public String getDescription() {
        return this.getAttributeValue(ATTRIBUTE_DESCRIPTION);
    }

    public String getDeploymentDate() {
        return this.getAttributeValue(ATTRIBUTE_DEPLOYMENT_DATE);
    }

    public APIID getDeployedByUserId() {
        return APIID.makeAPIID(this.getAttributeValue(ATTRIBUTE_DEPLOYED_BY_USER_ID));
    }

    public String getActivationState() {
        return this.getAttributeValue(ATTRIBUTE_ACTIVATION_STATE);
    }

    public String getConfigurationState() {
        return this.getAttributeValue(ATTRIBUTE_CONFIGURATION_STATE);
    }

    @Override
    public String getDisplayName() {
        return this.getAttributeValue(ATTRIBUTE_DISPLAY_NAME);
    }

    public String getDisplayDescription() {
        return this.getAttributeValue(ATTRIBUTE_DISPLAY_DESCRIPTION);
    }

    @Override
    public Date getLastUpdateDate() {
        return this.getAttributeValueAsDate(ATTRIBUTE_LAST_UPDATE_DATE);
    }

    public String getActorInitiatorId() {
        return this.getAttributeValue(ATTRIBUTE_ACTOR_INITIATOR_ID);
    }

    // SETTERS

    @Override
    public void setId(final String id) {
        this.setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public void setId(final Long id) {
        this.setId(id.toString());
    }

    @Override
    public void setName(final String name) {
        this.setAttribute(ATTRIBUTE_NAME, name);
    }

    public void setVersion(final String version) {
        this.setAttribute(ATTRIBUTE_VERSION, version);
    }

    public void setDescription(final String description) {
        this.setAttribute(ATTRIBUTE_DESCRIPTION, description);
    }

    public void setDeployedByUserId(final String id) {
        this.setAttribute(ATTRIBUTE_DEPLOYED_BY_USER_ID, id);
    }

    public void setDeployedByUserId(final Long id) {
        setDeployedByUserId(id.toString());
    }

    public void setDeployedByUserId(final APIID id) {
        this.setAttribute(ATTRIBUTE_DEPLOYED_BY_USER_ID, id);
    }

    public void setDeploymentDate(final String date) {
        this.setAttribute(ATTRIBUTE_DEPLOYMENT_DATE, date);
    }

    public void setDeploymentDate(final Date date) {
        this.setAttribute(ATTRIBUTE_DEPLOYMENT_DATE, date);
    }

    public void setActivationState(final String state) {
        this.setAttribute(ATTRIBUTE_ACTIVATION_STATE, state);
    }

    public void setConfigurationState(final String state) {
        this.setAttribute(ATTRIBUTE_CONFIGURATION_STATE, state);
    }

    @Override
    public void setDisplayName(final String displayName) {
        this.setAttribute(ATTRIBUTE_DISPLAY_NAME, displayName);
    }

    public void setDisplayDescription(final String displayDescription) {
        this.setAttribute(ATTRIBUTE_DISPLAY_DESCRIPTION, displayDescription);
    }

    @Override
    public void setLastUpdateDate(final String date) {
        this.setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    @Override
    public void setLastUpdateDate(final Date date) {
        this.setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    public void setActorInitiatorId(final Long id) {
        setActorInitiatorId(id.toString());
    }

    public void setActorInitiatorId(final APIID id) {
        setActorInitiatorId(id.toString());
    }

    public void setActorInitiatorId(final String id) {
        this.setAttribute(ATTRIBUTE_ACTOR_INITIATOR_ID, id);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPLOYS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UserItem getDeployedByUser() {
        return new UserItem(getDeploy(ATTRIBUTE_DEPLOYED_BY_USER_ID));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isEnabled() {
        return VALUE_ACTIVATION_STATE_ENABLED.equals(getActivationState());
    }

    public boolean isResolved() {
        return VALUE_CONFIGURATION_STATE_RESOLVED.equals(getConfigurationState());
    }

    public boolean isStartable() {
        return isEnabled() && isResolved();
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.web.toolkit.client.data.item.Item#getItemDefinition()
     */
    @Override
    public ItemDefinition<ProcessItem> getItemDefinition() {
        return ProcessDefinition.get();
    }
}
