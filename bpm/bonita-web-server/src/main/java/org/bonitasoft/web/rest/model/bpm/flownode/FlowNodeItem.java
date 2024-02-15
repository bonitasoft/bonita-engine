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
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public class FlowNodeItem extends Item implements IFlowNodeItem {

    public FlowNodeItem() {
        super();
    }

    public FlowNodeItem(final IItem item) {
        super(item);
    }

    @Override
    public final void setDescription(final String description) {
        this.setAttribute(ATTRIBUTE_DESCRIPTION, description);
    }

    @Override
    public final void setDisplayDescription(final String displayDescription) {
        this.setAttribute(ATTRIBUTE_DISPLAY_DESCRIPTION, displayDescription);
    }

    @Override
    public final String getDescription() {
        return getAttributeValue(ATTRIBUTE_DESCRIPTION);
    }

    @Override
    public final String getDisplayDescription() {
        return getAttributeValue(ATTRIBUTE_DISPLAY_DESCRIPTION);
    }

    @Override
    public final void setName(final String name) {
        this.setAttribute(ATTRIBUTE_NAME, name);
    }

    @Override
    public final void setDisplayName(final String displayName) {
        this.setAttribute(ATTRIBUTE_DISPLAY_NAME, displayName);
    }

    @Override
    public final String getName() {
        return getAttributeValue(ATTRIBUTE_NAME);
    }

    @Override
    public final String getDisplayName() {
        return getAttributeValue(ATTRIBUTE_DISPLAY_NAME);
    }

    @Override
    public final void setId(final String id) {
        this.setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public final void setId(final Long id) {
        this.setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public final void setProcessId(final APIID id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    @Override
    public final void setProcessId(final String id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    @Override
    public final void setProcessId(final Long id) {
        setAttribute(ATTRIBUTE_PROCESS_ID, id);
    }

    @Override
    public final APIID getProcessId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_PROCESS_ID);
    }

    @Override
    @Deprecated
    public final void setCaseId(final APIID id) {
        setRootCaseId(id);
    }

    @Override
    @Deprecated
    public final void setCaseId(final String id) {
        setRootCaseId(id);
    }

    @Override
    @Deprecated
    public final void setCaseId(final Long id) {
        setRootCaseId(id);
    }

    @Override
    @Deprecated
    public final APIID getCaseId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_CASE_ID);
    }

    @Override
    public void setRootCaseId(final APIID id) {
        setAttribute(ATTRIBUTE_ROOT_CASE_ID, id);
        setAttribute(ATTRIBUTE_CASE_ID, id);
    }

    @Override
    public void setRootCaseId(final String id) {
        setAttribute(ATTRIBUTE_ROOT_CASE_ID, id);
        setAttribute(ATTRIBUTE_CASE_ID, id);
    }

    @Override
    public void setRootCaseId(final Long id) {
        setAttribute(ATTRIBUTE_ROOT_CASE_ID, id);
        setAttribute(ATTRIBUTE_CASE_ID, id);
    }

    @Override
    public APIID getRootCaseId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_ROOT_CASE_ID);
    }

    @Override
    public void setParentCaseId(final APIID id) {
        setAttribute(ATTRIBUTE_PARENT_CASE_ID, id);
    }

    @Override
    public void setParentCaseId(final String id) {
        setAttribute(ATTRIBUTE_PARENT_CASE_ID, id);
    }

    @Override
    public void setParentCaseId(final Long id) {
        setAttribute(ATTRIBUTE_PARENT_CASE_ID, id);
    }

    @Override
    public APIID getParentCaseId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_PARENT_CASE_ID);
    }

    @Override
    public final void setRootContainerId(final APIID rootContainerId) {
        setAttribute(ATTRIBUTE_ROOT_CONTAINER_ID, rootContainerId);
    }

    @Override
    public final void setRootContainerId(final String rootContainerId) {
        setAttribute(ATTRIBUTE_ROOT_CONTAINER_ID, rootContainerId);
    }

    @Override
    public final void setRootContainerId(final Long rootContainerId) {
        setAttribute(ATTRIBUTE_ROOT_CONTAINER_ID, rootContainerId);
    }

    @Override
    public APIID getRootContainerId() {
        return this.getAttributeValueAsAPIID(ATTRIBUTE_ROOT_CONTAINER_ID);
    }

    @Override
    public final void setState(final String state) {
        setAttribute(ATTRIBUTE_STATE, state);
    }

    @Override
    public final String getState() {
        return getAttributeValue(ATTRIBUTE_STATE);
    }

    @Override
    public final void setType(final String type) {
        setAttribute(ATTRIBUTE_TYPE, type);
    }

    @Override
    public final String getType() {
        return getAttributeValue(ATTRIBUTE_TYPE);
    }

    @Override
    public final void setExecutedByUserId(final APIID id) {
        setAttribute(ATTRIBUTE_EXECUTED_BY_USER_ID, id);
    }

    @Override
    public final void setExecutedByUserId(final String id) {
        setAttribute(ATTRIBUTE_EXECUTED_BY_USER_ID, id);
    }

    @Override
    public final void setExecutedByUserId(final Long id) {
        setAttribute(ATTRIBUTE_EXECUTED_BY_USER_ID, id);
    }

    @Override
    public final APIID getExecutedByUserId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_EXECUTED_BY_USER_ID);
    }

    @Override
    public final UserItem getExecutedByUser() {
        return new UserItem(getDeploy(ATTRIBUTE_EXECUTED_BY_USER_ID));
    }

    @Override
    public void setExecutedBySubstituteUserId(final APIID id) {
        setAttribute(ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID, id);
    }

    @Override
    public void setExecutedBySubstituteUserId(final String id) {
        setAttribute(ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID, id);
    }

    @Override
    public void setExecutedBySubstituteUserId(final Long id) {
        setAttribute(ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID, id);
    }

    @Override
    public APIID getExecutedBySubstituteUserId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID);
    }

    @Override
    public UserItem getExecutedBySubstituteUser() {
        return new UserItem(getDeploy(ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID));
    }

    @Override
    public final ProcessItem getProcess() {
        return new ProcessItem(getDeploy(ATTRIBUTE_PROCESS_ID));
    }

    @Override
    @Deprecated
    public final CaseItem getCase() {
        return new CaseItem(getDeploy(ATTRIBUTE_CASE_ID));
    }

    @Override
    public final CaseItem getRootCase() {
        return new CaseItem(getDeploy(ATTRIBUTE_ROOT_CASE_ID));
    }

    @Override
    public final CaseItem getParentCase() {
        return new CaseItem(getDeploy(ATTRIBUTE_PARENT_CASE_ID));
    }

    @Override
    public final ProcessItem getRootContainerProcess() {
        return new ProcessItem(getDeploy(ATTRIBUTE_ROOT_CONTAINER_ID));
    }

    @Override
    public ItemDefinition getItemDefinition() {
        return FlowNodeDefinition.get();
    }

}
