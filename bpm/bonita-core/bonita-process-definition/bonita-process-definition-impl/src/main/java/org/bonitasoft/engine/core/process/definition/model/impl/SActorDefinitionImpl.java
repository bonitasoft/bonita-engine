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
package org.bonitasoft.engine.core.process.definition.model.impl;

import org.bonitasoft.engine.bpm.actor.ActorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SActorDefinition;

/**
 * @author Matthieu Chaffotte
 */
public class SActorDefinitionImpl extends SNamedElementImpl implements SActorDefinition {

    private static final long serialVersionUID = -3781827896225458787L;

    private String description;

    private boolean initiator;

    public SActorDefinitionImpl(final ActorDefinition actor) {
        super(actor.getName());
        description = actor.getDescription();
        initiator = actor.isInitiator();
    }

    public SActorDefinitionImpl(final String name) {
        super(name);
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public boolean isInitiator() {
        return initiator;
    }

    public void setInitiator(boolean initiator) {
        this.initiator = initiator;
    }

}
