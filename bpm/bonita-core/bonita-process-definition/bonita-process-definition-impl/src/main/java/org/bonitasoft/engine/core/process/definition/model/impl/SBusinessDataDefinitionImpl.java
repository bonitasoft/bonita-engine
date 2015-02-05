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
package org.bonitasoft.engine.core.process.definition.model.impl;

import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Emmanuel Duchastenier
 */
public class SBusinessDataDefinitionImpl implements SBusinessDataDefinition {

    private static final long serialVersionUID = 1L;

    private String name;

    private String description;

    private String className;

    private SExpression defaultValueExpression;

    private boolean multiple;

    public SBusinessDataDefinitionImpl() {
        super();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public SExpression getDefaultValueExpression() {
        return defaultValueExpression;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setDefaultValueExpression(final SExpression defaultValueExpression) {
        this.defaultValueExpression = defaultValueExpression;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(final boolean multiple) {
        this.multiple = multiple;
    }

}
