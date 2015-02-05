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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.core.process.definition.model.impl.SUserFilterDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class SUserFilterDefinitionBinding extends SNamedElementBinding {

    private String userFilterId;

    private String version;

    private final Map<String, SExpression> inputs = new HashMap<String, SExpression>();

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        userFilterId = attributes.get(XMLSProcessDefinition.USER_FILTER_ID);
        version = attributes.get(XMLSProcessDefinition.CONNECTOR_VERSION);
    }

    @Override
    public Object getObject() {
        final SUserFilterDefinitionImpl userFilterDefinitionImpl = new SUserFilterDefinitionImpl(name, userFilterId, version);
        for (final Entry<String, SExpression> entry : inputs.entrySet()) {
            userFilterDefinitionImpl.addInput(entry.getKey(), entry.getValue());
        }
        return userFilterDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.USER_FILTER_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.CONNECTOR_INPUT.equals(name)) {// Same as connector input
            final Entry<?, ?> entry = (Entry<?, ?>) value;
            inputs.put((String) entry.getKey(), (SExpression) entry.getValue());
        }
    }

}
