/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.search;

import org.bonitasoft.engine.actor.privilege.model.builder.ActorPrivilegeBuilders;
import org.bonitasoft.engine.businesslogger.model.builder.SBusinessLogModelBuilder;
import org.bonitasoft.engine.command.model.SCommandBuilderAccessor;
import org.bonitasoft.engine.core.category.model.builder.SCategoryBuilderAccessor;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilders;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.document.mapping.model.builder.SDocumentMappingBuilderAccessor;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilders;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.privilege.model.buidler.PrivilegeBuilders;
import org.bonitasoft.engine.profile.model.SProfileBuilderAccessor;
import org.bonitasoft.engine.search.entitymember.SearchEntityMemberUserDescriptor;
import org.bonitasoft.engine.search.flownode.SearchArchivedFlowNodeInstanceDescriptor;
import org.bonitasoft.engine.search.flownode.SearchFlowNodeInstanceDescriptor;
import org.bonitasoft.engine.supervisor.mapping.model.SSupervisorBuilders;

/**
 * @author Matthieu Chaffotte
 * @author Zhang Bole
 * @author Yanyan Liu
 */
public final class SearchEntitiesDescriptor {

    private final SearchUserDescriptor userDescriptor;

    private final SearchRoleDescriptor roleDescriptor;

    private final SearchGroupDescriptor groupDescriptor;

    private final SearchPrivilegeDescriptor privilegeDescriptor;

    private final SearchActorPrivilegeDescriptor actorPrivilegeDescriptor;

    private final SearchProcessInstanceDescriptor processInstanceDescriptor;

    private final SearchArchivedProcessInstancesDescriptor archivedProcessInstanceDescriptor;

    private final SearchHumanTaskInstanceDescriptor humanTaskInstanceDescriptor;

    private final SearchArchivedHumanTaskInstanceDescriptor archivedHumanTaskInstanceDescriptor;

    private final SearchProcessDefinitionsDescriptor searchProcessDefinitionsDescriptor;

    private final SearchCommentDescriptor commentDescriptor;

    private final SearchProfileMemberUserDescriptor profileMemberUserDescriptor;

    private final SearchProfileMemberGroupDescriptor profileMemberGroupDescriptor;

    private final SearchProfileMemberRoleDescriptor profileMemberRoleDescriptor;

    private final SearchProfileMemberRoleAndGroupDescriptor profileMemberRoleAndGroupDescriptor;

    private final SearchLogDescriptor logDescriptor;

    private final SearchDocumentDescriptor documentDescriptor;

    private final SearchEntityMemberUserDescriptor entityMemberUserDescriptor;

    private final SearchArchivedDocumentDescriptor archivedDocumentDescriptor;

    private final SearchActivityInstanceDescriptor activityInstanceDescriptor;

    private final SearchFlowNodeInstanceDescriptor flowNodeInstanceDescriptor;

    private final SearchArchivedActivityInstanceDescriptor archivedActivityInstanceDescriptor;

    private final SearchArchivedCommentsDescriptor searchArchivedCommentsDescriptor;

    private final SearchCommandDescriptor searchCommandDescriptor;

    private final SearchArchivedFlowNodeInstanceDescriptor archivedFlowNodeInstanceDescriptor;

