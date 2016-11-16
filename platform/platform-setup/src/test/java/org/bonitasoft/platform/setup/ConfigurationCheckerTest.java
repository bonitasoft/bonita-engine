/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
 */
package org.bonitasoft.platform.setup;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.setup.command.configure.PropertyLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

/**
 * @author Emmanuel Duchastenier
 */
public class ConfigurationCheckerTest {

    @Rule
    public TestRule clean = new ClearSystemProperties("db.admin.user", "sysprop.bonita.db.vendor", "db.user", "db.password", "db.vendor", "db.server.name=",
            "db.admin.password", "sysprop.bonita.bdm.db.vendor", "db.server.port", "db.database.name");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void validate_should_load_class_if_present() throws Exception {
        // Default org.h2.Driver should be found:
        new ConfigurationChecker(new PropertyLoader().loadProperties()).validate();
    }

    @Test
    public void validate_should_fail_to_load_class_if_not_found() throws Exception {
        final ConfigurationChecker configurationChecker = new ConfigurationChecker(
                new PropertyLoader("/database.properties", "/missingDriverClass_internal.properties").loadProperties());
        configurationChecker.loadProperties();

        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("The driver class named 'org.404.NonExistent'");

        configurationChecker.validate();
    }

    @Test
    public void validate_should_fail_mandatory_property_is_not_set() throws Exception {
        final String dbVendor = "dbVendor";
        System.setProperty("sysprop.bonita.db.vendor", dbVendor);
        final Properties propertiesWithMissingServerName = new PropertyLoader("/incomplete_database.properties").loadProperties();

        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("Mandatory property");

        new ConfigurationChecker(propertiesWithMissingServerName).validate();
    }

    @Test
    public void validate_should_fail_if_class_to_load_is_not_set() throws Exception {
        final ConfigurationChecker configurationChecker = new ConfigurationChecker(
                new PropertyLoader("/database.properties", "/incomplete_internal.properties").loadProperties());

        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("Mandatory property 'postgres.nonXaDriver'");

        configurationChecker.validate();
    }

    @Test
    public void validate_should_check_driver_loadability() throws Exception {
        final ConfigurationChecker checker = spy(new ConfigurationChecker(new PropertyLoader().loadProperties()));

        checker.validate();

        verify(checker).tryToLoadDriverClass();
    }

}
