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
package org.bonitasoft.engine;

import org.bonitasoft.engine.application.ApplicationIT;
import org.bonitasoft.engine.business.application.LivingApplicationIT;
import org.bonitasoft.engine.business.application.LivingApplicationImportExportIT;
import org.bonitasoft.engine.business.application.LivingApplicationMenuIT;
import org.bonitasoft.engine.business.application.LivingApplicationPageIT;
import org.bonitasoft.engine.business.data.BDRepositoryLocalIT;
import org.bonitasoft.engine.form.FormMappingIT;
import org.bonitasoft.engine.page.PageAPIIT;
import org.bonitasoft.engine.tenant.TenantMaintenanceLocalIT;
import org.bonitasoft.engine.test.APIMethodLocalIT;
import org.bonitasoft.engine.test.BPMLocalSuiteTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses({
        TestShades.class,
        BPMLocalSuiteTests.class,
        BPMRemoteTestsLocal.class,
        FormMappingIT.class,
        PageAPIIT.class,
        LivingApplicationIT.class,
        LivingApplicationPageIT.class,
        LivingApplicationMenuIT.class,
        LivingApplicationImportExportIT.class,
        ApplicationIT.class,
        DeleteEventTriggerInstanceIT.class,
        APIMethodLocalIT.class,
        TenantMaintenanceLocalIT.class,
        BDRepositoryLocalIT.class,
        // last test suite because it breaks the platform
        AllBPMTests.class,
})
@RunWith(Suite.class)
public class LocalIntegrationTests {

    @BeforeClass
    public static void beforeClass() {
        System.err.println("=================== LocalIntegrationTests setup");
    }

    @AfterClass
    public static void afterClass() {
        System.err.println("=================== LocalIntegrationTests afterClass");
    }

}
