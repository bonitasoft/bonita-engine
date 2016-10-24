/**
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
 **/

package org.bonitasoft.platform.setup.command.configure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

/**
 * @author Emmanuel Duchastenier
 */
public class PropertyReaderTest {

    @Rule
    public TestRule clean = new RestoreSystemProperties();

    @Test
    public void properties_file_values_can_be_overridden_by_system_properties() throws Exception {
        // given:
        final Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/database.properties"));

        final PropertyReader bdmConfig = new PropertyReader(properties);
        assertThat(bdmConfig.getPropertyAndFailIfNull("bdm.db.vendor")).isEqualTo("oracle");

        // when:
        System.setProperty("bdm.db.vendor", "otherValue");

        // then:
        assertThat(bdmConfig.getPropertyAndFailIfNull("bdm.db.vendor")).isEqualTo("otherValue");
    }
}
