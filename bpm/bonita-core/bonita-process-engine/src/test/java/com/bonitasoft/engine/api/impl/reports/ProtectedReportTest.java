/*******************************************************************************
 * Copyright (C) 2013-2014 Bonitasoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.reports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.bonitasoft.engine.service.impl.LicenseChecker;

/**
 * @author Vincent Elcrin
 * @author Celine Souchet
 */
public class ProtectedReportTest {

    @Mock
    private LicenseChecker checker;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_an_illegal_state_exception_if_license_is_not_valid() throws Exception {
        doThrow(IllegalStateException.class).when(checker).checkLicenseAndFeature("feature");
        final ProtectedReport report = new ProtectedReport("myreport", "feature", checker);

        report.deploy("path/to/report", null);
    }

    @Test
    public void should_deploy_report_if_license_is_valid() throws Exception {
        final ProtectedReport report = new ProtectedReport("myreport", "feature", checker);

        report.deploy("src/test/resources/reports", new ReportDeployer() {

            @Override
            public void deploy(final String name, final String description, final byte[] screenShot, final byte[] content) {
                assertThat(name).isEqualTo("myreport");
            }
        });
    }
}
