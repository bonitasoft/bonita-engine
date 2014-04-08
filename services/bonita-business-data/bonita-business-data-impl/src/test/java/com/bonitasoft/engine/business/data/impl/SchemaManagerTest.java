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
public class SchemaManagerTest {

    @Autowired
    @Qualifier("notManagedBizDataSource")
    private DataSource modelDatasource;

    @Resource(name = "jpa-model-configuration")
    private Map<String, Object> modelConfiguration;

    private final TechnicalLoggerService loggerService = mock(TechnicalLoggerService.class);

    private SchemaManager schemaManager;

    @Before
    public void setUp() throws Exception {
        schemaManager = new SchemaManager(modelConfiguration, loggerService);
    }

    @After
    public void tearDown() throws Exception {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(modelDatasource);
        try {
            jdbcTemplate.update("drop table ComplexInvoice");
        } catch (final Exception e) {
            // e.printStackTrace();
        }
        try {
            jdbcTemplate.update("drop table ConstrainedItem");
        } catch (final Exception e) {
        }
    }

    @Test
    public void executeScriptsABOMShouldWorkWithAllSupportedTypes() throws Exception {
        final BusinessObjectModel bom = BOMBuilder.aBOM().buildModelWithAllSupportedTypes();
        final List<Exception> updateExceptions = schemaManager.update(bom.getBusinessObjectsClassNames());
        if (!updateExceptions.isEmpty()) {
            fail("Upating schema fails due to: " + updateExceptions);
        }
    }

    @Test
    public void executeScriptsABOMShouldSupportConstraints() throws Exception {
        final BusinessObjectModel bom = BOMBuilder.aBOM().buildModelWithConstrainedFields();
        final List<Exception> updateExceptions = schemaManager.update(bom.getBusinessObjectsClassNames());
        if (!updateExceptions.isEmpty()) {
            fail("Upating schema fails due to: " + updateExceptions);
        }
    }

    @Test
    public void executeDropScriptsABOMShouldWorkWithAllSupportedTypes() throws Exception {
        final BusinessObjectModel bom = BOMBuilder.aBOM().buildModelWithAllSupportedTypes();
        final List<Exception> dropExceptions = schemaManager.drop(bom.getBusinessObjectsClassNames());
        if (!dropExceptions.isEmpty()) {
            fail("Upating schema fails due to: " + dropExceptions);
        }
    }

    @Test
    public void executedropScriptsABOMShouldSupportConstraints() throws Exception {
        final BusinessObjectModel bom = BOMBuilder.aBOM().buildModelWithConstrainedFields();
        final List<Exception> exceptions = schemaManager.drop(bom.getBusinessObjectsClassNames());
        if (!exceptions.isEmpty()) {
            fail("Upating schema fails due to: " + exceptions);
        }
    }

}
