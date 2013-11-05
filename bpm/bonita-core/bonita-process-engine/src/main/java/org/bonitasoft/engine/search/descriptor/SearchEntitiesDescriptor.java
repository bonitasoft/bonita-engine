/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.search.descriptor;


/**
 * @author Matthieu Chaffotte
 * @author Zhang Bole
 * @author Yanyan Liu
 */
public class SearchEntitiesDescriptor {

    private final SearchUserDescriptor userDescriptor;

    private final SearchRoleDescriptor roleDescriptor;

    private final SearchGroupDescriptor groupDescriptor;

    private final SearchProcessInstanceDescriptor processInstanceDescriptor;

    private final SearchArchivedProcessInstancesDescriptor archivedProcessInstanceDescriptor;

    private final SearchHumanTaskInstanceDescriptor humanTaskInstanceDescriptor;

    private final SearchArchivedHumanTaskInstanceDescriptor archivedHumanTaskInstanceDescriptor;

    private final SearchProcessDefinitionsDescriptor searchProcessDefinitionsDescriptor;

    private final SearchCommentDescriptor commentDescriptor;

    private final SearchDocumentDescriptor documentDescriptor;

    private final SearchEntityMemberUserDescriptor entityMemberUserDescriptor;

    private final SearchArchivedDocumentDescriptor archivedDocumentDescriptor;

    private final SearchActivityInstanceDescriptor activityInstanceDescriptor;

    private final SearchFlowNodeInstanceDescriptor flowNodeInstanceDescriptor;

    private final SearchArchivedActivityInstanceDescriptor archivedActivityInstanceDescriptor;

    private final SearchArchivedCommentsDescriptor searchArchivedCommentsDescriptor;

    private final SearchArchivedConnectorInstanceDescriptor searchArchivedConnectorInstanceDescriptor;

    private final SearchCommandDescriptor searchCommandDescriptor;

    private final SearchArchivedFlowNodeInstanceDescriptor archivedFlowNodeInstanceDescriptor;

    private final SearchConnectorInstanceDescriptor connectorInstanceDescriptor;

    private final SearchProfileDescriptor searchProfileDescriptor;

    private final SearchProfileEntryDescriptor searchProfileEntryDescriptor;

    private final SearchProfileMemberUserDescriptor profileMemberUserDescriptor;

    private final SearchProfileMemberGroupDescriptor profileMemberGroupDescriptor;

    private final SearchProfileMemberRoleDescriptor profileMemberRoleDescriptor;

    private final SearchProfileMemberRoleAndGroupDescriptor profileMemberRoleAndGroupDescriptor;

    public SearchEntitiesDescriptor() {
        userDescriptor = new SearchUserDescriptor();
        roleDescriptor = new SearchRoleDescriptor();
        groupDescriptor = new SearchGroupDescriptor();
        processInstanceDescriptor = new SearchProcessInstanceDescriptor();
        archivedProcessInstanceDescriptor = new SearchArchivedProcessInstancesDescriptor();
        humanTaskInstanceDescriptor = new SearchHumanTaskInstanceDescriptor();
        archivedHumanTaskInstanceDescriptor = new SearchArchivedHumanTaskInstanceDescriptor();
        searchProcessDefinitionsDescriptor = new SearchProcessDefinitionsDescriptor();
        commentDescriptor = new SearchCommentDescriptor();
        connectorInstanceDescriptor = new SearchConnectorInstanceDescriptor();
        documentDescriptor = new SearchDocumentDescriptor();
        entityMemberUserDescriptor = new SearchEntityMemberUserDescriptor();
        archivedDocumentDescriptor = new SearchArchivedDocumentDescriptor();
        activityInstanceDescriptor = new SearchActivityInstanceDescriptor();
        archivedActivityInstanceDescriptor = new SearchArchivedActivityInstanceDescriptor();
        searchArchivedCommentsDescriptor = new SearchArchivedCommentsDescriptor();
        searchArchivedConnectorInstanceDescriptor = new SearchArchivedConnectorInstanceDescriptor();
        flowNodeInstanceDescriptor = new SearchFlowNodeInstanceDescriptor();
        searchCommandDescriptor = new SearchCommandDescriptor();
        archivedFlowNodeInstanceDescriptor = new SearchArchivedFlowNodeInstanceDescriptor();

        searchProfileDescriptor = new SearchProfileDescriptor();
        searchProfileEntryDescriptor = new SearchProfileEntryDescriptor();
        profileMemberUserDescriptor = new SearchProfileMemberUserDescriptor();
        profileMemberGroupDescriptor = new SearchProfileMemberGroupDescriptor();
        profileMemberRoleDescriptor = new SearchProfileMemberRoleDescriptor();
        profileMemberRoleAndGroupDescriptor = new SearchProfileMemberRoleAndGroupDescriptor();
    }

