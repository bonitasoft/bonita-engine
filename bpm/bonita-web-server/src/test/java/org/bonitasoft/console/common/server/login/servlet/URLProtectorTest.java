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
package org.bonitasoft.console.common.server.login.servlet;

import static org.junit.Assert.*;

import org.junit.Test;

public class URLProtectorTest {

    URLProtector urlProtecter = new URLProtector();

    @Test
    public void testProtectRedirectUrlShouldRemoveHTTPFromURL() {
        assertEquals("google", urlProtecter.protectRedirectUrl("httpgoogle"));
    }

    @Test
    public void testProtectRedirectUrlShouldRemoveHTTPSFromURL() {
        assertEquals("google", urlProtecter.protectRedirectUrl("httpsgoogle"));
    }

    @Test
    public void testProtectRedirectUrlShouldNotChangeURL() {
        assertEquals("apps/#home", urlProtecter.protectRedirectUrl("apps/#home"));
        assertEquals("/bonita/apps/#login", urlProtecter.protectRedirectUrl("/bonita/apps/#login"));
        assertEquals("/apps/appDirectoryBonita", urlProtecter.protectRedirectUrl("/apps/appDirectoryBonita"));
    }

    @Test
    public void it_should_filter_capital_letters() {
        assertEquals(":.google.com", urlProtecter.protectRedirectUrl("HTTPS://WWW.google.com"));
    }

    @Test
    public void it_should_filter_double_backslash() {
        assertEquals(".google.com", urlProtecter.protectRedirectUrl("//www.google.com"));
        assertEquals("google.com", urlProtecter.protectRedirectUrl("//google.com"));
    }

}
