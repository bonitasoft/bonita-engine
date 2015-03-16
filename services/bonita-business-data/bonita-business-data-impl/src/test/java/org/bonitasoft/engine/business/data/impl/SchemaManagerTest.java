/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.business.data.impl;

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

import org.bonitasoft.engine.BOMBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;

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