    public SearchUserDescriptor getUserDescriptor() {
        return userDescriptor;
    }

    public SearchRoleDescriptor getRoleDescriptor() {
        return roleDescriptor;
    }

    public SearchHumanTaskInstanceDescriptor getHumanTaskInstanceDescriptor() {
        return humanTaskInstanceDescriptor;
    }

    public SearchArchivedHumanTaskInstanceDescriptor getArchivedHumanTaskInstanceDescriptor() {
        return archivedHumanTaskInstanceDescriptor;
    }

    public SearchGroupDescriptor getGroupDescriptor() {
        return groupDescriptor;
    }

    public SearchProcessInstanceDescriptor getProcessInstanceDescriptor() {
        return processInstanceDescriptor;
    }

    public SearchArchivedProcessInstancesDescriptor getArchivedProcessInstancesDescriptor() {
        return archivedProcessInstanceDescriptor;
    }

    public SearchProcessDefinitionsDescriptor getProcessDefinitionsDescriptor() {
        return searchProcessDefinitionsDescriptor;
    }

    public SearchCommentDescriptor getCommentDescriptor() {
        return commentDescriptor;
    }

    public SearchConnectorInstanceDescriptor getConnectorInstanceDescriptor() {
        return connectorInstanceDescriptor;
    }

    public SearchDocumentDescriptor getDocumentDescriptor() {
        return documentDescriptor;
    }

    public SearchEntityMemberUserDescriptor getEntityMemberUserDescriptor() {
        return entityMemberUserDescriptor;
    }

    public SearchArchivedDocumentDescriptor getArchivedDocumentDescriptor() {
        return archivedDocumentDescriptor;
    }

    public SearchActivityInstanceDescriptor getActivityInstanceDescriptor() {
        return activityInstanceDescriptor;
    }

    public SearchArchivedActivityInstanceDescriptor getArchivedActivityInstanceDescriptor() {
        return archivedActivityInstanceDescriptor;
    }

    public SearchArchivedCommentsDescriptor getArchivedCommentsDescriptor() {
        return searchArchivedCommentsDescriptor;
    }

    public SearchArchivedConnectorInstanceDescriptor getArchivedConnectorInstanceDescriptor() {
        return searchArchivedConnectorInstanceDescriptor;
    }

    public SearchFlowNodeInstanceDescriptor getFlowNodeInstanceDescriptor() {
        return flowNodeInstanceDescriptor;
    }

    public SearchCommandDescriptor getCommandDescriptor() {
        return searchCommandDescriptor;
    }

    public SearchArchivedFlowNodeInstanceDescriptor getArchivedFlowNodeInstanceDescriptor() {
        return archivedFlowNodeInstanceDescriptor;
    }

    public SearchProfileDescriptor getProfileDescriptor() {
        return searchProfileDescriptor;
    }

    public SearchProfileEntryDescriptor getProfileEntryDescriptor() {
        return searchProfileEntryDescriptor;
    }

    public SearchProfileMemberUserDescriptor getProfileMemberUserDescriptor() {
        return profileMemberUserDescriptor;
    }

    public SearchProfileMemberGroupDescriptor getProfileMemberGroupDescriptor() {
        return profileMemberGroupDescriptor;
    }

    public SearchProfileMemberRoleDescriptor getProfileMemberRoleDescriptor() {
        return profileMemberRoleDescriptor;
    }

    public SearchProfileMemberRoleAndGroupDescriptor getProfileMemberRoleAndGroupDescriptor() {
        return profileMemberRoleAndGroupDescriptor;
    }

}
