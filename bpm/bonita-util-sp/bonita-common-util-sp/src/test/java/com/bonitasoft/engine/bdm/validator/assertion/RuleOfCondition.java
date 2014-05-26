/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.assertion;

import org.assertj.core.api.Condition;

import com.bonitasoft.engine.bdm.validator.rule.ValidationRule;

/**
 * @author Colin PUY
 */
public class RuleOfCondition extends Condition<ValidationRule<?>> {

    public static RuleOfCondition ruleOf(Class<? extends ValidationRule<?>> ruleClass) {
        return new RuleOfCondition(ruleClass);
    }

    private Class<?> ruleClass;

    public RuleOfCondition(Class<?> ruleClass) {
        this.ruleClass = ruleClass;
    }

    @Override
    public boolean matches(ValidationRule<?> rule) {
        return rule.getClass().equals(ruleClass);
    }
}
