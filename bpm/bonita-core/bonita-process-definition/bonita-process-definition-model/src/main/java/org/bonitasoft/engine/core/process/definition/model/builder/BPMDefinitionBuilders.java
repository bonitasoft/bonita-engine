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
package org.bonitasoft.engine.core.process.definition.model.builder;

import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SEndEventDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowErrorEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowSignalEventTriggerDefinitionBuilder;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 */
public interface BPMDefinitionBuilders {

    SProcessDefinitionBuilder getProcessDefinitionBuilder();

    ProcessDefinitionDeployInfoBuilder getProcessDefinitionDeployInfoBuilder();

    ProcessDefinitionLogBuilder getProcessDefinitionLogBuilder();

    SProcessDefinitionDeployInfoUpdateBuilder getProcessDefinitionDeployInfoUpdateBuilder();

    SThrowSignalEventTriggerDefinitionBuilder getThrowSignalEventTriggerDefinitionBuilder();

    SThrowMessageEventTriggerDefinitionBuilder getThrowMessageEventTriggerDefinitionBuilder();

    SThrowErrorEventTriggerDefinitionBuilder getThrowErrorEventTriggerDefinitionBuilder();

    SEndEventDefinitionBuilder getSEndEventDefinitionBuilder();

}
