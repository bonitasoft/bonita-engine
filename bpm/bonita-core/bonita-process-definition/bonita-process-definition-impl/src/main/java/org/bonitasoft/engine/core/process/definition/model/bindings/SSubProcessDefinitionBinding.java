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
package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.impl.SFlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SSubProcessDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SSubProcessDefinitionBinding extends SActivityDefinitionBinding {

    private boolean triggeredByEvent;

    private SFlowElementContainerDefinitionImpl container;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        triggeredByEvent = Boolean.valueOf(attributes.get(XMLSProcessDefinition.TRIGGERED_BY_EVENT));
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.FLOW_ELEMENTS_NODE.equals(name)) {
            container = (SFlowElementContainerDefinitionImpl) value;
        } else {
            super.setChildObject(name, value);
        }
    }

    @Override
    public Object getObject() {
        final SSubProcessDefinitionImpl subProcess = new SSubProcessDefinitionImpl(id, name, triggeredByEvent);
        fillNode(subProcess);
        subProcess.setSubProcessContainer(container);
        container.setElementContainer(subProcess);
        return subProcess;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.SUB_PROCESS;
    }

}
