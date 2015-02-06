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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;

/**
 * @author Vincent Elcrin
 */
public class Branch extends Fragment {

    private List<List<Fragment>> sequence = new ArrayList<List<Fragment>>();

    private Fragment origin;

    private String name;

    public Branch start(Fragment origin) {
        this.origin = origin;
        name = origin.getName();
        return this;
    }

    public Branch then(Fragment... targets) {
        assert origin != null : "Branch need to be started by calling start method";
        sequence.add(Arrays.asList(targets));
        name = targets[0].getName();
        return this;
    }

    @Override
    public String getName() {
        assert name != null;
        return name;
    }

    @Override
    public void bind(List<Fragment> sources, ProcessDefinitionBuilder builder) {
        super.bind(sources, builder);
    }

    @Override
    public void bind(String source, Transition transition, ProcessDefinitionBuilder builder) {
        transition.bind(source, origin.getName(), builder);
    }

    @Override
    public void build(ProcessDefinitionBuilder builder) {
        List<Fragment> sources = new ArrayList<Fragment>();
        origin.build(builder);
        sources.add(origin);
        for (List<Fragment> targets : sequence) {
            link(sources, targets, builder);
        }
    }

    private void link(List<Fragment> sources, List<Fragment> targets, ProcessDefinitionBuilder builder) {
        for (Fragment target : targets) {
            target.build(builder);
            target.bind(sources, builder);
        }
        sources.clear();
        sources.addAll(targets);
    }
}
