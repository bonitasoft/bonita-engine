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
package org.bonitasoft.engine.core.process.definition.model.event;

import java.util.List;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;

/**
 * @author Elias Ricken de Medeiros
 */
public interface SEventDefinition extends SFlowNodeDefinition {

    /**
     * Retrieve an unmodifiable list of event triggers associate to this event. If none, an empty list is returned.
     * 
     * @return an unmodifiable list of event triggers associate to this event
     */
    List<SEventTriggerDefinition> getEventTriggers();

}
