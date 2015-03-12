/**
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
 **/
package org.bonitasoft.engine.actor.xml;

import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.xml.XMLNode;

/**
 * @author Matthieu Chaffotte
 */
public class ActorMappingNodeBuilder {

    private static final String NS_PREFIX = "actormappings";

    private static final String NAME_SPACE = "http://www.bonitasoft.org/ns/actormapping/6.0";

    public static XMLNode getDocument(final ActorMapping actorMapping) {
        final XMLNode actorMappingNode = new XMLNode(NS_PREFIX + ":actorMappings");
        actorMappingNode.addAttribute("xmlns:" + NS_PREFIX, NAME_SPACE);
        final List<Actor> actors = actorMapping.getActors();
        for (final Actor actor : actors) {
            final XMLNode actorNode = getActor(actor);
            actorMappingNode.addChild(actorNode);
        }
        return actorMappingNode;
    }

    private static XMLNode getActor(final Actor actor) {
        final XMLNode actorNode = new XMLNode("actorMapping");
        actorNode.addAttribute("name", actor.getName());
        final Set<String> users = actor.getUsers();
        if (!users.isEmpty()) {
            final XMLNode usersNode = getUsers(users);
            actorNode.addChild(usersNode);
        }
        final Set<String> roles = actor.getRoles();
        if (!roles.isEmpty()) {
            final XMLNode rolesNode = getRoles(roles);
            actorNode.addChild(rolesNode);
        }
        final Set<String> groups = actor.getGroups();
        if (!groups.isEmpty()) {
            final XMLNode groupsNode = getGroups(groups);
            actorNode.addChild(groupsNode);
        }
        final Set<BEntry<String, String>> memberships = actor.getMemberships();
        if (!memberships.isEmpty()) {
            final XMLNode membershipsNode = getMemberships(memberships);
            actorNode.addChild(membershipsNode);
        }
        return actorNode;
    }

    /**
     * @param memberships
     * @return
     */
    private static XMLNode getMemberships(final Set<BEntry<String, String>> memberships) {
        final XMLNode membershipsNode = new XMLNode("memberships");
        for (final BEntry<String, String> membership : memberships) {
            final XMLNode membershipNode = new XMLNode("membership");
            final XMLNode groupNode = new XMLNode("group");
            groupNode.setContent(membership.getKey());
            final XMLNode roleNode = new XMLNode("role");
            roleNode.setContent(membership.getValue());
            membershipNode.addChild(roleNode);
            membershipNode.addChild(groupNode);
            membershipsNode.addChild(membershipNode);
        }
        return membershipsNode;
    }

    private static XMLNode getUsers(final Set<String> users) {
        final XMLNode usersNode = new XMLNode("users");
        for (final String user : users) {
            final XMLNode userNode = new XMLNode("user");
            userNode.setContent(user);
            usersNode.addChild(userNode);
        }
        return usersNode;
    }

    private static XMLNode getRoles(final Set<String> roles) {
        final XMLNode rolesNode = new XMLNode("roles");
        for (final String role : roles) {
            final XMLNode roleNode = new XMLNode("role");
            roleNode.setContent(role);
            rolesNode.addChild(roleNode);
        }
        return rolesNode;
    }

    private static XMLNode getGroups(final Set<String> groups) {
        final XMLNode groupsNode = new XMLNode("groups");
        for (final String group : groups) {
            final XMLNode groupNode = new XMLNode("group");
            groupNode.setContent(group);
            groupsNode.addChild(groupNode);
        }
        return groupsNode;
    }

}
