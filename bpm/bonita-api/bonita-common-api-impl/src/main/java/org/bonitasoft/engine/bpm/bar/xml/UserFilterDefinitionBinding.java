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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.userfilter.impl.UserFilterDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 */
public class UserFilterDefinitionBinding extends NamedElementBinding {

    private String userFilterId;

    private String version;

    private final Map<String, Expression> inputs = new HashMap<String, Expression>();

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
        userFilterId = attributes.get(XMLProcessDefinition.USER_FILTER_ID);
        version = attributes.get(XMLProcessDefinition.CONNECTOR_VERSION);
    }

    @Override
    public Object getObject() {
        final UserFilterDefinitionImpl userFilterDefinitionImpl = new UserFilterDefinitionImpl(name, userFilterId, version);
        for (final Entry<String, Expression> entry : inputs.entrySet()) {
            userFilterDefinitionImpl.addInput(entry.getKey(), entry.getValue());
        }
        return userFilterDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.USER_FILTER_NODE;
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.CONNECTOR_INPUT.equals(name)) {// Same as connector input
            final Entry<?, ?> entry = (Entry<?, ?>) value;
            inputs.put((String) entry.getKey(), (Expression) entry.getValue());
        }
    }
}
