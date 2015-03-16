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

import org.bonitasoft.engine.bpm.flownode.impl.internal.TransitionDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class TransitionDefinitionBinding extends NamedElementBinding {

    private long source;

    private long target;

    private Expression condition;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        source = Long.valueOf(attributes.get(XMLProcessDefinition.TRANSITION_SOURCE));
        target = Long.valueOf(attributes.get(XMLProcessDefinition.TRANSITION_TARGET));
    }

    @Override
    public Object getObject() {
        final TransitionDefinitionImpl transitionDefinitionImpl = new TransitionDefinitionImpl(name, source, target);
        transitionDefinitionImpl.setId(id);
        if (condition != null) {
            transitionDefinitionImpl.setCondition(condition);
        }
        return transitionDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.TRANSITION_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.TRANSITION_CONDITION.equals(name)) {
            condition = (Expression) value;
        }
    }

}
