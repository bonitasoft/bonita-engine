/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.toolkit.server.servlet;

import static org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.LOCALE;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.console.common.server.utils.LocaleUtils;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.server.ServletCall;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Created by Vincent Elcrin
 * Date: 23/09/13
 * Time: 16:50
 */
public class ToolkitHttpServletTest {

    ToolkitHttpServlet toolkitHttpServlet = new ToolkitHttpServlet() {

        @Override
        protected ServletCall defineServletCall(HttpServletRequest req, HttpServletResponse resp) {
            return null;
        }
    };

    @Mock
    HttpServletRequest req;

    @Mock
    HttpServletResponse resp;

    @Mock
    PrintWriter writer;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        HashMap<String, String> availableLocales;
        availableLocales = new HashMap<>();
        availableLocales.put("en", "English");
        availableLocales.put("fr", "Français");
        availableLocales.put("es", "Español");
        availableLocales.put("pt_BR", "Português (Brasil)");
        availableLocales.put("ja", "日本語");

        I18n i18n = mock(I18n.class);
        I18n.setInstance(i18n);
        Mockito.when(i18n.getAvailableLocalesFor(anyString())).thenReturn(availableLocales);
    }

    @After
    public void cleanUp() throws Exception {
        I18n.setInstance(null);
    }

    @Test
    public void testOutputExceptionPrintProperJson() throws Exception {
        APIException exception = new APIException("message");
        doReturn(writer).when(resp).getWriter();

        toolkitHttpServlet.outputException(exception, req, resp, 500);

        verify(writer).print(exception.toJson());
    }

    @Test
    public void testLocaleIsPassedFromRequestToException() throws Exception {
        APIException exception = mock(APIException.class,
                withSettings().defaultAnswer(RETURNS_MOCKS));

        doReturn(writer).when(resp).getWriter();
        doReturn(new Cookie[] {
                new Cookie(LocaleUtils.LOCALE_COOKIE_NAME, "fr_FR")
        }).when(req).getCookies();

        toolkitHttpServlet.outputException(exception, req, resp, 500);

        verify(exception).setLocale(LOCALE.fr);
    }

    @Test
    public void testIfLocaleIsNotInACookieThatBrowserLocaleIsPassedThrough() throws Exception {
        APIException exception = mock(APIException.class,
                withSettings().defaultAnswer(RETURNS_MOCKS));

        doReturn(writer).when(resp).getWriter();
        doReturn(new Cookie[0]).when(req).getCookies();
        doReturn(Locale.CANADA_FRENCH).when(req).getLocale();

        toolkitHttpServlet.outputException(exception, req, resp, 500);

        verify(exception).setLocale(LOCALE.fr);
    }

    @Test
    public void testIfLocaleIsNotInACookieNorBrowserThatDefaultLocaleIsPassedThrough() throws Exception {
        APIException exception = mock(APIException.class,
                withSettings().defaultAnswer(RETURNS_MOCKS));

        doReturn(writer).when(resp).getWriter();
        doReturn(new Cookie[0]).when(req).getCookies();
        doReturn(null).when(req).getLocale();

        toolkitHttpServlet.outputException(exception, req, resp, 500);

        verify(exception).setLocale(LOCALE.en);
    }
}
