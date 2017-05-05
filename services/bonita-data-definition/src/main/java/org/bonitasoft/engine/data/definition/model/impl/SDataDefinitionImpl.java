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
package org.bonitasoft.engine.data.definition.model.impl;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 */
public class SDataDefinitionImpl implements SDataDefinition {

    private static final long serialVersionUID = 1L;

    private String name;

    private String description;

    private boolean transientData;

    private String className;

    private SExpression defaultValueExpression;

    public SDataDefinitionImpl() {
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
    public Boolean isTransientData() {
        return transientData;
    }

    @Override
    public SExpression getDefaultValueExpression() {
        return defaultValueExpression;
    }

    public void setTransientData(final boolean transientData) {
        this.transientData = transientData;
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

}
