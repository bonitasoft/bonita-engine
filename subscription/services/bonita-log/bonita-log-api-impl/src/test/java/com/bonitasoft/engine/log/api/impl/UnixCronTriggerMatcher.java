/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log.api.impl;

import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.mockito.ArgumentMatcher;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class UnixCronTriggerMatcher extends ArgumentMatcher<UnixCronTrigger> {
    
    
    private String expression;

    public UnixCronTriggerMatcher(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean matches(Object argument) {
        if (!(argument instanceof UnixCronTrigger)) {
            return false;
        }
        UnixCronTrigger cron = (UnixCronTrigger) argument;
        return expression.equals(cron.getExpression());
    }

}
