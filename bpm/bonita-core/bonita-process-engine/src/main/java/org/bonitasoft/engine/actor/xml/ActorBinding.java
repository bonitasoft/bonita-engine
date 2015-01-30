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
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Matthieu Chaffotte
 */
public class ActorBinding extends ElementBinding {

    private Actor actor;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        final String name = attributes.get("name");
        actor = new Actor(name);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setChildObject(final String name, final Object value) {
        if ("users".equals(name)) {
            actor.addUsers((List<String>) value);
        } else if ("groups".equals(name)) {
            actor.addGroups((List<String>) value);
        } else if ("roles".equals(name)) {
            actor.addRoles((List<String>) value);
        } else if ("membership".equals(name)) {
            final BEntry<String, String> entry = (BEntry<String, String>) value;
            actor.addMembership(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Actor getObject() {
        return actor;
    }

    @Override
    public String getElementTag() {
        return "actorMapping";
    }

}
