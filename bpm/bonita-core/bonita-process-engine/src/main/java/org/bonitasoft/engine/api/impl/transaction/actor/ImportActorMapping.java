
/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
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
 ******************************************************************************/

package org.bonitasoft.engine.api.impl.transaction.actor;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.SActorMemberAlreadyExistsException;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.actor.xml.Actor;
import org.bonitasoft.engine.actor.xml.ActorMapping;
import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.xml.sax.SAXException;

/**
 * @author Matthieu Chaffotte
 */
public class ImportActorMapping implements TransactionContent {

    private final ActorMappingService actorMappingService;

    private final IdentityService identityService;

    private final long processDefinitionId;

    private final String xmlContent;

    public ImportActorMapping(final ActorMappingService actorMappingService, final IdentityService identityService,
            final long processDefinitionId, final String xmlContent) {
        super();
        this.actorMappingService = actorMappingService;
        this.identityService = identityService;
        this.processDefinitionId = processDefinitionId;
        this.xmlContent = xmlContent;
    }

    @Override
    public void execute() throws SBonitaException {
        final ActorMapping actorMapping = getActorMappingFromXML();
        final List<Actor> actors = actorMapping.getActors();
        for (final Actor actor : actors) {
            final SActor sActor = actorMappingService.getActor(actor.getName(), processDefinitionId);
            final long actorId = sActor.getId();
            final Set<String> userNames = actor.getUsers();
            for (final String userName : userNames) {
                final SUser user = identityService.getUserByUserName(userName);
                checkAlreadyExistingUserMapping(actorId, user.getId());
                actorMappingService.addUserToActor(actorId, user.getId());
            }
            final Set<String> roleNames = actor.getRoles();
            for (final String roleName : roleNames) {
                final SRole role = identityService.getRoleByName(roleName);
                checkAlreadyExistingRoleMapping(actorId, role.getId());
                actorMappingService.addRoleToActor(actorId, role.getId());
            }
            final Set<String> groupPaths = actor.getGroups();
            for (final String groupPath : groupPaths) {
                final SGroup group = identityService.getGroupByPath(groupPath);
                checkAlreadyExistingGroupMapping(actorId, group.getId());
                actorMappingService.addGroupToActor(actorId, group.getId());
            }
            final Set<BEntry<String, String>> memberships = actor.getMemberships();
            for (final BEntry<String, String> membership : memberships) {
                final SGroup group = identityService.getGroupByPath(membership.getKey());
                final SRole role = identityService.getRoleByName(membership.getValue());
                checkAlreadyExistingMembershipMapping(actorId, group.getId(), role.getId());
                actorMappingService.addRoleAndGroupToActor(actorId, role.getId(), group.getId());
            }
        }
    }

    private void checkAlreadyExistingUserMapping(final long actorId, final long userId) throws SActorMemberAlreadyExistsException, SBonitaReadException {
        List<SActorMember> actorMembersOfUser;
        int startIndex = 0;
        do {
            actorMembersOfUser = actorMappingService.getActorMembers(actorId, startIndex, 50);
            for (final SActorMember sActorMember : actorMembersOfUser) {
                if (sActorMember.getUserId() == userId && sActorMember.getRoleId() == -1 && sActorMember.getGroupId() == -1) {
                    throw new SActorMemberAlreadyExistsException("This user / actor mapping already exists: actorId=" + actorId + ", userId=" + userId);
                }
            }
            startIndex += 50;
        } while (actorMembersOfUser.size() > 0);
    }

    private void checkAlreadyExistingGroupMapping(final long actorId, final long groupId) throws SActorMemberAlreadyExistsException, SBonitaReadException {
        List<SActorMember> actorMembersOfGroup;
        int startIndex = 0;
        do {
            actorMembersOfGroup = actorMappingService.getActorMembers(actorId, startIndex, 50);
            for (final SActorMember sActorMember : actorMembersOfGroup) {
                if (sActorMember.getGroupId() == groupId && sActorMember.getRoleId() == -1 && sActorMember.getUserId() == -1) {
                    throw new SActorMemberAlreadyExistsException("This group / actor mapping already exists: actorId=" + actorId + ", groupId=" + groupId);
                }
            }
            startIndex += 50;
        } while (actorMembersOfGroup.size() > 0);
    }

    private void checkAlreadyExistingRoleMapping(final long actorId, final long roleId) throws SActorMemberAlreadyExistsException, SBonitaReadException {
        List<SActorMember> actorMembersOfRole;
        int startIndex = 0;
        do {
            actorMembersOfRole = actorMappingService.getActorMembers(actorId, startIndex, 50);
            for (final SActorMember sActorMember : actorMembersOfRole) {
                if (sActorMember.getRoleId() == roleId && sActorMember.getGroupId() == -1 && sActorMember.getUserId() == -1) {
                    throw new SActorMemberAlreadyExistsException("This role / actor mapping already exists: actorId=" + actorId + ", roleId=" + roleId);
                }
            }
            startIndex += 50;
        } while (actorMembersOfRole.size() > 0);
    }

    private void checkAlreadyExistingMembershipMapping(final long actorId, final long groupId, final long roleId) throws SActorMemberAlreadyExistsException,
            SBonitaReadException {
        List<SActorMember> actorMembersOfMembership;
        int startIndex = 0;
        do {
            actorMembersOfMembership = actorMappingService.getActorMembers(actorId, startIndex, 50);
            for (final SActorMember sActorMember : actorMembersOfMembership) {
                if (sActorMember.getRoleId() == roleId && sActorMember.getGroupId() == groupId) {
                    throw new SActorMemberAlreadyExistsException("This membership / actor mapping already exists: actorId=" + actorId + ", groupId=" + groupId
                            + ", roleId=" + roleId);
                }
            }
            startIndex += 50;
        } while (actorMembersOfMembership.size() > 0);
    }

    ActorMapping getActorMappingFromXML() throws SBonitaException {
        byte[] b = xmlContent.getBytes();
        try {
            return IOUtil.unmarshallXMLtoObject(b, ActorMapping.class, ActorMapping.class.getResource("/actorMapping.xsd"));
        } catch (JAXBException | SAXException | IOException e) {
            throw new SBonitaReadException("Unable to read the actor mapping xml", e);
        }

    }

}
