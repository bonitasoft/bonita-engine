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
package org.bonitasoft.engine.bpm.flownode;

import java.util.Date;

/**
 * Represent the instance of the {@link TimerEventTriggerDefinition} (only for the type {@link TimerType#DATE} and {@link TimerType#DURATION})
 * 
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
public interface TimerEventTriggerInstance extends EventTriggerInstance {

    /**
     * Return the date of the execution of the trigger.
     * 
     * @return The date of the execution of the trigger.
     * @since 6.4.0
     */
    Date getExecutionDate();

    /**
     * Return the name of the {@link EventInstance} which it is attached.
     * 
     * @return The name of the {@link EventInstance}
     * @since 6.4.0
     */
    String getEventInstanceName();

}
