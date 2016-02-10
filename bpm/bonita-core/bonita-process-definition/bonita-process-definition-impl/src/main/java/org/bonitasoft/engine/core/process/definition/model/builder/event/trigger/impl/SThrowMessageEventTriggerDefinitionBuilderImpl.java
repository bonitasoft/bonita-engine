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
package org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.impl;

import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCorrelationDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SThrowMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.data.definition.model.SDataDefinition;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 */
public class SThrowMessageEventTriggerDefinitionBuilderImpl implements SThrowMessageEventTriggerDefinitionBuilder {

    private final SThrowMessageEventTriggerDefinitionImpl entity;

    public SThrowMessageEventTriggerDefinitionBuilderImpl(final SThrowMessageEventTriggerDefinitionImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SThrowMessageEventTriggerDefinitionBuilder addCorrelation(final SExpression key, final SExpression value) {
        entity.addCorrelation(new SCorrelationDefinitionImpl(key, value));
        return this;
    }

    @Override
    public SThrowMessageEventTriggerDefinitionBuilder addData(final SDataDefinition dataDefinition) {
        entity.addDataDefinition(dataDefinition);
        return this;
    }

    @Override
    public SThrowMessageEventTriggerDefinition done() {
        return entity;
    }
}
