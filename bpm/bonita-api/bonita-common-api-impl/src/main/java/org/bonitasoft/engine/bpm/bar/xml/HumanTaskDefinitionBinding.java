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

import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowNodeDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.HumanTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;

/**
 * @author Baptiste Mesta
 */
public abstract class HumanTaskDefinitionBinding extends ActivityDefinitionBinding {

    protected String actorName;

    private UserFilterDefinition userFilter;

    private Long expectedDuration;

    private String priority;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        actorName = attributes.get(XMLProcessDefinition.ACTOR_NAME);
        priority = attributes.get(XMLProcessDefinition.PRIORITY);
        final String expectedDurationAsString = attributes.get(XMLProcessDefinition.EXPECTED_DURATION);
        if (expectedDurationAsString == null) {
            expectedDuration = null;
        } else {
            expectedDuration = Long.valueOf(expectedDurationAsString);
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.USER_FILTER_NODE.equals(name)) {
            userFilter = (UserFilterDefinition) value;
        }
    }

    @Override
    protected void fillNode(final FlowNodeDefinitionImpl flowNode) {
        super.fillNode(flowNode);
        if (userFilter != null) {
            ((HumanTaskDefinitionImpl) flowNode).setUserFilter(userFilter);
        }
        if (priority != null) {
            ((HumanTaskDefinitionImpl) flowNode).setPriority(priority);
        }
        if (expectedDuration != null) {
            ((HumanTaskDefinitionImpl) flowNode).setExpectedDuration(expectedDuration);
        }
    }

}
