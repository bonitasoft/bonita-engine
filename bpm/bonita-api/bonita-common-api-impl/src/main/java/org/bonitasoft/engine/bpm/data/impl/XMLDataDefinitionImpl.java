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
package org.bonitasoft.engine.bpm.data.impl;

import org.bonitasoft.engine.bpm.data.XMLDataDefinition;
import org.bonitasoft.engine.expression.Expression;

/**
 * @author Feng Hui
 * @author Celine Souchet
 */
public class XMLDataDefinitionImpl extends DataDefinitionImpl implements XMLDataDefinition {

    private static final long serialVersionUID = 3614847378996945363L;

    private String namespace;

    private String element;

    public XMLDataDefinitionImpl(final String name, final Expression defaultValueExpression) {
        super(name, defaultValueExpression);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getElement() {
        return element;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public void setElement(final String element) {
        this.element = element;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (namespace == null ? 0 : namespace.hashCode());
        result = prime * result + (element == null ? 0 : element.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final XMLDataDefinitionImpl other = (XMLDataDefinitionImpl) obj;
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }
        if (element == null) {
            if (other.element != null) {
                return false;
            }
        } else if (!element.equals(other.element)) {
            return false;
        }
        return true;
    }

}
