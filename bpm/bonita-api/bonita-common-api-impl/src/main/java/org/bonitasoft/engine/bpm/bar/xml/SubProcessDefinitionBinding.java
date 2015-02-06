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
package org.bonitasoft.engine.bpm.bar.xml;

import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.process.impl.internal.SubProcessDefinitionImpl;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SubProcessDefinitionBinding extends ActivityDefinitionBinding {

    private boolean triggeredByEvent;

    private FlowElementContainerDefinition container;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        triggeredByEvent = Boolean.valueOf(attributes.get(XMLProcessDefinition.TRIGGERED_BY_EVENT));
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.FLOW_ELEMENTS_NODE.equals(name)) {
            container = (FlowElementContainerDefinition) value;
        } else {
            super.setChildObject(name, value);
        }
    }

    @Override
    public Object getObject() {
        final SubProcessDefinitionImpl subProcessDefinitionImpl = new SubProcessDefinitionImpl(id, name, triggeredByEvent);
        fillNode(subProcessDefinitionImpl);
        subProcessDefinitionImpl.setSubProcessContainer(container);
        return subProcessDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.SUB_PROCESS;
    }

}
