/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bpm.process.impl;

import org.bonitasoft.engine.bpm.flownode.impl.ActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SubProcessDefinitionImpl extends ActivityDefinitionImpl implements SubProcessDefinition {

    private static final long serialVersionUID = -5578839351835375715L;

    private final boolean triggeredByEvent;

    private FlowElementContainerDefinition subProcessContainer;

    public SubProcessDefinitionImpl(final String name, final boolean triggeredByEvent) {
        super(name);
        this.triggeredByEvent = triggeredByEvent;
    }

    public SubProcessDefinitionImpl(final long id, final String name, final boolean triggeredByEvent) {
        super(id, name);
        this.triggeredByEvent = triggeredByEvent;
    }

    public void setSubProcessContainer(final FlowElementContainerDefinition subProcessContainer) {
        this.subProcessContainer = subProcessContainer;
    }

    @Override
    public FlowElementContainerDefinition getSubProcessContainer() {
        return subProcessContainer;
    }

    @Override
    public boolean isTriggeredByEvent() {
        return triggeredByEvent;
    }

}
