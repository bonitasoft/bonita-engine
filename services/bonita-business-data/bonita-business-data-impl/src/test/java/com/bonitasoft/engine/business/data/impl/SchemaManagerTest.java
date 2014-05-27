package com.bonitasoft.engine.business.data.impl;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bonitasoft.engine.BOMBuilder;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;

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

    @Test
    public void executeUpdateAndDropScriptsShouldWorkWithAllSupportedTypes() throws Exception {
        final BusinessObjectModel bom = BOMBuilder.aBOM().buildModelWithAllSupportedTypes();
        updateAndDropSchema(bom);
    }

    @Test
    public void executeUpdateAndDropScriptsShouldSupportConstraints() throws Exception {
        final BusinessObjectModel bom = BOMBuilder.aBOM().buildModelWithConstrainedFields();
        updateAndDropSchema(bom);
    }

    protected void updateAndDropSchema(final BusinessObjectModel bom) throws SBusinessDataRepositoryDeploymentException {
        final List<Exception> updateExceptions = schemaManager.update(bom.getBusinessObjectsClassNames());
        if (!updateExceptions.isEmpty()) {
            fail("Upating schema fails due to: " + updateExceptions);
        }
        final List<Exception> dropExceptions = schemaManager.drop(bom.getBusinessObjectsClassNames());
        if (!dropExceptions.isEmpty()) {
            fail("Upating schema fails due to: " + dropExceptions);
        }
    }

}
