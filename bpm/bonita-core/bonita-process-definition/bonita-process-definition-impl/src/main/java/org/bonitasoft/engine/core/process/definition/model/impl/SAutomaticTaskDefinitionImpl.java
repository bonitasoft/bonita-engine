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

import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SAutomaticTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SAutomaticTaskDefinitionImpl extends SActivityDefinitionImpl implements SAutomaticTaskDefinition {

    private static final long serialVersionUID = 96851790923787649L;

    public SAutomaticTaskDefinitionImpl(final ActivityDefinition activityDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(activityDefinition, transitionsMap);
    }

    public SAutomaticTaskDefinitionImpl(final long id, final String name) {
        super(id, name);
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.AUTOMATIC_TASK;
    }

}
