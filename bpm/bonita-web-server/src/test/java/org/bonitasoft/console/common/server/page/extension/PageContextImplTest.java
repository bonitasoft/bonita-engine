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
package org.bonitasoft.console.common.server.page.extension;

import java.util.Locale;

import org.bonitasoft.console.common.server.page.PageContextAssert;
import org.bonitasoft.engine.session.APISession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class PageContextImplTest {

    public static final Locale LOCALE = Locale.FRANCE;
    public static final String PROFILE_ID = "profileId";
    @Mock
    private APISession apiSession;

    @Test
    public void testPageContext() throws Exception {
        PageContextImpl pageContext = new PageContextImpl(apiSession, LOCALE, PROFILE_ID);

        PageContextAssert.assertThat(pageContext).hasApiSession(apiSession)
                .hasLocale(LOCALE)
                .hasProfileID(PROFILE_ID);

    }
}
