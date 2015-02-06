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
import org.bonitasoft.engine.core.process.definition.model.impl.STransitionDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class STransitionDefinitionBinding extends SNamedElementBinding {

    private long source;

    private long target;

    private SExpression condition;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        source = Long.valueOf(attributes.get(XMLProcessDefinition.TRANSITION_SOURCE));
        target = Long.valueOf(attributes.get(XMLProcessDefinition.TRANSITION_TARGET));
    }

    @Override
    public Object getObject() {
        final STransitionDefinitionImpl transitionDefinitionImpl = new STransitionDefinitionImpl(name, source, target);
        transitionDefinitionImpl.setId(id);
        if (condition != null) {
            transitionDefinitionImpl.setCondition(condition);
        }
        return transitionDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.TRANSITION_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.TRANSITION_CONDITION.equals(name)) {
            condition = (SExpression) value;
        }
    }

}
