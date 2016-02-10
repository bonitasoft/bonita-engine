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
package org.bonitasoft.engine.core.process.definition.model.event.impl;

import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.StartEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SStartEventDefinitionImpl extends SCatchEventDefinitionImpl implements SStartEventDefinition {

    private static final long serialVersionUID = -8788360140531631436L;

    public SStartEventDefinitionImpl(final StartEventDefinition eventDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(eventDefinition, transitionsMap);
    }

    public SStartEventDefinitionImpl(final long id, final String name) {
        super(id, name);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.START_EVENT;
    }

    @Override
    public boolean isStartable() {
        return getEventTriggers().isEmpty() && super.isStartable();
    }
}
