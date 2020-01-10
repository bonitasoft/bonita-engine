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
package org.bonitasoft.engine.bpm.bar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bpm.bar.ParameterContribution.NULL;
import static org.bonitasoft.engine.bpm.bar.ParameterContribution.PARAMETERS_FILE;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.io.PropertiesManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Emmanuel Duchastenier
 */
public class ParameterContributionTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_replace_NULL_keyword_when_reading_bar_file() throws Exception {
        // given:
        final String myProcessKey = "myProcessKey";

        final File file = temporaryFolder.newFile(PARAMETERS_FILE);
        writePropertyFile(file, myProcessKey, NULL);

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setProcessDefinition(
                new ProcessDefinitionBuilder().createNewInstance("NULL_parameter_value", "check").done());
        final BusinessArchive businessArchive = businessArchiveBuilder.done();

        // when:
        final boolean hasRead = new ParameterContribution().readFromBarFolder(businessArchive,
                temporaryFolder.getRoot());

        // then:
        assertThat(hasRead).isTrue();
        assertThat(businessArchive.getParameters()).containsEntry(myProcessKey, null);
    }

    @Test
    public void should_preserve_non_NULL_values_when_reading_bar_file() throws Exception {
        // given:
        final String myProcessKey = "myProcessKey";

        final File file = temporaryFolder.newFile(PARAMETERS_FILE);
        final String someValueToRead = "someValueToRead";
        writePropertyFile(file, myProcessKey, someValueToRead);

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setProcessDefinition(
                new ProcessDefinitionBuilder().createNewInstance("non_null_parameter", "read").done());
        final BusinessArchive businessArchive = businessArchiveBuilder.done();

        // when:
        final boolean hasRead = new ParameterContribution().readFromBarFolder(businessArchive,
                temporaryFolder.getRoot());

        // then:
        assertThat(hasRead).isTrue();
        assertThat(businessArchive.getParameters()).containsEntry(myProcessKey, someValueToRead);
    }

    @Test
    public void should_replace_null_values_with_NULL_keyword_when_writing_bar_file() throws Exception {
        // given:
        final File parametersFile = temporaryFolder.newFile(PARAMETERS_FILE);

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setProcessDefinition(
                new ProcessDefinitionBuilder().createNewInstance("parameter_value_equals_null", "check").done());
        final String myKeyWithNullValue = "myKeyWithNullValue";
        businessArchiveBuilder.setParameters(Collections.<String, String> singletonMap(myKeyWithNullValue, null));
        final BusinessArchive businessArchive = businessArchiveBuilder.done();

        // when:
        new ParameterContribution().saveToBarFolder(businessArchive, temporaryFolder.getRoot());

        // then:
        final Properties properties = PropertiesManager.getProperties(parametersFile);
        assertThat(properties.getProperty(myKeyWithNullValue)).isEqualTo(NULL);
    }

    @Test
    public void should_preserve_non_NULL_values_when_writing_bar_file() throws Exception {
        // given:
        final File parametersFile = temporaryFolder.newFile(PARAMETERS_FILE);

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.setProcessDefinition(
                new ProcessDefinitionBuilder().createNewInstance("non_null_parameter", "write").done());
        final String myKeyWithNullValue = "myKeyWithNullValue";
        final String someValue = "someValue";
        businessArchiveBuilder.setParameters(Collections.<String, String> singletonMap(myKeyWithNullValue, someValue));
        final BusinessArchive businessArchive = businessArchiveBuilder.done();

        // when:
        new ParameterContribution().saveToBarFolder(businessArchive, temporaryFolder.getRoot());

        // then:
        final Properties properties = PropertiesManager.getProperties(parametersFile);
        assertThat(properties.getProperty(myKeyWithNullValue)).isEqualTo(someValue);
    }

    private void writePropertyFile(File file, String key, String value) throws IOException {
        Properties props = new Properties();
        props.setProperty(key, value);
        PropertiesManager.saveProperties(props, file);
    }
}
