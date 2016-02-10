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

import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowSignalEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowSignalEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SThrowSignalEventTriggerDefinitionImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class SThrowSignalEventTriggerDefinitionBuilderImpl implements SThrowSignalEventTriggerDefinitionBuilder {

    private final SThrowSignalEventTriggerDefinitionImpl entity;
            
    public SThrowSignalEventTriggerDefinitionBuilderImpl(final SThrowSignalEventTriggerDefinitionImpl entity) {
        super();
        this.entity = entity;
    }

    @Override
    public SThrowSignalEventTriggerDefinition done() {
        return entity;
    }

}
