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
package org.bonitasoft.web.rest.model.bpm.cases;

import java.util.Date;

import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasLastUpdateDate;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * process instance item
 *
 * @author Haojie Yuan
 * @author Celine Souchet
 */
public class CaseItem extends Item implements ItemHasLastUpdateDate, ItemHasUniqueId {

    public static final String ATTRIBUTE_VARIABLES = "variables";

    public static final String ATTRIBUTE_STATE = "state";

    public static final String ATTRIBUTE_PROCESS_ID = "processDefinitionId";

    public static final String ATTRIBUTE_PROCESS_NAME = "name";

    public static final String ATTRIBUTE_ROOT_CASE_ID = "rootCaseId";

    public static final String ATTRIBUTE_STARTED_BY_USER_ID = "started_by";

    public static final String ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID = "startedBySubstitute";

    public static final String ATTRIBUTE_START_DATE = "start";

    public static final String ATTRIBUTE_END_DATE = "end_date";

    public static final String COUNTER_FAILED_FLOW_NODES = "failedFlowNodes";

    public static final String COUNTER_ACTIVE_FLOW_NODES = "activeFlowNodes";

    public static final String ATTRIBUTE_SEARCH_INDEX_1_LABEL = "searchIndex1Label";
    public static final String ATTRIBUTE_SEARCH_INDEX_1_VALUE = "searchIndex1Value";
    public static final String ATTRIBUTE_SEARCH_INDEX_2_LABEL = "searchIndex2Label";
    public static final String ATTRIBUTE_SEARCH_INDEX_2_VALUE = "searchIndex2Value";
    public static final String ATTRIBUTE_SEARCH_INDEX_3_LABEL = "searchIndex3Label";
    public static final String ATTRIBUTE_SEARCH_INDEX_3_VALUE = "searchIndex3Value";
    public static final String ATTRIBUTE_SEARCH_INDEX_4_LABEL = "searchIndex4Label";
    public static final String ATTRIBUTE_SEARCH_INDEX_4_VALUE = "searchIndex4Value";
    public static final String ATTRIBUTE_SEARCH_INDEX_5_LABEL = "searchIndex5Label";
    public static final String ATTRIBUTE_SEARCH_INDEX_5_VALUE = "searchIndex5Value";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES VALUES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // see ProcessInstanceState enum
    public static final String VALUE_STATE_INITIALIZING = "0";

    public static final String VALUE_STATE_STARTED = "1";

    public static final String VALUE_STATE_SUSPENDED = "2";

    public static final String VALUE_STATE_CANCELLED = "3";

    public static final String VALUE_STATE_ABORTED = "4";

    public static final String VALUE_STATE_COMPLETING = "5";

    public static final String VALUE_STATE_COMPLETED = "6";

    public static final String VALUE_STATE_ERROR = "7";

    public static final String VALUE_STATE_TO_MIGRATE = "8";

    public static final String VALUE_STATE_READY_FOR_MIGRATION = "9";

    public static final String VALUE_STATE_MIGRATING = "10";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String FILTER_USER_ID = "user_id";

    public static final String FILTER_SUPERVISOR_ID = "supervisor_id";

    public static final String FILTER_TEAM_MANAGER_ID = "team_manager_id";

    public static final String FILTER_CALLER = "caller";

    public static final String FILTER_STATE = "state";

    public CaseItem() {
        super();
    }

    public CaseItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // DEPLOYS

    public UserItem getStartedByUser() {
        return new UserItem(getDeploy(ATTRIBUTE_STARTED_BY_USER_ID));
    }

    public UserItem getStartedBySubstituteUser() {
        return new UserItem(getDeploy(ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID));
    }

    public ProcessItem getProcess() {
        return new ProcessItem(getDeploy(ATTRIBUTE_PROCESS_ID));
    }

    // GETTERS

    @Override
    public Date getLastUpdateDate() {
        return getAttributeValueAsDate(ATTRIBUTE_LAST_UPDATE_DATE);
    }

    public String getState() {
        return getAttributeValue(ATTRIBUTE_STATE);
    }

    public Date getStartDate() {
        return getAttributeValueAsDate(ATTRIBUTE_START_DATE);
    }

