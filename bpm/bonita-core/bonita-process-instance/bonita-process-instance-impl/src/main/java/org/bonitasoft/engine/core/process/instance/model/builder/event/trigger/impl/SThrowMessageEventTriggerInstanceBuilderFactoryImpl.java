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
package org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowMessageEventTriggerInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SThrowMessageEventTriggerInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowMessageEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowMessageEventTriggerInstanceImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SThrowMessageEventTriggerInstanceBuilderFactoryImpl extends SEventTriggerInstanceBuilderFactoryImpl implements
        SThrowMessageEventTriggerInstanceBuilderFactory {

    @Override
    public SThrowMessageEventTriggerInstanceBuilder createNewInstance(final long eventInstanceId, final String messageName, final String targetProcess,
            final String targetFlowNode) {
        final SThrowMessageEventTriggerInstanceImpl entity = new SThrowMessageEventTriggerInstanceImpl(eventInstanceId, messageName, targetProcess,
                targetFlowNode);
        return new SThrowMessageEventTriggerInstanceBuilderImpl(entity);
    }

    @Override
    public SThrowMessageEventTriggerInstanceBuilder createNewInstance(final SThrowMessageEventTriggerInstance sThrowMessageEventTriggerInstance) {
        final SThrowMessageEventTriggerInstanceImpl entity = new SThrowMessageEventTriggerInstanceImpl(sThrowMessageEventTriggerInstance.getEventInstanceId(),
                sThrowMessageEventTriggerInstance.getMessageName(), sThrowMessageEventTriggerInstance.getTargetProcess(),
                sThrowMessageEventTriggerInstance.getTargetFlowNode());
        entity.setId(entity.getId());
        return new SThrowMessageEventTriggerInstanceBuilderImpl(entity);
    }

}
