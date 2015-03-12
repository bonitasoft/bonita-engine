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
package org.bonitasoft.engine.command.helper.designer;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;

/**
 * @author Vincent Elcrin
 */
public class BoundaryEvent {

    private final String name;

    private final Fragment flowsOut;

    private Trigger trigger;

    Transition transition = new Transition();

    public BoundaryEvent(String name, Fragment flowsOut) {
        this.name = name;
        this.flowsOut = flowsOut;
    }

    public void attach(UserTaskDefinitionBuilder task, ProcessDefinitionBuilder builder) {
        trigger.listen(task.addBoundaryEvent(name));
        flowsOut.build(builder);
        transition.bind(name, flowsOut.getName(), builder);
    }

    public BoundaryEvent triggeredBy(Trigger trigger) {
        this.trigger = trigger;
        return this;
    }

}
