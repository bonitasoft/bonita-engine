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

import org.bonitasoft.engine.bpm.flownode.impl.HumanTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.SHumanTaskDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public abstract class SHumanTaskDefinitionImpl extends SActivityDefinitionImpl implements SHumanTaskDefinition {

    private static final long serialVersionUID = -4475497975786419487L;

    private final String actorName;

    private SUserFilterDefinition sUserFilterDefinition;

    private String priority;

    private Long expectedDuration;

    public SHumanTaskDefinitionImpl(final HumanTaskDefinition userTaskDefinition,
            final Map<String, STransitionDefinition> transitionsMap) {
        super(userTaskDefinition, transitionsMap);
        actorName = userTaskDefinition.getActorName();
    }

    public SHumanTaskDefinitionImpl(final long id, final String name, final String actorName) {
        super(id, name);
        this.actorName = actorName;
    }

    @Override
    public String getActorName() {
        return actorName;
    }

    public void setUserFilter(final SUserFilterDefinition sUserFilterDefinition) {
        this.sUserFilterDefinition = sUserFilterDefinition;
    }

    @Override
    public SUserFilterDefinition getSUserFilterDefinition() {
        return sUserFilterDefinition;
    }

    @Override
    public Long getExpectedDuration() {
        return expectedDuration;
    }

    @Override
    public String getPriority() {
        return priority;
    }

    public void setPriority(final String priority) {
        this.priority = priority;
    }

    public void setExpectedDuration(final Long expectedDuration) {
        this.expectedDuration = expectedDuration;
    }

}
