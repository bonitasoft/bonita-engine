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
package org.bonitasoft.engine.api.impl.transaction.actor;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.xml.Actor;
import org.bonitasoft.engine.actor.xml.ActorMapping;
import org.bonitasoft.engine.actor.xml.SActorMappingImportException;
import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.xml.Parser;

/**
 * @author Matthieu Chaffotte
 */
public class ImportActorMapping implements TransactionContent {

    private final ActorMappingService actorMappingService;

    private final IdentityService identityService;

    private final Parser parser;

    private final long processDefinitionId;

    private final String xmlContent;

    public ImportActorMapping(final ActorMappingService actorMappingService, final IdentityService identityService, final Parser parser,
            final long processDefinitionId, final String xmlContent) {
        super();
        this.actorMappingService = actorMappingService;
        this.identityService = identityService;
        this.parser = parser;
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
                actorMappingService.addUserToActor(actorId, user.getId());
            }
            final Set<String> roleNames = actor.getRoles();
            for (final String roleName : roleNames) {
                final SRole role = identityService.getRoleByName(roleName);
                actorMappingService.addRoleToActor(actorId, role.getId());
            }
            final Set<String> groupPaths = actor.getGroups();
            for (final String groupPath : groupPaths) {
                final SGroup group = identityService.getGroupByPath(groupPath);
                actorMappingService.addGroupToActor(actorId, group.getId());
            }
            final Set<BEntry<String, String>> memberships = actor.getMemberships();
            for (final BEntry<String, String> membership : memberships) {
                final SGroup group = identityService.getGroupByPath(membership.getKey());
                final SRole role = identityService.getRoleByName(membership.getValue());
                actorMappingService.addRoleAndGroupToActor(actorId, role.getId(), group.getId());
            }
        }
    }

    private ActorMapping getActorMappingFromXML() throws SBonitaException {
        StringReader reader = new StringReader(xmlContent);
        try {
            parser.validate(reader);
            reader.close();
            reader = new StringReader(xmlContent);
            return (ActorMapping) parser.getObjectFromXML(reader);
        } catch (final IOException ioe) {
            throw new SActorMappingImportException(ioe);
        } finally {
            reader.close();
        }
    }

}
