package com.bonitasoft.engine.api.impl.resolver;

import org.assertj.core.api.Condition;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.Problem.Level;

public class ProblemCondition extends Condition<Problem> {

    @Override
    public boolean matches(final Problem value) {
        return Level.ERROR.equals(value.getLevel()) && "business data".equals(value.getResource());
    }

}
