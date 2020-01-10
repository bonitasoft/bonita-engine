/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.bpm.actor.impl;

import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Matthieu Chaffotte
 */
public class ActorInstanceImpl extends NamedElementImpl implements ActorInstance {

    private static final long serialVersionUID = 8251013663118023803L;

    private final String displayName;

    private final long processDefinitionId;

    private final boolean initiator;

    private final String description;

    public ActorInstanceImpl(final String name, final String description, final String displayName,
            final long processDefinitionId, final boolean initiator) {
        super(name);
        this.description = description;
        this.processDefinitionId = processDefinitionId;
        this.displayName = displayName;
        this.initiator = initiator;
    }

    @Override
    public long getProcessDefinitionId() {
        return this.processDefinitionId;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public boolean isInitiator() {
        return initiator;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
