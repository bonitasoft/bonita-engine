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
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 11:43
 */
public class UserTask extends FlowNode {

    private String actor = "actor";

    private BoundaryEvent event;

    public UserTask(String name) {
        super(name);
    }

    @Override
    public void build(ProcessDefinitionBuilder builder) {
        UserTaskDefinitionBuilder task = builder.addUserTask(getName(), actor);
        if(event != null) {
            event.attach(task, builder);
        }
    }

    public UserTask with(BoundaryEvent event) {
        this.event = event;
        return this;
    }
}
