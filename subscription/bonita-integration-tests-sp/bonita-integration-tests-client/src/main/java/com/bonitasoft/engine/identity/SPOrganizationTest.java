/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.identity;

import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.api.IdentityAPI;

/**
 * @author Celine Souchet
 */
public class SPOrganizationTest extends CommonAPISPIT {

    @After
    public void afterTest() throws Exception {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "Organization", "Encrypted password" }, jira = "ENGINE-1371")
    @Test
    public void canLoginAfterMerge() throws Exception {
        final String username = "jane";
        final String password = "mySecretP@ssw0rd";
        createUser(username, password);
        final String organization = getIdentityAPI().exportOrganization();
        getIdentityAPI().importOrganization(organization);
        logoutThenloginAs(username, password);
        logoutThenlogin();
        getIdentityAPI().deleteUser(username);
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "Organization", "Encrypted password" }, jira = "ENGINE-1371")
    @Test
    public void canLoginAfterImportEncryptedPassword() throws Exception {
        final String username = "jane";
        final String password = "mySecretP@ssw0rd";
        createUser(username, password);
        final String organization = getIdentityAPI().exportOrganization();

        // delete organization in order to create a new user
        getIdentityAPI().deleteOrganization();

        // import the organization and try to login
        getIdentityAPI().importOrganization(organization);
        logoutThenloginAs(username, password);
        logoutThenlogin();
        getIdentityAPI().deleteUser(username);
    }

}
