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

import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.document.impl.DocumentListDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class DocumentListDefinitionBinding extends NamedElementBinding {

    private String description;

    private Expression expression;

    @Override
    public void setAttributes(final Map<String, String> attributes) {
        super.setAttributes(attributes);
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (XMLProcessDefinition.DESCRIPTION.equals(name)) {
            description = value;
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (XMLProcessDefinition.EXPRESSION_NODE.equals(name)) {
            expression = (Expression) value;
        }
    }

    @Override
    public DocumentListDefinition getObject() {
        final DocumentListDefinitionImpl documentDefinitionImpl = new DocumentListDefinitionImpl(name);
        if (description != null) {
            documentDefinitionImpl.setDescription(description);
        }
        if (expression != null) {
            documentDefinitionImpl.setExpression(expression);
        }
        return documentDefinitionImpl;
    }

    @Override
    public String getElementTag() {
        return XMLProcessDefinition.DOCUMENT_LIST_DEFINITION_NODE;
    }

}
