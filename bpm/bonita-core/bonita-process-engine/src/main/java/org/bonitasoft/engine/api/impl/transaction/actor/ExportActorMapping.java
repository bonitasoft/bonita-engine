/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.api.impl.transaction.actor;

import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.bpm.bar.ActorMappingMarshaller;
import org.bonitasoft.engine.bpm.bar.XmlMarshallException;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.SGroupNotFoundException;
import org.bonitasoft.engine.identity.SRoleNotFoundException;
import org.bonitasoft.engine.identity.SUserNotFoundException;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Matthieu Chaffotte
 */
public class ExportActorMapping implements TransactionContentWithResult<String> {

    private final ActorMappingService actorMappingService;

    private final IdentityService identityService;

    private final long processDefinitionId;

    private String xmlContent;

    public ExportActorMapping(final ActorMappingService actorMappingService, final IdentityService identityService,
            final long processDefinitionId) {
        super();
        this.actorMappingService = actorMappingService;
        this.identityService = identityService;
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public void execute() throws SBonitaException {
        final ActorMapping mapping = getActorMapping();
        ActorMappingMarshaller marshaller = new ActorMappingMarshaller();

        try {
            xmlContent = new String(marshaller.serializeToXML(mapping));
        } catch (XmlMarshallException e) {
            throw new SBonitaReadException("Failed to generate xml from actorMapping", e);
        }
    }

    public ActorMapping getActorMapping() throws SBonitaException {
        final ActorMapping actorMapping = new ActorMapping();
        QueryOptions queryOptions = new QueryOptions(0, 100, SActor.class, "id", OrderByType.ASC);
        List<SActor> actors = actorMappingService.getActors(processDefinitionId, queryOptions);
        while (!actors.isEmpty()) {
            for (final SActor sActor : actors) {
                final Actor actor = new Actor(sActor.getName());
                final List<SActorMember> actorMembers = actorMappingService.getActorMembers(sActor.getId(), 0, Integer.MAX_VALUE);
                for (final SActorMember sActorMember : actorMembers) {
                    addUser(actor, sActorMember);
                    addGroup(actor, sActorMember);
                    addRole(actor, sActorMember);
                    addMembership(actor, sActorMember);
                }
                actorMapping.addActor(actor);
            }
            queryOptions = QueryOptions.getNextPage(queryOptions);
            actors = actorMappingService.getActors(processDefinitionId, queryOptions);
        }
        return actorMapping;
    }

    private void addUser(final Actor actor, final SActorMember sActorMember) throws SUserNotFoundException {
        if (sActorMember.getUserId() > 0) {
            final SUser user = identityService.getUser(sActorMember.getUserId());
            actor.addUser(user.getUserName());
        }
    }

    private void addGroup(final Actor actor, final SActorMember sActorMember) throws SGroupNotFoundException {
        if (sActorMember.getGroupId() > 0 && sActorMember.getRoleId() <= 0) {
            final SGroup group = identityService.getGroup(sActorMember.getGroupId());
            actor.addGroup(group.getPath());
        }
    }

    private void addRole(final Actor actor, final SActorMember sActorMember) throws SRoleNotFoundException {
        if (sActorMember.getRoleId() > 0 && sActorMember.getGroupId() <= 0) {
            final SRole role = identityService.getRole(sActorMember.getRoleId());
            actor.addRole(role.getName());
        }
    }

    private void addMembership(final Actor actor, final SActorMember sActorMember) throws SRoleNotFoundException, SGroupNotFoundException {
        if (sActorMember.getRoleId() > 0 && sActorMember.getGroupId() > 0) {
            final SRole role = identityService.getRole(sActorMember.getRoleId());
            final SGroup group = identityService.getGroup(sActorMember.getGroupId());
            actor.addMembership(group.getPath(), role.getName());
        }
    }

    @Override
    public String getResult() {
        return xmlContent;
    }

}
