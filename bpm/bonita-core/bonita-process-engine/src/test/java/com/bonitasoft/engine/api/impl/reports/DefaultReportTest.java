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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bonitasoft.engine.io.IOUtil;
import org.junit.Test;

/**
 * Created by Vincent Elcrin
 * Date: 02/12/13
 * Time: 18:21
 */
public class DefaultReportTest {

    public static final String RESOURCES_PATH = "src/test/resources/reports";

    private final ReportDeployer emptyDeployer = new ReportDeployer() {

        @SuppressWarnings("unused")
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

            @SuppressWarnings("unused")
            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) {
                assertThat(name).isEqualTo("myreport");
            }
        });
    }

    @Test
    public void should_provide_report_content_to_deployer() throws Exception {
        DefaultReport report = new DefaultReport("myreport");
        final File zip = new File(RESOURCES_PATH, "myreport-content.zip");

        report.deploy(RESOURCES_PATH, new ReportDeployer() {

            @SuppressWarnings("unused")
            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) throws IOException {
                assertThat(content).isEqualTo(IOUtil.getAllContentFrom(zip));
            }
        });
    }

    @Test
    public void should_provide_description_to_deployer() throws Exception {
        DefaultReport report = new DefaultReport("myreport");

        report.deploy(RESOURCES_PATH, new ReportDeployer() {

            @SuppressWarnings("unused")
            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) {
                assertThat(description).isEqualTo("My report description");
            }
        });
    }

    @Test
    public void should_fail_quietly_if_no_description_found() throws Exception {
        DefaultReport report = new DefaultReport("myemptyreport");

        report.deploy(RESOURCES_PATH, new ReportDeployer() {

            @SuppressWarnings("unused")
            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) {
                assertThat(description).isNull();
            }
        });
    }

    @Test
    public void should_fail_quietly_if_no_screen_shot_found() throws Exception {
        DefaultReport report = new DefaultReport("myemptyreport");

        report.deploy(RESOURCES_PATH, new ReportDeployer() {

            @SuppressWarnings("unused")
            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) {
                assertThat(screenShot).isNull();
            }
        });
    }

    @Test
    public void DefaultReport_should_provide_screenshot_if_existing() throws Exception {
        DefaultReport report = new DefaultReport("myreport");
        final File file = new File(RESOURCES_PATH, "myreport-screenshot.png");
        assert file.exists();

        report.deploy(RESOURCES_PATH, new ReportDeployer() {

            @SuppressWarnings("unused")
            @Override
            public void deploy(String name, String description, byte[] screenShot, byte[] content) throws IOException {
                assertThat(screenShot).isEqualTo(IOUtil.getAllContentFrom(file));
            }
        });
    }

}