    public SearchEntitiesDescriptor(final IdentityModelBuilder identityModelBuilder, final PrivilegeBuilders privilegeBuilders,
            final ActorPrivilegeBuilders actorPrivilegeBuilders, final BPMInstanceBuilders bpmInstanceBuilders,
            final FlowNodeStateManager flowNodeStateManager, final SSupervisorBuilders sSupervisorBuilders, final BPMDefinitionBuilders definitionBuilders,
            final SProfileBuilderAccessor sProfileBuilderAccessor, final SCommentBuilders commentBuilders,
            final SCategoryBuilderAccessor categoryBuilderAccessor, final SBusinessLogModelBuilder sBusinessLogModelBuilder,
            final SDocumentMappingBuilderAccessor sDocumentMappingBuilderAccessor, final SExternalIdentityMappingBuilders sExternalIdentityMappingBuilders,
            final SCommandBuilderAccessor commandBuilderAccessor) {
        userDescriptor = new SearchUserDescriptor(identityModelBuilder);
        roleDescriptor = new SearchRoleDescriptor(identityModelBuilder);
        groupDescriptor = new SearchGroupDescriptor(identityModelBuilder);
        privilegeDescriptor = new SearchPrivilegeDescriptor(privilegeBuilders);
        actorPrivilegeDescriptor = new SearchActorPrivilegeDescriptor(actorPrivilegeBuilders);
        processInstanceDescriptor = new SearchProcessInstanceDescriptor(bpmInstanceBuilders, sSupervisorBuilders);
        archivedProcessInstanceDescriptor = new SearchArchivedProcessInstancesDescriptor(bpmInstanceBuilders, sSupervisorBuilders);
        humanTaskInstanceDescriptor = new SearchHumanTaskInstanceDescriptor(bpmInstanceBuilders, sSupervisorBuilders);
        archivedHumanTaskInstanceDescriptor = new SearchArchivedHumanTaskInstanceDescriptor(bpmInstanceBuilders, flowNodeStateManager);
        searchProcessDefinitionsDescriptor = new SearchProcessDefinitionsDescriptor(definitionBuilders, categoryBuilderAccessor);
        commentDescriptor = new SearchCommentDescriptor(commentBuilders, identityModelBuilder.getUserBuilder());
        profileMemberUserDescriptor = new SearchProfileMemberUserDescriptor(sProfileBuilderAccessor.getSProfileMemberBuilder(),
                identityModelBuilder.getUserBuilder());
        profileMemberGroupDescriptor = new SearchProfileMemberGroupDescriptor(sProfileBuilderAccessor.getSProfileMemberBuilder(), identityModelBuilder);
        profileMemberRoleDescriptor = new SearchProfileMemberRoleDescriptor(sProfileBuilderAccessor.getSProfileMemberBuilder(),
                identityModelBuilder.getRoleBuilder());
        profileMemberRoleAndGroupDescriptor = new SearchProfileMemberRoleAndGroupDescriptor(sProfileBuilderAccessor.getSProfileMemberBuilder(),
                identityModelBuilder);
        logDescriptor = new SearchLogDescriptor(sBusinessLogModelBuilder.getBusinessLogBuilder());
        documentDescriptor = new SearchDocumentDescriptor(sDocumentMappingBuilderAccessor);
        entityMemberUserDescriptor = new SearchEntityMemberUserDescriptor(sExternalIdentityMappingBuilders, identityModelBuilder);
        archivedDocumentDescriptor = new SearchArchivedDocumentDescriptor(sDocumentMappingBuilderAccessor);
        activityInstanceDescriptor = new SearchActivityInstanceDescriptor(bpmInstanceBuilders, sSupervisorBuilders);
        archivedActivityInstanceDescriptor = new SearchArchivedActivityInstanceDescriptor(bpmInstanceBuilders, sSupervisorBuilders);
        searchArchivedCommentsDescriptor = new SearchArchivedCommentsDescriptor(bpmInstanceBuilders, commentBuilders, identityModelBuilder.getUserBuilder());
        flowNodeInstanceDescriptor = new SearchFlowNodeInstanceDescriptor(bpmInstanceBuilders, flowNodeStateManager);
        searchCommandDescriptor = new SearchCommandDescriptor(commandBuilderAccessor.getSCommandBuilder());
        archivedFlowNodeInstanceDescriptor = new SearchArchivedFlowNodeInstanceDescriptor(bpmInstanceBuilders, flowNodeStateManager);

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

    public SearchPrivilegeDescriptor getPrivilegeDescriptor() {
        return privilegeDescriptor;
    }

    public SearchActorPrivilegeDescriptor getActorPrivilegeDescriptor() {
        return actorPrivilegeDescriptor;
    }

    public SearchProcessInstanceDescriptor getProcessInstanceDescriptor() {
        return processInstanceDescriptor;
    }

    public SearchArchivedProcessInstancesDescriptor getArchivedProcessInstanceDescriptor() {
        return archivedProcessInstanceDescriptor;
    }

    public SearchProcessDefinitionsDescriptor getSearchProcessDefinitionsDescriptor() {
        return searchProcessDefinitionsDescriptor;
    }

    public SearchCommentDescriptor getCommentDescriptor() {
        return commentDescriptor;
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

    public SearchLogDescriptor getLogDescriptor() {
        return logDescriptor;
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

    public SearchArchivedCommentsDescriptor getSearchArchivedCommentsDescriptor() {
        return searchArchivedCommentsDescriptor;
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

}
