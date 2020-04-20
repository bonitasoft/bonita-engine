/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import org.bonitasoft.engine.form.FormMappingIT;
import org.bonitasoft.engine.page.PageAPIIT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        //        TestShadesIT.class, // inheritance in SP, cannot be moved to src/test/java !
        BPMRemoteTestsLocal.class, // client, stays in client as is for now
        FormMappingIT.class, // client
        PageAPIIT.class, // client
        LivingApplicationIT.class, // client
        LivingApplicationPageIT.class, // client
        LivingApplicationMenuIT.class, // client
        LivingApplicationImportExportIT.class, // client
        ApplicationIT.class, // client
})
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
