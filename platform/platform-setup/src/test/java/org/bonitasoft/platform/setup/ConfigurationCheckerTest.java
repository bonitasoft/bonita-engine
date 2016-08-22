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

import org.bonitasoft.platform.exception.PlatformException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

/**
 * @author Emmanuel Duchastenier
 */
public class ConfigurationCheckerTest {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void tryToLoadDriverClass_should_load_class_if_present() throws Exception {
        // Default org.h2.Driver should be found:
        new ConfigurationChecker().validate();
    }

    @Test
    public void tryToLoadDriverClass_should_fail_to_load_class_if_not_found() throws Exception {
        final String wrongDriverClass = "org.404.NonExistent";
        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("The driver class named '" + wrongDriverClass);

        new ConfigurationChecker().tryToLoadDriverClass(wrongDriverClass);
    }

    @Test
    public void tryToLoadDriverClass_should_fail_if_class_to_load_is_not_set() throws Exception {
        final String dbVendor = "dbVendor";
        System.setProperty("sysprop.bonita.db.vendor", dbVendor);

        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("Driver class name not set for database " + dbVendor);

        new ConfigurationChecker().validate();
    }

    @Test
    public void validate_should_check_driver_loadability() throws Exception {
        final ConfigurationChecker checker = spy(new ConfigurationChecker());

        checker.validate();

        verify(checker).validateDriverClass();
    }

}
