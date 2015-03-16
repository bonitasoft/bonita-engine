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

import org.bonitasoft.engine.core.process.definition.model.SDocumentListDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SDocumentListDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Baptiste Mesta
 */
public class SDocumentListDefinitionBinding extends SNamedElementBinding {

    private String description;

    private SExpression expression;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLSProcessDefinition.DESCRIPTION.equals(name)) {
            description = value;
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLSProcessDefinition.EXPRESSION_NODE.equals(name)) {
            expression = (SExpression) value;
        }
    }

    @Override
    public SDocumentListDefinition getObject() {
        SDocumentListDefinitionImpl sDocumentListDefinition = new SDocumentListDefinitionImpl(name);
        sDocumentListDefinition.setDescription(description);
        sDocumentListDefinition.setExpression(expression);
        return sDocumentListDefinition;
    }

    @Override
    public String getElementTag() {
        return XMLSProcessDefinition.DOCUMENT_LIST_DEFINITION_NODE;
    }
}
