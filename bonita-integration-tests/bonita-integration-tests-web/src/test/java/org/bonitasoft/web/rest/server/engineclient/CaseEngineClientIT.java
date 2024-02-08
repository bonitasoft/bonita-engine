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
package org.bonitasoft.web.rest.server.engineclient;

import static junit.framework.Assert.assertEquals;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.test.toolkit.bpm.TestCaseFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class CaseEngineClientIT extends AbstractConsoleTest {

    private CaseEngineClient caseEngineClient;

    @Override
    public void consoleTestSetUp() throws Exception {
        caseEngineClient = new CaseEngineClient(TenantAPIAccessor.getProcessAPI(getInitiator().getSession()));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test
    public void testCountNumberOfOpenCases() throws Exception {
        final long before = caseEngineClient.countOpenedCases();
        start2cases();

        long numberOfOpenCases = caseEngineClient.countOpenedCases();

        assertEquals(2L, numberOfOpenCases - before);
    }

    private void start2cases() {
        TestCaseFactory.createRandomCase(getInitiator());
        TestCaseFactory.createRandomCase(getInitiator());
    }

}
