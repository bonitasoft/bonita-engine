/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.process;

import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;

import com.bonitasoft.engine.CommonAPISPIT;

public class InterruptProcessInstanceTest extends CommonAPISPIT {

    protected User pedro;

    @Before
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        pedro = createUser("pedro", "secreto");
    }

    @After
    public void afterTest() throws Exception {
        deleteUser(pedro);
       logoutOnTenant();
    }

}
