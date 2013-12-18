/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package com.bonitasoft.engine.api.impl.reports;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.MockitoAnnotations.initMocks;

import com.bonitasoft.engine.service.impl.LicenseChecker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Vincent Elcrin
 *
 */
public class ProtectedReportTest {

    @Mock
    private LicenseChecker checker;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_an_illegal_state_exception_if_license_is_not_valid() throws Exception {
        doThrow(IllegalStateException.class).when(checker).checkLicenceAndFeature("feature");
        ProtectedReport report = new ProtectedReport("myreport", "feature", checker);

        report.deploy("path/to/report", null);
    }

    @Test
    public void should_deploy_report_if_license_is_valid() throws Exception {
        ProtectedReport report = new ProtectedReport("myreport", "feature", checker);

        report.deploy("src/test/resources/reports", new ReportDeployer() {

            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) throws Exception {
                assertEquals("myreport", name);
            }
        });
    }
}
