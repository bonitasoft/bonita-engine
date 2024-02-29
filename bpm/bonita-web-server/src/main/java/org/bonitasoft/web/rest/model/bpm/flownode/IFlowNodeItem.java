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
package org.bonitasoft.web.rest.model.bpm.flownode;

import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualDescription;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Séverin Moussel
 */
public interface IFlowNodeItem extends IItem, ItemHasUniqueId, ItemHasDualName, ItemHasDualDescription {

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String ATTRIBUTE_PROCESS_ID = "processId";

    /**
     * @see IFlowNodeItem#ATTRIBUTE_ROOT_CASE_ID
     * @deprecated since 6.4.0
     */
    @Deprecated
    String ATTRIBUTE_CASE_ID = "caseId";

    /**
     * @since 6.4.0
     */
    String ATTRIBUTE_ROOT_CASE_ID = "rootCaseId";

    /**
     * @since 6.4.0
     */
    String ATTRIBUTE_PARENT_CASE_ID = "parentCaseId";

    String ATTRIBUTE_PARENT_ACTIVITY_INSTANCE_ID = "parentActivityInstanceId";

    String ATTRIBUTE_ROOT_CONTAINER_ID = "rootContainerId";

    String ATTRIBUTE_STATE = "state";

    String ATTRIBUTE_TYPE = "type";

    String ATTRIBUTE_EXECUTED_BY_USER_ID = "executedBy";

    String ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID = "executedBySubstitute";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES VALUES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    String VALUE_STATE_FAILED = "failed";

    String VALUE_STATE_READY = "ready";

    String VALUE_STATE_COMPLETED = "completed";

    String VALUE_STATE_SKIPPED = "skipped";

    // TODO replay is the SP feature only
    String VALUE_STATE_REPLAY = "replay";

    String VALUE_TYPE_AUTOMATIC_TASK = "AUTOMATIC_TASK";

    String VALUE_TYPE_USER_TASK = "USER_TASK";

    String VALUE_TYPE_MANUAL_TASK = "MANUAL_TASK";

    String VALUE_TYPE_CALL_ACTIVITY = "CALL_ACTIVITY";

    String VALUE_TYPE_LOOP_ACTIVITY = "LOOP_ACTIVITY";

    String VALUE_TYPE_MULTI_INSTANCE_ACTIVITY = "MULTI_INSTANCE_ACTIVITY";

    String VALUE_TYPE_SUB_PROCESS_ACTIVITY = "SUB_PROCESS_ACTIVITY";

    String VALUE_TYPE_GATEWAY = "GATEWAY";

    String VALUE_TYPE_START_EVENT = "EVENT";

    String VALUE_TYPE_INTERMEDIATE_CATCH_EVENT = "INTERMEDIATE_CATCH_EVENT";

    String VALUE_TYPE_BOUNDARY_EVENT = "BOUNDARY_EVENT";

    String VALUE_TYPE_INTERMEDIATE_THROW_EVENT = "";

    String VALUE_TYPE_END_EVENT = "END_EVENT";

    String FILTER_IS_FAILED = "isFailed";

    void setProcessId(final APIID id);

    void setProcessId(final String id);

    void setProcessId(final Long id);

    APIID getProcessId();

    /**
     * @param id
     * @see IFlowNodeItem#setRootCaseId(APIID)
     * @deprecated Since 6.4.0
     */
    @Deprecated
    void setCaseId(final APIID id);

    /**
     * @param id
     * @see IFlowNodeItem#setRootCaseId(String)
     * @deprecated Since 6.4.0
     */
    @Deprecated
    void setCaseId(final String id);

    /**
     * @param id
     * @see IFlowNodeItem#setRootCaseId(Long)
     * @deprecated Since 6.4.0
     */
    @Deprecated
    void setCaseId(final Long id);

    void setRootCaseId(final APIID id);

    void setRootCaseId(final String id);

    void setRootCaseId(final Long id);

    void setParentCaseId(final APIID id);

    void setParentCaseId(final String id);

    void setParentCaseId(final Long id);

    /**
     * @return
     * @Deprecated Since 6.4.0
     * @see IFlowNodeItem#getRootCaseId()
     */
    @Deprecated
    APIID getCaseId();

    APIID getRootCaseId();

    APIID getParentCaseId();

    void setRootContainerId(final APIID rootContainerId);

    void setRootContainerId(final String rootContainerId);

    void setRootContainerId(final Long rootContainerId);

    APIID getRootContainerId();

    void setState(final String state);

    String getState();

    void setType(final String type);

    String getType();

    void setExecutedByUserId(final APIID id);

    void setExecutedByUserId(final String id);

    void setExecutedByUserId(final Long id);

    APIID getExecutedByUserId();

    UserItem getExecutedByUser();

    void setExecutedBySubstituteUserId(final APIID id);

    void setExecutedBySubstituteUserId(final String id);

    void setExecutedBySubstituteUserId(final Long id);

    APIID getExecutedBySubstituteUserId();

    UserItem getExecutedBySubstituteUser();

    ProcessItem getProcess();

    /**
     * @return
     * @see IFlowNodeItem#getCase()
     * @deprecated since 6.4.0
     */
    @Deprecated
    CaseItem getCase();

    CaseItem getRootCase();

    CaseItem getParentCase();

    ProcessItem getRootContainerProcess();
}
