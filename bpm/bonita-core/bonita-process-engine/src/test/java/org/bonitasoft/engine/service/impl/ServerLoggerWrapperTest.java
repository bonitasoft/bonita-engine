/*
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
    private Class<?> aClass = ServerLoggerWrapperTest.class;
    private IllegalStateException anException = new IllegalStateException();
    private ServerLoggerWrapper logger;

    @Before
    public void before() {
        logger = new ServerLoggerWrapper(aClass, technicalLoggerService);
    }

    @Test
    public void testTrace() throws Exception {
        logger.trace("msg");

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.TRACE, "msg");
    }

    @Test
    public void testTrace1() throws Exception {
        logger.trace("msg", anException);

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.TRACE, "msg", anException);
    }

    @Test
    public void testDebug() throws Exception {
        logger.debug("msg");

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.DEBUG, "msg");

    }

    @Test
    public void testDebug1() throws Exception {
        logger.debug("msg", anException);

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.DEBUG, "msg", anException);

    }

    @Test
    public void testInfo() throws Exception {
        logger.info("msg");

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.INFO, "msg");

    }

    @Test
    public void testInfo1() throws Exception {
        logger.info("msg", anException);

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.INFO, "msg", anException);

    }

    @Test
    public void testWarning() throws Exception {
        logger.warning("msg");

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.WARNING, "msg");

    }

    @Test
    public void testWarning1() throws Exception {
        logger.warning("msg", anException);

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.WARNING, "msg", anException);

    }

    @Test
    public void testError() throws Exception {
        logger.error("msg");

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.ERROR, "msg");

    }

    @Test
    public void testError1() throws Exception {
        logger.error("msg", anException);

        verify(technicalLoggerService).log(aClass, TechnicalLogSeverity.ERROR, "msg", anException);

    }
}
