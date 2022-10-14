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
package org.bonitasoft.web.rest.model.builder.bpm.process;

import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.web.rest.model.bpm.process.ActorItem;

/**
 * @author Colin PUY
 */
public class ActorItemBuilder {

    private Long id;
    private Long processId;
    private String name;
    private String displayName;
    private String description;

    private ActorItemBuilder() {
    }

    public static ActorItemBuilder anActorItem() {
        return new ActorItemBuilder();
    }

    public ActorItem build() {
        ActorItem actorItem = new ActorItem();
        actorItem.setId(id);
        actorItem.setProcessId(processId);
        actorItem.setName(name);
        actorItem.setDisplayName(displayName);
        actorItem.setDescription(description);
        return actorItem;
    }

    public ActorItemBuilder fromActorInstance(ActorInstance engineItem) {
        id = engineItem.getId();
        processId = engineItem.getProcessDefinitionId();
        name = engineItem.getName();
        displayName = engineItem.getDisplayName();
        description = engineItem.getDescription();
        return this;
    }
}
