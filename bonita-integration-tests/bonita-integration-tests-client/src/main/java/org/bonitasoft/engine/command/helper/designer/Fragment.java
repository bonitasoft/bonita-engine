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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 14:20
 */
public abstract class Fragment {

    private final Map<String, Transition> transitions = new HashMap<String, Transition>();

    public Fragment when(final String step, final Transition transition) {
        transitions.put(step, transition);
        return this;
    }

    public void bind(final List<Fragment> sources, final ProcessDefinitionBuilder builder) {
        for (final Fragment source : sources) {
            Transition transition = transitions.get(source.getName());
            if (transition == null) {
                transition = new Transition();
            }
            bind(source.getName(), transition, builder);
        }
    }

    public abstract void bind(String source, Transition transition, ProcessDefinitionBuilder builder);

    public abstract void build(ProcessDefinitionBuilder builder);

    public abstract String getName();

}
