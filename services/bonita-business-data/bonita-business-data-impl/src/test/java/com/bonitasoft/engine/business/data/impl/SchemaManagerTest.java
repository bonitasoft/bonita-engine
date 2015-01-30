/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
    public void executeUpdateAndDropScriptsShouldWorkWithAllSupportedTypes() {
        final BusinessObjectModel bom = BOMBuilder.aBOM().buildModelWithAllSupportedTypes();
        updateAndDropSchema(bom);
    }

    @Test
    public void executeUpdateAndDropScriptsShouldSupportConstraints() {
        final BusinessObjectModel bom = BOMBuilder.aBOM().buildModelWithConstrainedFields();
        updateAndDropSchema(bom);
    }

    protected void updateAndDropSchema(final BusinessObjectModel bom) {
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
