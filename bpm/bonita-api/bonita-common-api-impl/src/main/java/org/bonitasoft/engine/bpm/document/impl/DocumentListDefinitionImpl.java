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
package org.bonitasoft.engine.bpm.document.impl;

import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Baptiste Mesta
 */
public class DocumentListDefinitionImpl extends NamedElementImpl implements DocumentListDefinition {

    private static final long serialVersionUID = 1L;

    private String description;

    private Expression expression;

    /**
     * @param name
     *        the name of the document list
     */
    public DocumentListDefinitionImpl(final String name) {
        super(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        DocumentListDefinitionImpl that = (DocumentListDefinitionImpl) o;

        if (description != null ? !description.equals(that.description) : that.description != null)
            return false;
        if (expression != null ? !expression.equals(that.expression) : that.expression != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DocumentListDefinitionImpl{" +
                "description='" + description + '\'' +
                ", expression=" + expression +
                "} " + super.toString();
    }
}
