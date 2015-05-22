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
 */

package org.bonitasoft.engine.core.process.definition.model.bindings;

import java.util.Map;

import org.bonitasoft.engine.core.process.definition.model.impl.SContextEntryImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 */
public class SContextEntryBinding extends SNamedElementBinding {

    private final SContextEntryImpl contextEntry;

    public SContextEntryBinding() {
        contextEntry = new SContextEntryImpl();
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        contextEntry.setKey(attributes.get(XMLSProcessDefinition.CONTEXT_ENTRY_KEY));
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.EXPRESSION_NODE.equals(name)) {
            contextEntry.setExpression((SExpression) value);
        }
    }

    @Override
    public Object getObject() {
        return contextEntry;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.CONTEXT_ENTRY_NODE;
    }

}
