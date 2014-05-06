/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.BonitaTestRunner;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.PlatformLoginException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.PlatformSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.platform.TenantCreator;
import com.bonitasoft.engine.platform.TenantDeactivationException;
import com.bonitasoft.engine.platform.TenantNotFoundException;

/**
 * @author Laurent Leseigneur
 */
@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializerSP.class)
public class TenantLocalTestSpITest extends CommonAPISPTest {

    private static final int EXPECTED_PAGE_SEARCH_RESULT_COUNT = 1;

    private static PlatformAPI platformAPI;

    private static PlatformLoginAPI platformLoginAPI;

    private static PlatformSession platFormSession;

    @BeforeClass
    public static void beforeClass() throws BonitaException {
        platformLoginAPI = PlatformAPIAccessor.getPlatformLoginAPI();
    }

    @AfterClass
    public static void afterClass() throws BonitaException {
        platformLoginAPI.logout(platFormSession);
    }

    public static class TenantParameter {

        private String tenantName;

        private String technicalUsername;

        private String technicalPassword;

        private String description;

        private long tenantId;

        private String actorUsername;

        private String actorPassword;

        private long processDefinitionId;

        public long getProcessDefinitionId() {
            return processDefinitionId;
        }

        public TenantParameter(final String tenantUniqueName) {
            this.setTenantName(tenantUniqueName);

            this.setTechnicalUsername("install");
            this.setTechnicalPassword("install");
            this.setDescription("description " + tenantName);
            this.setActorUsername("john");
            this.setActorPassword("password");
        }

        public String getTenantName() {
            return tenantName;
        }

        public void setTenantName(final String tenantName) {
            this.tenantName = tenantName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public void setTentantId(final long tenantId) {
            this.tenantId = tenantId;
        }

        public long getTenantId() {
            return tenantId;
        }

        public String getTechnicalUsername() {
            return technicalUsername;
        }

        public void setTechnicalUsername(final String technicalUsername) {
            this.technicalUsername = technicalUsername;
        }

        public String getTechnicalPassword() {
            return technicalPassword;
        }

        public void setTechnicalPassword(final String technicalPassword) {
            this.technicalPassword = technicalPassword;
        }

        public String getActorUsername() {
            return actorUsername;
        }

        public void setActorUsername(final String actorUsername) {
            this.actorUsername = actorUsername;
        }

        public String getActorPassword() {
            return actorPassword;
        }

        public void setActorPassword(final String actorPassword) {
            this.actorPassword = actorPassword;
        }

        public void setProcessDefinitionId(final long processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
        }
    }

    private TenantParameter createTenant(final String uniqueName)
            throws BonitaException {

        logAsPlatformAdmin();

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(uniqueName);
        stringBuilder.append("_");
        stringBuilder.append(new Date().getTime());
        final String tenantUniqueName = stringBuilder.toString();
        final TenantParameter tenantParameter = new TenantParameter(tenantUniqueName);

        long tenantId = platformAPI.createTenant(new TenantCreator(
                tenantParameter.getTenantName(), tenantParameter
                        .getDescription(), "testIconName", "testIconPath",
                tenantParameter.getTechnicalUsername(), tenantParameter
                        .getTechnicalPassword()));
        platformAPI.activateTenant(tenantId);
        tenantId = platformAPI.getTenantByName(tenantParameter.getTenantName())
                .getId();
        tenantParameter.setTentantId(tenantId);
        logAsTechnicalUserOnTenant(tenantParameter);
        getIdentityAPI().createUser("john", "password");

        return tenantParameter;
    }

    private void logAsTechnicalUserOnTenant(
            final TenantParameter tenantParameter) throws BonitaException {
        loginWith(tenantParameter.getTechnicalUsername(),
                tenantParameter.getTechnicalPassword(),
                tenantParameter.getTenantId());
    }

    private void logAsPlatformAdmin() throws PlatformLoginException,
            BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        platFormSession = platformLoginAPI.login("platformAdmin", "platform");
        platformAPI = PlatformAPIAccessor.getPlatformAPI(platFormSession);
    }

    @Test
    public void createNewTenant_should_deploy_provided_customPage_example() throws Exception {
        // given
        final TenantParameter createTenant = createTenant("new tenant");

        // when then
        checkThatPageServiceExamplesAreDeployedOnTenant(createTenant.getTenantId());

        // clean up
        deactivateAndDeleteTenant(createTenant);

    }

    @Test
    public void createDefaultTenant_should_deploy_provided_customPage_example() throws Exception {
        // given
        final long tenantId = platformAPI.getDefaultTenant().getId();

        // when then
        checkThatPageServiceExamplesAreDeployedOnTenant(tenantId);
    }

    private void checkThatPageServiceExamplesAreDeployedOnTenant(final long tenantId) throws BonitaException, SearchException {
        // given
        login(tenantId);

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, EXPECTED_PAGE_SEARCH_RESULT_COUNT).done());

        // then
        assertThat(searchPages.getResult()).as("should have:" + EXPECTED_PAGE_SEARCH_RESULT_COUNT + " provided pages on tenantId:" + tenantId).hasSize(
                EXPECTED_PAGE_SEARCH_RESULT_COUNT);

        // clean up
        logout();
    }

    private void deactivateAndDeleteTenant(final TenantParameter tenantParameter)
            throws PlatformLoginException, BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException,
            TenantNotFoundException, TenantDeactivationException,
            DeletionException {
        logAsPlatformAdmin();
        platformAPI.deactiveTenant(tenantParameter.getTenantId());
        platformAPI.deleteTenant(tenantParameter.getTenantId());
    }

}
