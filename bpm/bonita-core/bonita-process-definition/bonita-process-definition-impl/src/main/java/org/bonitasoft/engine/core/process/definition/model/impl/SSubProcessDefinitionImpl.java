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

import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SSubProcessDefinition;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SSubProcessDefinitionImpl extends SActivityDefinitionImpl implements SSubProcessDefinition {

    private static final long serialVersionUID = -244737326513630024L;

    private final boolean triggeredByEvent;

    private SFlowElementContainerDefinition subProcessContainer;

    public SSubProcessDefinitionImpl(final SubProcessDefinition subProcess) {
        super(subProcess.getId(), subProcess.getName());
        triggeredByEvent = subProcess.isTriggeredByEvent();
        subProcessContainer = new SFlowElementContainerDefinitionImpl(this, subProcess.getSubProcessContainer());
    }

    public SSubProcessDefinitionImpl(final long id, final String name, final boolean triggeredByEvent) {
        super(id, name);
        this.triggeredByEvent = triggeredByEvent;
    }

    @Override
    public SFlowNodeType getType() {
        return SFlowNodeType.SUB_PROCESS;
    }

    @Override
    public boolean isTriggeredByEvent() {
        return triggeredByEvent;
    }

    @Override
    public SFlowElementContainerDefinition getSubProcessContainer() {
        return subProcessContainer;
    }

    public void setSubProcessContainer(final SFlowElementContainerDefinition container) {
        subProcessContainer = container;
    }

    @Override
    public boolean isStartable() {
        return !isTriggeredByEvent() && super.isStartable();
    }

    @Override
    public boolean isEventSubProcess() {
        return isTriggeredByEvent();
    }
}
