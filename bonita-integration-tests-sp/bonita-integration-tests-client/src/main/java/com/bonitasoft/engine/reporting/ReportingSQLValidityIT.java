package com.bonitasoft.engine.reporting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.bonitasoft.engine.CommonAPISPIT;

/**
 * Validate sql used in reports. Check only if there is no errors in sql, according to each supported databases
 * 
 * Run all sql files located in same package than this class and postfixed by '_test.sql' (see in resources folder)
 */
@RunWith(Parameterized.class)
public class ReportingSQLValidityIT extends CommonAPISPIT {

    @Parameters(name = "{0}")
    public static Collection<Object[]> params() {
        ArrayList<Object[]> params = new ArrayList<Object[]>();
        params.add(new Object[] { "AppName_test.sql" });
        params.add(new Object[] { "CaseAvgTime_test.sql" });
        params.add(new Object[] { "CaseHistory_test.sql" });
        params.add(new Object[] { "CaseList_test.sql" });
        params.add(new Object[] { "TaskList_test.sql" });
        return params;
    }

    private final String sql;

    public ReportingSQLValidityIT(String filename) throws IOException {
        String sql = IOUtils.toString(ReportingSQLValidityIT.class.getResourceAsStream(filename));
        this.sql = sql;
    }

    @Before
    public void setUp() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    /* expect no exception */
    @Test
    public void testSQLValidity() throws Exception {
        getReportingAPI().selectList(sql);
    }

}
