/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.command.helper.designer;

import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 18:38
 */
public class SimpleProcessDesigner {

    private final Branch branch;

    private ProcessDefinitionBuilder builder;

    public SimpleProcessDesigner(ProcessDefinitionBuilder builder) {
        this.builder = builder;
        this.branch = new Branch();
    }

    public SimpleProcessDesigner start() {
        startWith(new StartEvent("start"));
        return this;
    }

    public SimpleProcessDesigner startWith(StartEvent start) {
        branch.start(start);
        return this;
    }

    public SimpleProcessDesigner then(Fragment... targets) {
        branch.then(targets);
        return this;
    }

    public SimpleProcessDesigner end() {
        return then(new EndEvent("end"));
    }

    public DesignProcessDefinition done() throws InvalidProcessDefinitionException {
        branch.build(builder);
        return builder.done();
    }

}
