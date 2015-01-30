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
package org.bonitasoft.engine.core.process.definition.model.builder.impl;

import org.bonitasoft.engine.core.process.definition.model.SBusinessDataDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.SBusinessDataDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.impl.SBusinessDataDefinitionImpl;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Emmanuel Duchastenier
 */
public class SBusinessDataDefinitionBuilderImpl implements SBusinessDataDefinitionBuilder {

    private final SBusinessDataDefinitionImpl businessDataDefinitionImpl;

    public SBusinessDataDefinitionBuilderImpl(final SBusinessDataDefinitionImpl businessDataDefinitionImpl) {
        this.businessDataDefinitionImpl = businessDataDefinitionImpl;
    }

    public static SBusinessDataDefinitionBuilder getInstance() {
        return new SBusinessDataDefinitionBuilderImpl(null);
    }

    @Override
    public SBusinessDataDefinitionBuilder setDescription(final String description) {
        businessDataDefinitionImpl.setDescription(description);
        return this;
    }

    @Override
    public SBusinessDataDefinitionBuilder setDefaultValue(final SExpression expression) {
        businessDataDefinitionImpl.setDefaultValueExpression(expression);
        return this;
    }

    @Override
    public SBusinessDataDefinitionBuilder setMultiple(boolean isMultiple) {
        businessDataDefinitionImpl.setMultiple(isMultiple);
        return this;
    }

    @Override
    public SBusinessDataDefinition done() {
        return businessDataDefinitionImpl;
    }

}
