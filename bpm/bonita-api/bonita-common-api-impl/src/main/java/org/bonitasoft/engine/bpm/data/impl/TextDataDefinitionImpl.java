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

import org.bonitasoft.engine.bpm.data.TextDataDefinition;
import org.bonitasoft.engine.expression.Expression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Feng Hui
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TextDataDefinitionImpl extends DataDefinitionImpl implements TextDataDefinition {

    private static final long serialVersionUID = 1619846767581787465L;
    @XmlAttribute
    private boolean longText;

    public TextDataDefinitionImpl(){}
    public TextDataDefinitionImpl(final String name, final Expression defaultValueExpression) {
        super(name, defaultValueExpression);
    }

    @Override
    public boolean isLongText() {
        return longText;
    }

    public void setLongText(final boolean longText) {
        this.longText = longText;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (longText ? 1231 : 1237);
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
        final TextDataDefinitionImpl other = (TextDataDefinitionImpl) obj;
        if (longText != other.longText) {
            return false;
        }
        return true;
    }

}
