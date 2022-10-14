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
package org.bonitasoft.web.rest.model.identity;

import java.util.Date;

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasCreator;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasIcon;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasLastUpdateDate;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Séverin Moussel
 */
public class UserItem extends Item implements ItemHasUniqueId, ItemHasLastUpdateDate, ItemHasCreator, ItemHasIcon {

    public static final String DEFAULT_USER_ICON = "icons/default/icon_user.png";

    public static final String ATTRIBUTE_FIRSTNAME = "firstname";

    public static final String ATTRIBUTE_LASTNAME = "lastname";

    public static final String ATTRIBUTE_PASSWORD = "password";

    public static final String ATTRIBUTE_USERNAME = "userName";

    public static final String ATTRIBUTE_MANAGER_ID = "manager_id";

    public static final String ATTRIBUTE_LAST_CONNECTION_DATE = "last_connection";

    public static final String ATTRIBUTE_TITLE = "title";

    public static final String ATTRIBUTE_JOB_TITLE = "job_title";

    public static final String ATTRIBUTE_STATE = "user_state";

    public static final String ATTRIBUTE_ENABLED = "enabled";

    public UserItem() {
        super();
    }

    public UserItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES VALUES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String VALUE_ACTIVATION_STATE_DISABLED = "DISABLED";

    public static final String VALUE_ACTIVATION_STATE_ENABLED = "ENABLED";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String FILTER_ROLE_ID = "role_id";

    public static final String FILTER_GROUP_ID = "group_id";

    public static final String FILTER_PROFILE_ID = "profile_id";

    public static final String FILTER_INDIRECT_PROFILE_ID = "indirect_profile_id";

    public static final String FILTER_PROCESS_ID = "process_id";

    public static final String FILTER_HUMAN_TASK_ID = "task_id";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COUNTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String COUNTER_OPEN_TASKS = "open_tasks";

    public static final String COUNTER_OVERDUE_TASKS = "overdue_tasks";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // GETTERS

    public String getFirstName() {
        return this.getAttributeValue(ATTRIBUTE_FIRSTNAME);
    }

    public String getLastName() {
        return this.getAttributeValue(ATTRIBUTE_LASTNAME);
    }

    public String getPassword() {
        return this.getAttributeValue(ATTRIBUTE_PASSWORD);
    }

    public String getUserName() {
        return this.getAttributeValue(ATTRIBUTE_USERNAME);
    }

    public APIID getManagerId() {
        return APIID.makeAPIID(this.getAttributeValue(ATTRIBUTE_MANAGER_ID));
    }

    @Override
    public String getIcon() {
        return this.getAttributeValue(ATTRIBUTE_ICON);
    }

    @Override
    public Date getCreationDate() {
        return this.getAttributeValueAsDate(ATTRIBUTE_CREATION_DATE);
    }

    @Override
    public APIID getCreatedByUserId() {
        return APIID.makeAPIID(this.getAttributeValue(ATTRIBUTE_CREATED_BY_USER_ID));
    }

    @Override
    public Date getLastUpdateDate() {
        return this.getAttributeValueAsDate(ATTRIBUTE_LAST_UPDATE_DATE);
    }

    public String getState() {
        return this.getAttributeValue(ATTRIBUTE_STATE);
    }

    public String getLastConnectionDate() {
        return this.getAttributeValue(ATTRIBUTE_LAST_CONNECTION_DATE);
    }

    public String getTitle() {
        return this.getAttributeValue(ATTRIBUTE_TITLE);
    }

    public String getJobTitle() {
        return this.getAttributeValue(ATTRIBUTE_JOB_TITLE);
    }

    public boolean isEnabled() {
        return "true".equals(getAttributeValue(ATTRIBUTE_ENABLED));
    }

    public void setEnabled(boolean enabled) {
        setAttribute(ATTRIBUTE_ENABLED, String.valueOf(enabled));
    }

    // SETTERS

    @Override
    public void setId(final String id) {
        this.setAttribute("id", id);
    }

    @Override
    public void setId(final Long id) {
        this.setAttribute("id", id.toString());
    }

    public void setFirstName(final String firstName) {
        this.setAttribute(ATTRIBUTE_FIRSTNAME, firstName);
    }

    public void setLastName(final String lastName) {
        this.setAttribute(ATTRIBUTE_LASTNAME, lastName);
    }

    public void setPassword(final String password) {
        this.setAttribute(ATTRIBUTE_PASSWORD, password);
    }

    public void setUserName(final String userName) {
        this.setAttribute(ATTRIBUTE_USERNAME, userName);
    }

    public void setManagerId(final String id) {
        this.setAttribute(ATTRIBUTE_MANAGER_ID, id);
    }

    public void setManagerId(final Long id) {
        setManagerId(id.toString());
    }

    public void setManagerId(final APIID id) {
        setAttribute(ATTRIBUTE_MANAGER_ID, id);
    }

    public void setState(final String state) {
        this.setAttribute(ATTRIBUTE_STATE, state);
    }

    @Override
    public void setIcon(final String iconPath) {
        this.setAttribute(ATTRIBUTE_ICON, iconPath);
    }

    @Override
    public void setCreationDate(final String date) {
        this.setAttribute(ATTRIBUTE_CREATION_DATE, date);
    }

    @Override
    public void setCreationDate(final Date date) {
        this.setAttribute(ATTRIBUTE_CREATION_DATE, date);
    }

    @Override
    public void setCreatedByUserId(final String id) {
        this.setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);
    }

    @Override
    public void setCreatedByUserId(final Long id) {
        setCreatedByUserId(id.toString());
    }

    @Override
    public void setCreatedByUserId(final APIID id) {
        this.setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);
    }

    @Override
    public void setLastUpdateDate(final String date) {
        this.setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    @Override
    public void setLastUpdateDate(final Date date) {
        this.setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    public void setLastConnectionDate(final String date) {
        this.setAttribute(ATTRIBUTE_LAST_CONNECTION_DATE, date);
    }

    public void setLastConnectionDate(final Date date) {
        this.setAttribute(ATTRIBUTE_LAST_CONNECTION_DATE, date);
    }

    public void setTitle(final String title) {
        this.setAttribute(ATTRIBUTE_TITLE, title);
    }

    public void setJobTitle(final String jobTitle) {
        this.setAttribute(ATTRIBUTE_JOB_TITLE, jobTitle);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPLOYS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String DEPLOY_PROFESSIONAL_DATA = "professional_data";

    public static final String DEPLOY_PERSONAL_DATA = "personnal_data";

    public ProfessionalContactDataItem getProfessionalData() {
        return new ProfessionalContactDataItem(getDeploy(DEPLOY_PROFESSIONAL_DATA));
    }

    public PersonalContactDataItem getPersonalData() {
        return new PersonalContactDataItem(getDeploy(DEPLOY_PERSONAL_DATA));
    }

    public UserItem getManager() {
        return new UserItem(getDeploy(ATTRIBUTE_MANAGER_ID));
    }

    @Override
    public UserItem getCreatedByUser() {
        return new UserItem(getDeploy(ATTRIBUTE_CREATED_BY_USER_ID));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemDefinition<UserItem> getItemDefinition() {
        return new UserDefinition();
    }

}
