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
