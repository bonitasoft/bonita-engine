/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.service.impl;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ServerLoggerWrapperTest {

    private final Class<?> aClass = ServerLoggerWrapperTest.class;
    private final IllegalStateException anException = new IllegalStateException();
    private ServerLoggerWrapper logger;
    @Mock
    private Logger loggerSL4J;

    @Before
    public void before() {
        logger = new ServerLoggerWrapper(aClass, loggerSL4J);
    }

    @Test
    public void testTrace() {
        logger.trace("msg");

        verify(loggerSL4J).trace("msg");
    }

    @Test
    public void testTrace1() {
        logger.trace("msg", anException);

        verify(loggerSL4J).trace("msg", anException);
    }

    @Test
    public void testDebug() {
        logger.debug("msg");

        verify(loggerSL4J).debug("msg");

    }

    @Test
    public void testDebug1() {
        logger.debug("msg", anException);

        verify(loggerSL4J).debug("msg", anException);

    }

    @Test
    public void testInfo() {
        logger.info("msg");

        verify(loggerSL4J).info("msg");

    }

    @Test
    public void testInfo1() {
        logger.info("msg", anException);

        verify(loggerSL4J).info("msg", anException);

    }

    @Test
    public void testWarning() {
        logger.warning("msg");

        verify(loggerSL4J).warn("msg");

    }

    @Test
    public void testWarning1() {
        logger.warning("msg", anException);

        verify(loggerSL4J).warn("msg", anException);

    }

    @Test
    public void testError() {
        logger.error("msg");

        verify(loggerSL4J).error("msg");

    }

    @Test
    public void testError1() {
        logger.error("msg", anException);

        verify(loggerSL4J).error("msg", anException);

    }
}
