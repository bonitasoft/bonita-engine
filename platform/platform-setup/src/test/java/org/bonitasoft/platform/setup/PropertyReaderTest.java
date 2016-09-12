package org.bonitasoft.platform.setup;

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
