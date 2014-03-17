/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.reports;

import static junit.framework.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;


import static junit.framework.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Vincent Elcrin
 */
public class DefaultReportListTest {

    @BeforeClass
    public static void setBonitaHome() {
        System.setProperty("bonita.home", "target/home");
    }

    @Mock
    private TechnicalLoggerService logger;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void test_we_can_deploy_all_default_reports() throws Exception {

        DefaultReportList defaultReports = new DefaultReportList(logger, "src/test/resources/reports");
        final List<String> reports = new ArrayList<String>();

        defaultReports.deploy(new ReportDeployer() {

            @Override
            public void deploy(final String name, final String description, final byte[] screenShot, final byte[] content) {
                reports.add(name);
            }
        });

        assertEquals("[case_avg_time, case_list, task_list, case_history]", reports.toString());
    }
}
