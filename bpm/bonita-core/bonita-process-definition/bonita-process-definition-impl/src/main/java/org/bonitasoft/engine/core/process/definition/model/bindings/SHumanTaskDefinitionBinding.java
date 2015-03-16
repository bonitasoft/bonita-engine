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

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.SActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SUserFilterDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SHumanTaskDefinitionImpl;

/**
 * @author Baptiste Mesta
 */
public class SHumanTaskDefinitionBinding extends SAutomaticTaskDefinitionBinding {

    protected String actorName;

    private SUserFilterDefinition userFilter;

    private Long expectedDuration;

    private String priority;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        actorName = attributes.get(XMLSProcessDefinition.ACTOR_NAME);
        priority = attributes.get(XMLSProcessDefinition.PRIORITY);
        final String expectedDurationAsString = attributes.get(XMLSProcessDefinition.EXPECTED_DURATION);
        if (expectedDurationAsString == null) {
            expectedDuration = null;
        } else {
            expectedDuration = Long.valueOf(expectedDurationAsString);
        }
    }

    @Override
    protected void fillNode(final SActivityDefinition humanTaskDefinitionImpl) {
        super.fillNode(humanTaskDefinitionImpl);
        final SHumanTaskDefinitionImpl humanTaskDefinitionImpl2 = (SHumanTaskDefinitionImpl) humanTaskDefinitionImpl;
        if (userFilter != null) {
            humanTaskDefinitionImpl2.setUserFilter(userFilter);
        }
        if (priority != null) {
            humanTaskDefinitionImpl2.setPriority(priority);
        }
        if (expectedDuration != null) {
            humanTaskDefinitionImpl2.setExpectedDuration(expectedDuration);
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        super.setChildObject(name, value);
        if (XMLProcessDefinition.USER_FILTER_NODE.equals(name)) {
            userFilter = (SUserFilterDefinition) value;
        }
    }
}
