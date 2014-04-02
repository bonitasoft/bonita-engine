package com.bonitasoft.engine.business.data.impl;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bonitasoft.engine.BOMBuilder;
import com.bonitasoft.engine.bdm.BusinessObjectModel;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
public class SchemaUpdaterTest {

    @Autowired
    @Qualifier("notManagedBizDataSource")
    private DataSource modelDatasource;

    @Resource(name = "jpa-model-configuration")
    private Map<String, Object> modelConfiguration;

    private final TechnicalLoggerService loggerService = mock(TechnicalLoggerService.class);

    private SchemaUpdater schemaUpdater;

    @Before
    public void setUp() throws Exception {
        schemaUpdater = new SchemaUpdater(modelConfiguration, loggerService);
    }

    @After
    public void tearDown() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(modelDatasource);
        try {
            jdbcTemplate.update("drop table ComplexInvoice");
        } catch (Exception e) {
            // ignore drop of non-existing table
        }
    }

    @Test
    public void executeScriptsABOMShouldWorkWithAllSupportedTypes() throws Exception {
        BOMBuilder bomBuilder = BOMBuilder.aBOM();
        BusinessObjectModel bom = bomBuilder.buildModelWithAllSupportedTypes();

        schemaUpdater.execute(bom.getBusinessObjectsClassNames());
        List<Exception> updateExceptions = schemaUpdater.getExceptions();
        if (!updateExceptions.isEmpty()) {
            fail("Upating schema fails due to: " + updateExceptions);
        }
    }

}
