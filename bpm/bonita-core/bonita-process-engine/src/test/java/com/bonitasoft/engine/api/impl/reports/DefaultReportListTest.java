/*
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
 */

package com.bonitasoft.engine.api.impl.reports;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Vincent Elcrin
 * Date: 02/12/13
 * Time: 16:42
 */
public class DefaultReportListTest {

    @Mock
    private TechnicalLoggerService logger;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void test_we_can_deploy_all_default_reports() throws Exception {

        DefaultReportList defaultReports = new DefaultReportList(logger, "src/test/resources");
        final List<String> reports = new ArrayList<String>();

        defaultReports.deploy(new ReportDeployer() {

            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) {
                reports.add(name);
            }
        });

        assertEquals("[case_avg_time, case_list, task_list]", reports.toString());
    }
}
