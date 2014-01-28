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

    private Map<String, Transition> transitions = new HashMap<String, Transition>();

    public Fragment when(String step, Transition transition) {
        transitions.put(step, transition);
        return this;
    }

    public void bind(List<Fragment> sources, ProcessDefinitionBuilder builder) {
        for (Fragment source : sources) {
            Transition transition = transitions.get(source.getName());
            if(transition == null) {
                transition = new Transition();
            }
            bind(source.getName(), transition, builder);
        }
    }

    abstract public void bind(String source, Transition transition, ProcessDefinitionBuilder builder);

    public abstract void build(ProcessDefinitionBuilder builder);

    public abstract String getName();

}
