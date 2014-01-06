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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 18:38
 */
public class SimpleProcessDesigner {

    private final ProcessDefinitionBuilder builder;

    private List<FlowNode> origins = new ArrayList<FlowNode>();

    public SimpleProcessDesigner(ProcessDefinitionBuilder builder) {
        this.builder = builder;
    }

    public SimpleProcessDesigner start() {
        return startWith(new StartEvent("start"));
    }

    public SimpleProcessDesigner branch() {
        return new SimpleProcessDesigner(builder);
    }

    public SimpleProcessDesigner startWith(FlowNode start) {
        origins.add(start);
        start.build(builder);
        return this;
    }

    public SimpleProcessDesigner then(SimpleProcessDesigner... branches) {
        return this;
    }

    public SimpleProcessDesigner then(FlowNode... targets) {
        assert !origins.isEmpty() : "startWith method need to be called first";

        for (FlowNode target : targets) {
            target.build(builder);
            target.bind(origins, builder);
        }

        origins.clear();
        origins.addAll(Arrays.asList(targets));
        return this;
    }

    public SimpleProcessDesigner end() {
        return then(new EndEvent("end"));
    }

    public DesignProcessDefinition done() throws InvalidProcessDefinitionException {
        return builder.done();
    }

}
