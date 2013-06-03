/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model.builder.event.handling.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SMessageInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SMessageInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowMessageEventTriggerInstance;

/**
 * @author Elias Ricken de Medeiros
 */
public class SMessageInstanceBuilderImpl implements SMessageInstanceBuilder {

    private SMessageInstanceImpl entity;

    @Override
    public SMessageInstanceBuilder createNewInstance(final SThrowMessageEventTriggerInstance throwMessage, final long processDefinitionId,
            final String flowNodeName) {
        entity = new SMessageInstanceImpl(throwMessage, processDefinitionId, flowNodeName);
        return this;
    }

    @Override
    public SMessageInstanceBuilder createNewInstance(final SMessageInstance message) {
        entity = new SMessageInstanceImpl(message.getMessageName(), message.getTargetProcess(), message.getTargetFlowNode(), message.getProcessDefinitionId(),
                message.getFlowNodeName());
        return this;
    }

    @Override
    public SMessageInstanceBuilder setCorrelation(final int index, final String correlation) {
        switch (index) {
            case 1:
                entity.setCorrelation1(correlation);
                break;
            case 2:
                entity.setCorrelation2(correlation);
                break;
            case 3:
                entity.setCorrelation3(correlation);
                break;
            case 4:
                entity.setCorrelation4(correlation);
                break;
            case 5:
                entity.setCorrelation5(correlation);
                break;
            default:
                break;
        }
        return this;
    }

    @Override
    public SMessageInstance done() {
        return entity;
    }

    @Override
    public String getTargetProcessKey() {
        return "targetProcess";
    }

    @Override
    public String getTargetFlowNodeKey() {
        return "targetFlowNode";
    }

    @Override
    public String getMessageNameKey() {
        return "messageName";
    }

    @Override
    public String getLockedKey() {
        return "locked";
    }

    @Override
    public String getHandledKey() {
        return "handled";
    }

}
