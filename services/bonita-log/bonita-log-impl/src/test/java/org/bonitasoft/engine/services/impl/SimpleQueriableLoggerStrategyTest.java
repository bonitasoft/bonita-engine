package org.bonitasoft.engine.services.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.junit.Before;
import org.junit.Test;

public class SimpleQueriableLoggerStrategyTest {

    private static final String DISABLE_LOG_KEY = "org.bonitasoft.engine.services.queryablelog.disable";

    @Before
    public void setUp() {
        System.clearProperty(DISABLE_LOG_KEY);
    }

    @Test
    public void isLogable_shoud_return_false_if_system_property_queryablelogDisable_is_null() {
        System.setProperty(DISABLE_LOG_KEY, "true");
        SimpleQueriableLoggerStrategy strategy = new SimpleQueriableLoggerStrategy();
        assertThat(strategy.isLoggable("anyAction", SQueriableLogSeverity.BUSINESS)).isFalse();
    }

    @Test
    public void isLogable_shoud_return_true_if_system_property_queryablelogDisable_is_not_null() {
        SimpleQueriableLoggerStrategy strategy = new SimpleQueriableLoggerStrategy();
        assertThat(strategy.isLoggable("anyAction", SQueriableLogSeverity.BUSINESS)).isTrue();
    }

}
