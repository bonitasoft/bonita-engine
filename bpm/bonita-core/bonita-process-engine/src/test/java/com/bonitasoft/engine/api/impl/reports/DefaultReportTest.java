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

import org.bonitasoft.engine.io.IOUtil;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static junit.framework.Assert.*;

/**
 * Created by Vincent Elcrin
 * Date: 02/12/13
 * Time: 18:21
 */
public class DefaultReportTest {

    public static final String RESOURCES_PATH = "src/test/resources";

    private ReportDeployer emptyDeployer = new ReportDeployer() {

        @Override
        public void deploy(String name, String description, byte[] screenShot, byte[] content) {
        }
    };

    @Test(expected = FileNotFoundException.class)
    public void should_fail_to_deploy_non_existing_report_content() throws Exception {
        DefaultReport report = new DefaultReport("notexistingreport");

        report.deploy(RESOURCES_PATH, emptyDeployer);
    }

    @Test
    public void should_provide_name_to_deployer() throws Exception {
        DefaultReport report = new DefaultReport("myreport");

        report.deploy(RESOURCES_PATH, new ReportDeployer() {

            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) {
                assertEquals("myreport", name);
            }
        });
    }

    @Test
    public void should_provide_report_content_to_deployer() throws Exception {
        DefaultReport report = new DefaultReport("myreport");
        final File zip = new File(RESOURCES_PATH, "myreport-content.zip");

        report.deploy(RESOURCES_PATH, new ReportDeployer() {

            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) throws IOException {
                assertTrue(areEquals(IOUtil.getAllContentFrom(zip), content));
            }
        });
    }

    @Test
    public void should_provide_description_to_deployer() throws Exception {
        DefaultReport report = new DefaultReport("myreport");

        report.deploy(RESOURCES_PATH, new ReportDeployer() {

            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) {
                assertEquals("My report description", description);
            }
        });
    }

    @Test
    public void should_fail_quietly_if_no_description_found() throws Exception {
        DefaultReport report = new DefaultReport("myemptyreport");

        report.deploy(RESOURCES_PATH, new ReportDeployer() {

            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) {
                assertNull(description);
            }
        });
    }

    @Test
    public void should_fail_quietly_if_no_screen_shot_found() throws Exception {
        DefaultReport report = new DefaultReport("myemptyreport");

        report.deploy(RESOURCES_PATH, new ReportDeployer() {

            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) {
                assertNull(screenShot);
            }
        });
    }

    private boolean areEquals(byte[] expected, byte[] actual) {
        for(int i = 0; i < expected.length; i++) {
            if(expected[i] != actual[i]) {
                return false;
            }
        }
        return true;
    }
}
