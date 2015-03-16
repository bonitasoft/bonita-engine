/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
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
