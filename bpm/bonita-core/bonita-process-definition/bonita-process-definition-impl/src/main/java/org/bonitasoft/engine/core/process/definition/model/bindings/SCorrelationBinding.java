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

import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCorrelationDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Baptiste Mesta
 */
public class SCorrelationBinding extends ElementBinding {

    private SExpression key;

    private SExpression value;

    public SCorrelationBinding() {
        super();
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.CORRELATION_NODE;
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.CORRELATION_KEY.equals(name)) {
            key = (SExpression) value;
        }
        if (XMLSProcessDefinition.CORRELATION_VALUE.equals(name)) {
            this.value = (SExpression) value;
        }
    }

    @Override
    public Object getObject() {
        return new SCorrelationDefinitionImpl(key, value);
    }

}
