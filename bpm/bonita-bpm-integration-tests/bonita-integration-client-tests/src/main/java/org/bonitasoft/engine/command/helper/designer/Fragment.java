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
