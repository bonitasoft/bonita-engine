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
package org.bonitasoft.engine.service.impl;

import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServerLoggerWrapperTest {

    @Mock
    private TechnicalLoggerService technicalLoggerService;
    private final Class<?> aClass = ServerLoggerWrapperTest.class;
    private final IllegalStateException anException = new IllegalStateException();
    private ServerLoggerWrapper logger;

    @Before
    public void before() {
        logger = new ServerLoggerWrapper(aClass, technicalLoggerService);
    }

    @Test
    public void testTrace() {
        logger.trace("msg");

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.TRACE, "msg");
    }

    @Test
    public void testTrace1() {
        logger.trace("msg", anException);

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.TRACE, "msg", anException);
    }

    @Test
    public void testDebug() {
        logger.debug("msg");

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.DEBUG, "msg");

    }

    @Test
    public void testDebug1() {
        logger.debug("msg", anException);

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.DEBUG, "msg", anException);

    }

    @Test
    public void testInfo() {
        logger.info("msg");

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.INFO, "msg");

    }

    @Test
    public void testInfo1() {
        logger.info("msg", anException);

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.INFO, "msg", anException);

    }

    @Test
    public void testWarning() {
        logger.warning("msg");

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.WARNING, "msg");

    }

    @Test
    public void testWarning1() {
        logger.warning("msg", anException);

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.WARNING, "msg", anException);

    }

    @Test
    public void testError() {
        logger.error("msg");

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.ERROR, "msg");

    }

    @Test
    public void testError1() {
        logger.error("msg", anException);

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.ERROR, "msg", anException);

    }
}