    public APIID getStartedByUserId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_STARTED_BY_USER_ID);
    }

    public APIID getStartedBySubstituteUserId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID);
    }

    public Date getEndDate() {
        return getAttributeValueAsDate(ATTRIBUTE_END_DATE);
    }

    public APIID getProcessId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_PROCESS_ID);
    }

    public APIID getRootCaseId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_ROOT_CASE_ID);
    }

    public String getSearchIndex1Label() {
        return getAttributeValue(ATTRIBUTE_SEARCH_INDEX_1_LABEL);
    }

    public String getSearchIndex1Value() {
        return getAttributeValue(ATTRIBUTE_SEARCH_INDEX_1_VALUE);
    }

    public String getSearchIndex2Label() {
        return getAttributeValue(ATTRIBUTE_SEARCH_INDEX_2_LABEL);
    }

    public String getSearchIndex2Value() {
        return getAttributeValue(ATTRIBUTE_SEARCH_INDEX_2_VALUE);
    }

    public String getSearchIndex3Label() {
        return getAttributeValue(ATTRIBUTE_SEARCH_INDEX_3_LABEL);
    }

    public String getSearchIndex3Value() {
        return getAttributeValue(ATTRIBUTE_SEARCH_INDEX_3_VALUE);
    }

    public String getSearchIndex4Label() {
        return getAttributeValue(ATTRIBUTE_SEARCH_INDEX_4_LABEL);
    }

    public String getSearchIndex4Value() {
        return getAttributeValue(ATTRIBUTE_SEARCH_INDEX_4_VALUE);
    }

    public String getSearchIndex5Label() {
        return getAttributeValue(ATTRIBUTE_SEARCH_INDEX_5_LABEL);
    }

    public String getSearchIndex5Value() {
        return getAttributeValue(ATTRIBUTE_SEARCH_INDEX_5_VALUE);
    }

    // SETTERS

    @Override
    public void setId(final Long id) {
        setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public void setId(final String id) {
        setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public void setLastUpdateDate(final String date) {
        setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    @Override
    public void setLastUpdateDate(final Date date) {
        setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    public void setState(final String state) {
        setAttribute(ATTRIBUTE_STATE, state);
    }

    /**
     * @param date Must be SQL formated date
     */
    public void setStartDate(final String date) {
        setAttribute(ATTRIBUTE_START_DATE, date);
    }

    public void setStartDate(final Date date) {
        setAttribute(ATTRIBUTE_START_DATE, date);
    }

    public void setStartedByUserId(final Long userId) {
        setAttribute(ATTRIBUTE_STARTED_BY_USER_ID, userId);
    }

    public void setStartedByUserId(final APIID userId) {
        setAttribute(ATTRIBUTE_STARTED_BY_USER_ID, userId);
    }

    public void setStartedByUserId(final String userId) {
        setAttribute(ATTRIBUTE_STARTED_BY_USER_ID, userId);
    }

    public void setStartedBySubstituteUserId(final Long userId) {
        setAttribute(ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID, userId);
    }

    public void setStartedBySubstituteUserId(final APIID userId) {
        setAttribute(ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID, userId);
    }

    public void setStartedBySubstituteUserId(final String userId) {
        setAttribute(ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID, userId);
    }

    public void setEndDate(final String date) {
        setAttribute(ATTRIBUTE_END_DATE, date);
    }

    public void setEndDate(final Date date) {
        setAttribute(ATTRIBUTE_END_DATE, date);
    }

    public void setProcessId(final Long processId) {
        setProcessId(APIID.makeAPIID(processId));
    }

    public void setProcessId(final String processId) {
        setAttribute(ATTRIBUTE_PROCESS_ID, processId);
    }

    public void setProcessId(final APIID processId) {
        setAttribute(ATTRIBUTE_PROCESS_ID, processId);
    }

    public void setRootCaseId(final long rootCaseId) {
        setAttribute(ATTRIBUTE_ROOT_CASE_ID, rootCaseId);
    }

    public void setSearchIndex1Label(final String attributeSearchIndex1Label) {
        setAttribute(ATTRIBUTE_SEARCH_INDEX_1_LABEL, attributeSearchIndex1Label);
    }

    public void setSearchIndex1Value(final String attributeSearchIndex1Value) {
        setAttribute(ATTRIBUTE_SEARCH_INDEX_1_VALUE, attributeSearchIndex1Value);
    }

    public void setSearchIndex2Label(final String attributeSearchIndex2Label) {
        setAttribute(ATTRIBUTE_SEARCH_INDEX_2_LABEL, attributeSearchIndex2Label);
    }

    public void setSearchIndex2Value(final String attributeSearchIndex2Value) {
        setAttribute(ATTRIBUTE_SEARCH_INDEX_2_VALUE, attributeSearchIndex2Value);
    }

    public void setSearchIndex3Label(final String attributeSearchIndex3Label) {
        setAttribute(ATTRIBUTE_SEARCH_INDEX_3_LABEL, attributeSearchIndex3Label);
    }

    public void setSearchIndex3Value(final String attributeSearchIndex3Value) {
        setAttribute(ATTRIBUTE_SEARCH_INDEX_3_VALUE, attributeSearchIndex3Value);
    }

    public void setSearchIndex4Label(final String attributeSearchIndex4Label) {
        setAttribute(ATTRIBUTE_SEARCH_INDEX_4_LABEL, attributeSearchIndex4Label);
    }

    public void setSearchIndex4Value(final String attributeSearchIndex4Value) {
        setAttribute(ATTRIBUTE_SEARCH_INDEX_4_VALUE, attributeSearchIndex4Value);
    }

    public void setSearchIndex5Label(final String attributeSearchIndex5Label) {
        setAttribute(ATTRIBUTE_SEARCH_INDEX_5_LABEL, attributeSearchIndex5Label);
    }

    public void setSearchIndex5Value(final String attributeSearchIndex5Value) {
        setAttribute(ATTRIBUTE_SEARCH_INDEX_5_VALUE, attributeSearchIndex5Value);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // UTILS

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemDefinition getItemDefinition() {
        return new CaseDefinition();
    }

}
