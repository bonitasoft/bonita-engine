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

import org.bonitasoft.engine.data.definition.model.SXMLDataDefinition;
import org.bonitasoft.engine.data.definition.model.builder.SXMLDataDefinitionBuilder;
import org.bonitasoft.engine.data.definition.model.impl.SXMLDataDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SXMLDataDefinitionBuilderImpl implements SXMLDataDefinitionBuilder {

    private final SXMLDataDefinitionImpl dataDefinitionImpl;

    public SXMLDataDefinitionBuilderImpl(
            SXMLDataDefinitionImpl dataDefinitionImpl) {
        super();
        this.dataDefinitionImpl = dataDefinitionImpl;
    }

    @Override
    public SXMLDataDefinitionBuilder setDescription(final String description) {
        dataDefinitionImpl.setDescription(description);
        return this;
    }

    @Override
    public SXMLDataDefinitionBuilder setTransient(final boolean transientData) {
        dataDefinitionImpl.setTransientData(transientData);
        return this;
    }

    @Override
    public SXMLDataDefinitionBuilder setDefaultValue(final SExpression expression) {
        dataDefinitionImpl.setDefaultValueExpression(expression);
        return this;
    }

    @Override
    public SXMLDataDefinitionBuilder setNamespace(final String namespace) {
        dataDefinitionImpl.setNamespace(namespace);
        return this;
    }

    @Override
    public SXMLDataDefinitionBuilder setElement(final String element) {
        dataDefinitionImpl.setElement(element);
        return this;
    }

    @Override
    public SXMLDataDefinition done() {
        return dataDefinitionImpl;
    }

}
