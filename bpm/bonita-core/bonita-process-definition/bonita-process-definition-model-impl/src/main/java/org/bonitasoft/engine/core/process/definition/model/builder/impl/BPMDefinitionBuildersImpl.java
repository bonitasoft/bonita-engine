/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.definition.model.builder.ProcessDefinitionDeployInfoBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.ProcessDefinitionLogBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.SProcessDefinitionDeployInfoUpdateBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SEndEventDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowErrorEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowSignalEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.impl.SEndEventDefinitionBuilderImpl;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.impl.SThrowErrorEventTriggerDefinitionBuilderImpl;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.impl.SThrowMessageEventTriggerDefinitionBuilderImpl;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.impl.SThrowSignalEventTriggerDefinitionBuilderImpl;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 */
public class BPMDefinitionBuildersImpl implements BPMDefinitionBuilders {

    @Override
    public SProcessDefinitionBuilder getProcessDefinitionBuilder() {
        return new SProcessDefinitionBuilderImpl();
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder getProcessDefinitionDeployInfoBuilder() {
        return new ProcessDefinitionDeployInfoBuilderImpl();
    }

    @Override
    public ProcessDefinitionLogBuilder getProcessDefinitionLogBuilder() {
        return new ProcessDefinitionLogBuilderImpl();
    }

    @Override
    public SProcessDefinitionDeployInfoUpdateBuilder getProcessDefinitionDeployInfoUpdateBuilder() {
        return new SProcessDefinitionDeployInfoUpdateBuilderImpl(getProcessDefinitionDeployInfoBuilder());
    }

    @Override
    public SThrowSignalEventTriggerDefinitionBuilder getThrowSignalEventTriggerDefinitionBuilder() {
        return new SThrowSignalEventTriggerDefinitionBuilderImpl();
    }

    @Override
    public SThrowMessageEventTriggerDefinitionBuilder getThrowMessageEventTriggerDefinitionBuilder() {
        return new SThrowMessageEventTriggerDefinitionBuilderImpl();
    }

    @Override
    public SThrowErrorEventTriggerDefinitionBuilder getThrowErrorEventTriggerDefinitionBuilder() {
        return new SThrowErrorEventTriggerDefinitionBuilderImpl();
    }

    @Override
    public SEndEventDefinitionBuilder getSEndEventDefinitionBuilder() {
        return new SEndEventDefinitionBuilderImpl();
    }
}
