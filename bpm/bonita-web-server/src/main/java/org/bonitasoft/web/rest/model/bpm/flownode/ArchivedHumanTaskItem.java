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

import java.util.Date;

import org.bonitasoft.web.rest.model.bpm.process.ActorItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public class ArchivedHumanTaskItem extends ArchivedTaskItem implements IHumanTaskItem {

    public ArchivedHumanTaskItem() {
        super();
    }

    public ArchivedHumanTaskItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // SETTERS

    @Override
    public void setAssignedId(final String id) {
        setAttribute(ATTRIBUTE_ASSIGNED_USER_ID, id);
    }

    @Override
    public void setAssignedId(final APIID id) {
        setAttribute(ATTRIBUTE_ASSIGNED_USER_ID, id);
    }

    @Override
    public void setAssignedId(final Long id) {
        setAttribute(ATTRIBUTE_ASSIGNED_USER_ID, id);
    }

    @Override
    public void setAssignedDate(final String date) {
        setAttribute(ATTRIBUTE_ASSIGNED_DATE, date);
    }

    @Override
    public void setAssignedDate(final Date date) {
        setAttribute(ATTRIBUTE_ASSIGNED_DATE, date);
    }

    @Override
    public void setPriority(final String priority) {
        setAttribute(ATTRIBUTE_PRIORITY, priority);
    }

    @Override
    public void setDueDate(final String date) {
        setAttribute(ATTRIBUTE_DUE_DATE, date);
    }

    @Override
    public void setDueDate(final Date date) {
        setAttribute(ATTRIBUTE_DUE_DATE, date);
    }

    @Override
    public void setActorId(final APIID id) {
        setAttribute(ATTRIBUTE_ACTOR_ID, id);
    }

    @Override
    public void setActorId(final String actorId) {
        setAttribute(ATTRIBUTE_ACTOR_ID, actorId);
    }

    @Override
    public void setActorId(final Long actorId) {
        setAttribute(ATTRIBUTE_ACTOR_ID, actorId);
    }

    // Counters
    @Override
    public void setNbOfAttachment(final String count) {
        setAttribute(COUNT_ATTACHMENT_NUMBER, count);
    }

    @Override
    public void setNbOfAttachment(final int count) {
        setAttribute(COUNT_ATTACHMENT_NUMBER, count);
    }

    @Override
    public void setNbOfComment(final String count) {
        setAttribute(COUNT_COMMENT_NUMBER, count);
    }

    @Override
    public void setNbOfComment(final int count) {
        setAttribute(COUNT_COMMENT_NUMBER, count);
    }

    @Override
    public void setNbOfActorUser(final String count) {
        setAttribute(COUNT_ACTOR_USER_NUMBER, count);
    }

    @Override
    public void setNbOfActorUser(final int count) {
        setAttribute(COUNT_ACTOR_USER_NUMBER, count);
    }

    // GETTERS

    @Override
    public final APIID getActorId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_ACTOR_ID);
    }

    @Override
    public String getPriority() {
        return this.getAttributeValue(ATTRIBUTE_PRIORITY);
    }

    @Override
    public String getDueDate() {
        return this.getAttributeValue(ATTRIBUTE_DUE_DATE);
    }

    @Override
    public APIID getAssignedId() {
        return this.getAttributeValueAsAPIID(ATTRIBUTE_ASSIGNED_USER_ID);
    }

    @Override
    public String getAssignedDate() {
        return this.getAttributeValue(ATTRIBUTE_ASSIGNED_DATE);
    }

    // Counters
    @Override
    public Integer getNbOfAttachment() {
        return Integer.parseInt(this.getAttributeValue(COUNT_ATTACHMENT_NUMBER));
    }

    @Override
    public Integer getNbOfComment() {
        return Integer.parseInt(this.getAttributeValue(COUNT_COMMENT_NUMBER));
    }

    @Override
    public Integer getNbOfActorUser() {
        return Integer.parseInt(this.getAttributeValue(COUNT_ACTOR_USER_NUMBER));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPLOYS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public UserItem getAssignedUser() {
        return new UserItem(getDeploy(ATTRIBUTE_ASSIGNED_USER_ID));
    }

    @Override
    public final ActorItem getActor() {
        return new ActorItem(getDeploy(ATTRIBUTE_ACTOR_ID));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemDefinition getItemDefinition() {
        return new HumanTaskDefinition();
    }

    @Override
    public boolean isAssigned() {
        return getAssignedId() != null;
    }

    @Override
    public boolean isUnassigned() {
        return !isAssigned();
    }

}
