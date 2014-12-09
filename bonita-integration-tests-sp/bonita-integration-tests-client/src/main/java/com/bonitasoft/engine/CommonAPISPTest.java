/*******************************************************************************
 * Copyright (C) 2009, 2014 Bonitasoft S.A.
 * BonitaSoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.BonitaSuiteRunner.Initializer;
import org.bonitasoft.engine.BonitaTestRunner;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.PlatformSession;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bonitasoft.engine.api.PlatformAPI;
import com.bonitasoft.engine.api.PlatformAPIAccessor;
import com.bonitasoft.engine.platform.Tenant;
import org.xml.sax.SAXException;

@RunWith(BonitaTestRunner.class)
@Initializer(TestsInitializerSP.class)
public abstract class CommonAPISPTest extends APITestSPUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonAPISPTest.class);

    @Rule
    public TestRule testWatcher = new TestWatcher() {

        @Override
        public void starting(final Description d) {
            LOGGER.info("Starting test: " + d.getClassName() + "." + d.getMethodName());
        }

        @Override
        public void failed(final Throwable cause, final Description d) {
            LOGGER.error("Failed test: " + d.getClassName() + "." + d.getMethodName());
            try {
                clean();
            } catch (final Exception be) {
                LOGGER.error("Unable to clean db", be);
            } finally {
                LOGGER.info("-----------------------------------------------------------------------------------------------");
            }
        }

        @Override
        public void succeeded(final Description d) {
            try {
                List<String> clean = null;
                try {
                    clean = clean();
                } catch (final BonitaException e) {
                    throw new BonitaRuntimeException(e);
                }
                LOGGER.info("Succeeded test: " + d.getClassName() + "." + d.getMethodName());
                if (!clean.isEmpty()) {
                    throw new BonitaRuntimeException(clean.toString());
                }
            } finally {
                LOGGER.info("-----------------------------------------------------------------------------------------------");
            }
        }
    };

    /**
     * FIXME: clean actors!
     * 
     * @return
     * @throws BonitaException
     */
    private List<String> clean() throws BonitaException {
        final List<String> messages = new ArrayList<String>();
        final PlatformSession platformSession = BPMTestSPUtil.loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(platformSession);
        final List<Tenant> tenants = platformAPI.searchTenants(new SearchOptionsBuilder(0, 100).done()).getResult();
        BPMTestSPUtil.logoutOnPlatform(platformSession);

        for (final Tenant tenant : tenants) {
            loginOnTenantWithTechnicalLogger(tenant.getId());
            if (getTenantManagementAPI().isPaused()) {
                messages.add("Tenant was in paused state");
                getTenantManagementAPI().resume();
            }
            messages.addAll(checkNoCommands());
            messages.addAll(checkNoUsers());
            messages.addAll(checkNoGroups());
            messages.addAll(checkNoRoles());
            messages.addAll(checkNoProcessDefinitions());
            messages.addAll(checkNoProcessIntances());
            messages.addAll(checkNoArchivedProcessIntances());
            messages.addAll(checkNoFlowNodes());
            messages.addAll(checkNoArchivedFlowNodes());
            messages.addAll(checkNoCategories());
            messages.addAll(checkNoComments());
            messages.addAll(checkNoArchivedComments());
            messages.addAll(checkNoBreakpoints());
            messages.addAll(checkNoReports());

            // FIXME : Uncomment when fix bug : BS-9436
            //            messages.addAll(checkNoActiveTransactions());

            // FIXME : Uncomment when fix bug : BS-7206
            //messages.addAll(checkNoDataMappings());
            logoutOnTenant();
        }
        return messages;
    }

    protected void assertThatXmlHaveNoDifferences(final String xmlPrettyFormatExpected, final String xmlPrettyFormatExported) throws SAXException, IOException {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        final DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(xmlPrettyFormatExported, xmlPrettyFormatExpected));
        final List<?> allDifferences = diff.getAllDifferences();
        assertThat(allDifferences).as("should have no differences between:\n%s\n and:\n%s\n", xmlPrettyFormatExpected, xmlPrettyFormatExported).isEmpty();
    }

}
