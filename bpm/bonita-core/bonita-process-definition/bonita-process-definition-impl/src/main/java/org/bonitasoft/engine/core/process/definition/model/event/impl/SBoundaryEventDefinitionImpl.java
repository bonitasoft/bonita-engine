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

import org.bonitasoft.engine.bpm.flownode.CatchEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SBoundaryEventDefinition;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SBoundaryEventDefinitionImpl extends SCatchEventDefinitionImpl implements SBoundaryEventDefinition {

    private static final long serialVersionUID = 7591508888593777075L;

    public SBoundaryEventDefinitionImpl(final long id, final String name) {
        super(id, name);
    }

    public SBoundaryEventDefinitionImpl(final CatchEventDefinition eventDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(eventDefinition, transitionsMap);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.BOUNDARY_EVENT;
    }

    @Override
    public boolean isStartable() {
        return false;
    }
}
