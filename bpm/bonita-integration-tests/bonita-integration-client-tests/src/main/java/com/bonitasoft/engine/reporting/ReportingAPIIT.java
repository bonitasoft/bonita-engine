package com.bonitasoft.engine.reporting;

import java.util.Collections;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.reporting.Report;
import org.bonitasoft.engine.reporting.ReportSearchDescriptor;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;

public class ReportingAPIIT extends CommonAPISPTest {

    @Before
    public void setUp() throws BonitaException {
        login();
    }

    @After
    public void tearDown() throws BonitaException {
        logout();
    }

    @Test
    public void addGetAndDeleteReport() throws BonitaException {
        final Report report = getReportingAPI().addReport("report1", null, null);
        Assert.assertEquals("report1", report.getName());
        Assert.assertFalse(report.isProvided());

        getReportingAPI().deleteReport(report.getId());
    }

    @Test
    public void searchProfiles() throws BonitaException {
        final Report report = getReportingAPI().addReport("report1", null, null);
        final SearchOptionsImpl options = new SearchOptionsImpl(0, 10);
        options.addFilter(ReportSearchDescriptor.NAME, "report1");
        final SearchResult<Report> searchReports = getReportingAPI().searchReports(options);
        Assert.assertEquals(1, searchReports.getCount());
        final Report report2 = searchReports.getResult().get(0);
        Assert.assertEquals(report, report2);

        getReportingAPI().deleteReports(Collections.singletonList(report.getId()));
    }

}
