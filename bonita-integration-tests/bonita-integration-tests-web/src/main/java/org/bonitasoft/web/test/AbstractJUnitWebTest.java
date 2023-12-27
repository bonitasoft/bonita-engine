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
package org.bonitasoft.web.test;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.console.common.server.utils.PlatformManagementUtils;
import org.bonitasoft.test.toolkit.AbstractJUnitTest;
import org.bonitasoft.test.toolkit.organization.TestToolkitCtx;
import org.bonitasoft.web.toolkit.client.data.item.Item;

/**
 * @author Vincent Elcrin
 */
public abstract class AbstractJUnitWebTest extends AbstractJUnitTest {

    /**
     * the request param for the username
     */
    public static final String USERNAME_SESSION_PARAM = "username";

    public static final String API_SESSION_PARAM_KEY = "apiSession";

    @Override
    protected void testSetUp() throws Exception {
        // toolkit initialization
        I18n.getInstance();
        new PlatformManagementUtils().initializePlatformConfiguration();
        webTestSetUp();
    }

    @Override
    protected TestToolkitCtx getContext() {
        return TestToolkitCtx.getInstance();
    }

    @Override
    protected void testTearDown() {
        webTestTearDown();
    }

    public abstract void webTestSetUp() throws Exception;

    protected void webTestTearDown() {
    }

    protected boolean areEquals(Item item1, Item item2) {
        return item1.getAttributes().equals(item2.getAttributes());
    }
}
