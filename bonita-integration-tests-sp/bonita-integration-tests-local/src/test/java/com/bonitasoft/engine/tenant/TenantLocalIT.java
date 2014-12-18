/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.TestsInitializerSP;
import com.bonitasoft.engine.page.Page;

/**
 * @author Laurent Leseigneur
 * @author Celine Souchet
 */
@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializerSP.class)
public class TenantLocalIT extends CommonAPISPTest {

    private static final int EXPECTED_PAGE_SEARCH_RESULT_COUNT = 1;

    @Test
    public void createNewTenant_should_deploy_provided_customPage_example() throws Exception {
        // given
        final long tenantId = createAndActivateTenant("new tenant");

        // when then
        checkThatPageServiceExamplesAreDeployedOnTenant(tenantId);

        // clean up
        BPMTestSPUtil.deactivateAndDeleteTenant(tenantId);
    }

    @Test
    public void createDefaultTenant_should_deploy_provided_customPage_example() throws Exception {
        // given
        final long tenantId = BPMTestSPUtil.loginOnDefaultTenantWithDefaultTechnicalUser().getTenantId();

        // when then
        checkThatPageServiceExamplesAreDeployedOnTenant(tenantId);
    }

    private void checkThatPageServiceExamplesAreDeployedOnTenant(final long tenantId) throws BonitaException, SearchException {
        // given
        loginOnTenantWithTechnicalLogger(tenantId);

        // when
        final SearchResult<Page> searchPages = getPageAPI().searchPages(new SearchOptionsBuilder(0, EXPECTED_PAGE_SEARCH_RESULT_COUNT).done());

        // then
        assertThat(searchPages.getResult()).as("should have:" + EXPECTED_PAGE_SEARCH_RESULT_COUNT + " provided pages on tenantId:" + tenantId).hasSize(
                EXPECTED_PAGE_SEARCH_RESULT_COUNT);

        // clean up
        logoutOnTenant();
    }

}
