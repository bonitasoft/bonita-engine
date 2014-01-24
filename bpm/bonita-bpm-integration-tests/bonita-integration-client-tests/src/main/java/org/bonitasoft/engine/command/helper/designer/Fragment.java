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

    private Map<String, Condition> conditions = new HashMap<String, Condition>();

    public Fragment when(String step, Condition condition) {
        conditions.put(step, condition);
        return this;
    }

    public void bind(List<Fragment> sources, ProcessDefinitionBuilder builder) {
        for (Fragment source : sources) {
            bind(source.getName(), conditions.get(source.getName()), builder);
        }
    }

    abstract public void bind(String source, Condition condition, ProcessDefinitionBuilder builder);

    public abstract void build(ProcessDefinitionBuilder builder);

    public abstract String getName();

}
