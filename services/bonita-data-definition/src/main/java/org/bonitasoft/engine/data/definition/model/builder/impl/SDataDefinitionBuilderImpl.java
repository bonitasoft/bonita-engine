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
package org.bonitasoft.engine.data.definition.model.builder.impl;

import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.impl.SDataDefinitionImpl;
import org.bonitasoft.engine.data.definition.model.impl.STextDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SDataDefinitionBuilderImpl implements SDataDefinitionBuilder {

    private final SDataDefinitionImpl dataDefinitionImpl;
    
    public SDataDefinitionBuilderImpl(final SDataDefinitionImpl dataDefinitionImpl) {
        super();
        this.dataDefinitionImpl = dataDefinitionImpl;
    }

    @Override
    public SDataDefinition done() {
        return dataDefinitionImpl;
    }

    public SDataDefinitionBuilder setAsLongText(final boolean value) {
        if (dataDefinitionImpl instanceof STextDefinitionImpl && dataDefinitionImpl.getClassName().equals(String.class.getName())) {
            ((STextDefinitionImpl) dataDefinitionImpl).setIsLongText(value);
        }
        return this;
    }
    @Override
    public SDataDefinitionBuilder setName(final String name) {
        dataDefinitionImpl.setName(name);
        return this;
    }

    @Override
    public SDataDefinitionBuilder setDescription(final String description) {
        dataDefinitionImpl.setDescription(description);
        return this;
    }

    @Override
    public SDataDefinitionBuilder setTransient(final boolean transientData) {
        dataDefinitionImpl.setTransientData(transientData);
        return this;
    }

    @Override
    public SDataDefinitionBuilder setDefaultValue(final SExpression expression) {
        dataDefinitionImpl.setDefaultValueExpression(expression);
        return this;
    }

    public static SDataDefinitionBuilder getInstance() {
        return new SDataDefinitionBuilderImpl(null);
    }

}
