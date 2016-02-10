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
package org.bonitasoft.engine.execution.job;

import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class JobNameBuilder {

    private static final String PREFIX = "Timer_Ev_";

    public static String getTimerEventJobName(final Long processDefinitionId, final SEventDefinition eventDefinition, final SCatchEventInstance flowNodeInstance) {
        if (SFlowNodeType.START_EVENT.equals(eventDefinition.getType())) {
            return PREFIX + processDefinitionId + eventDefinition.getName();
        }
        return PREFIX + flowNodeInstance.getId();
    }

    public static String getTimerEventJobName(final Long processDefinitionId, final SEventDefinition eventDefinition, final long parentProcessInstanceId,
            final long subProcessId) {
        return PREFIX + processDefinitionId + eventDefinition.getName() + parentProcessInstanceId + subProcessId;
    }

}
