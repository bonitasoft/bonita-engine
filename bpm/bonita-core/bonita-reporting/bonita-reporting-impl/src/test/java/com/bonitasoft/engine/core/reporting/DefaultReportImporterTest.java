/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import org.assertj.core.data.MapEntry;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultReportImporterTest {

    @Mock
    private ReportingServiceImpl reportingService;

    @Mock
    private TechnicalLoggerService loggerService;

    private DefaultReportImporter defaultReportImporter;

    @Before
    public void before() {
        defaultReportImporter = spy(new DefaultReportImporter(reportingService, loggerService));
    }

    @Test
    public void should_import_when_inexistant() throws Exception {
        //given
        byte[] screenshot = "plop".getBytes();
        byte[] zipContent = zip(entry("the_report-screenshot.png", "plop"),
                entry("the_report.properties", "the_report.description=The description of the report"));
        doReturn(stream(zipContent)).when(defaultReportImporter).getReportFromClassPath("the_report-content.zip");
        //when
        defaultReportImporter.invoke("the_report");
        //then
        verify(reportingService).addReport(argThat(new SReportMatcher("the_report",-1,true,"The description of the report", screenshot)), eq(zipContent));
    }


    @Test
    public void should_do_nothing_when_is_the_same() throws Exception {
        //given
        byte[] screenshot = "plop".getBytes();
        byte[] zipContent = zip(entry("the_report-screenshot.png", "plop"),
                entry("the_report.properties", "the_report.description=The description of the report"));
        doReturn(stream(zipContent)).when(defaultReportImporter).getReportFromClassPath("the_report-content.zip");

        SReportImpl existingReport = new SReportImpl();
        existingReport.setId(125l);
        doReturn(existingReport).when(reportingService).getReportByName("the_report");
        doReturn(zipContent).when(reportingService).getReportContent(125l);

        //when
        defaultReportImporter.invoke("the_report");
        //then
        verify(reportingService, never()).addReport(any(SReport.class), any(byte[].class));
    }

    @Test
    public void should_delete_and_import_when_is_different() throws Exception {
        //given
        byte[] screenshot = "plop".getBytes();
        byte[] zipContent = zip(entry("the_report-screenshot.png", "plop"),
                entry("the_report.properties", "the_report.description=The description of the report"));
        doReturn(stream(zipContent)).when(defaultReportImporter).getReportFromClassPath("the_report-content.zip");

        SReportImpl existingReport = new SReportImpl();
        existingReport.setId(125l);
        doReturn(existingReport).when(reportingService).getReportByName("the_report");
        doReturn("other".getBytes()).when(reportingService).getReportContent(125l);

        //when
        defaultReportImporter.invoke("the_report");
        //then
        ArgumentCaptor<EntityUpdateDescriptor> captor = ArgumentCaptor.forClass(EntityUpdateDescriptor.class);
        verify(reportingService, never()).addReport(any(SReport.class), any(byte[].class));
        verify(reportingService).update(any(SReport.class), captor.capture());
        EntityUpdateDescriptor value = captor.getValue();
        assertThat((byte[])value.getFields().get("content")).containsExactly(zipContent);
        assertThat((byte[])value.getFields().get("screenshot")).containsExactly(screenshot);
    }

    private InputStream stream(byte[] zip) throws IOException {
        return new ByteArrayInputStream(zip);
    }

    private byte[] zip(MapEntry... entry) throws IOException {
        HashMap<String, byte[]> zipContent = new HashMap<String, byte[]>();
        for (MapEntry mapEntry : entry) {
            zipContent.put((String) mapEntry.key, ((String) mapEntry.value).getBytes());
        }
        return IOUtil.zip(zipContent);
    }

    private class SReportMatcher extends BaseMatcher<SReport> {

        private String name;
        private long installedBy;
        private boolean provided;
        private String description;
        private byte[] screenshot;

        private SReportMatcher(String name, long installedBy, boolean provided, String description, byte[] screenshot) {
            this.name = name;
            this.installedBy = installedBy;
            this.provided = provided;
            this.description = description;
            this.screenshot = screenshot;
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof SReport) {
                SReport report = (SReport) item;
                return name.equals(report.getName()) && installedBy == report.getInstalledBy() && provided == report.isProvided()
                        && Arrays.equals(screenshot, report.getScreenshot()) && description.equals(report.getDescription());
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {

        }
    }
}
