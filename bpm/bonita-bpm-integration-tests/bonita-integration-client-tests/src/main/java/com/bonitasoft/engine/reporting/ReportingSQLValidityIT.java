package com.bonitasoft.engine.reporting;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.exception.BonitaException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.bonitasoft.engine.CommonAPISPTest;

/**
 * Validate sql used in reports. Check only if there is no errors in sql, according to each supported databases
 * 
 * Run all sql files located in same package than this class and postfixed by '_test.sql' (see in resources folder)
 */
@RunWith(Parameterized.class)
public class ReportingSQLValidityIT extends CommonAPISPTest {

    @Parameters(name = "{1}")
    public static Collection<Object[]> params() throws URISyntaxException, IOException {
        ArrayList<Object[]> params = new ArrayList<Object[]>();
        for (Resource resource : getSqlTestsFiles(params)) {
            params.add(new Object[] { resource.getFile(), resource.getFile().getName() });
        }
        return params;
    }

    private static Resource[] getSqlTestsFiles(ArrayList<Object[]> list) throws URISyntaxException, IOException {
        String reportingPackage = ReportingSQLValidityIT.class.getPackage().getName().replace(".", File.separator);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        String locationPattern = "classpath*:" + reportingPackage + File.separator + "*_test.sql";
        Resource[] resources = resolver.getResources(locationPattern);
        if (resources.length == 0) {
            throw new RuntimeException("No sql test files found");
        }
        return resources;
    }

    private String sql;

    /* FileName is not used, only for testName printing */
    public ReportingSQLValidityIT(File sqlFile, String fileName) throws IOException {
        this.sql = IOUtils.toString(sqlFile.toURI());
    }

    @Before
    public void setUp() throws BonitaException {
        login();
    }

    /* expect no exception */
    @Test
    public void testSQLValidity() throws Exception {
        getReportingAPI().selectList(sql);
    }

}
