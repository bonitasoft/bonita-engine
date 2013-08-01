/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Celine Souchet
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServerAPIImpl.class })
public class ServerAPIImplTest {

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ServerAPIImpl#invokeMethod(java.util.Map, java.lang.String, java.lang.String, java.util.List, java.lang.Object[])}.
     * 
     * @throws Exception
     */
    @Test(expected = ServerWrappedException.class)
    public void invokeMethodCatchUndeclaredThrowableException() throws Exception {
        final Map<String, Serializable> options = new HashMap<String, Serializable>();
        final String apiInterfaceName = "apiInterfaceName";
        final String methodName = "methodName";
        final List<String> classNameParameters = new ArrayList<String>();
        final Object[] parametersValues = null;

        final ServerAPIImpl mockedServerAPIImpl = mock(ServerAPIImpl.class);
        doReturn(mock(SessionAccessor.class)).when(mockedServerAPIImpl, "beforeInvokeMethod", options, apiInterfaceName);
        final TechnicalLoggerService technicalLogger = mock(TechnicalLoggerService.class);
        when(technicalLogger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.DEBUG))).thenReturn(true);
        doThrow(new UndeclaredThrowableException(null, "")).when(mockedServerAPIImpl, "invokeAPI", apiInterfaceName, methodName, classNameParameters,
                parametersValues, null);

        doCallRealMethod().when(mockedServerAPIImpl, "setTechnicalLogger", technicalLogger);
        // Let's call it for real:
        doCallRealMethod().when(mockedServerAPIImpl).invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
        try {
            mockedServerAPIImpl.setTechnicalLogger(technicalLogger);
            mockedServerAPIImpl.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
        } finally {
            verify(technicalLogger, VerificationModeFactory.atLeastOnce()).log(any(Class.class), eq(TechnicalLogSeverity.DEBUG), Mockito.any(Throwable.class));
        }
    }
}
