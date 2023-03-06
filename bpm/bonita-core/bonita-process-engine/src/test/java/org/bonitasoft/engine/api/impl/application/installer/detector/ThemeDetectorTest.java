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
package org.bonitasoft.engine.api.impl.application.installer.detector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.IOUtils.createTempFile;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Emmanuel Duchastenier
 */
public class ThemeDetectorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void isCompliant_should_accept_if_theme_css_file_exists() throws Exception {
        // given:
        final byte[] content = zip(file("page.properties", "name=custompage_test1\ncontentType=theme"),
                file("resources/theme.css", "someContent"));
        File tempFile = createTempFile("custom-theme", "zip", content);

        // when:
        final boolean compliant = new ThemeDetector().isCompliant(tempFile);

        // then:
        assertThat(compliant).isTrue();

        //cleanup
        tempFile.delete();
    }

    @Test
    public void isCompliant_should_reject_if_page_is_not_a_theme() throws Exception {
        // given:
        byte[] zip = zip(file("page.properties", "name=custompage_test1\ncontentType=layout"),
                file("resources/index.html", "someContent"));
        File tempFile = createTempFile("layout", "zip", zip);

        // when:
        final boolean compliant = new ThemeDetector().isCompliant(tempFile);

        // then:
        assertThat(compliant).isFalse();

        //cleanup
        tempFile.delete();
    }

    @Test
    public void isCompliant_should_reject_theme_css_file_absence() throws Exception {
        // given:
        byte[] zip = zip(file("page.properties", "contentType=theme"));
        File tempFile = createTempFile("invalid-theme", "zip", zip);

        // when:
        final boolean compliant = new ThemeDetector().isCompliant(tempFile);

        // then:
        assertThat(compliant).isFalse();

        //cleanup
        tempFile.delete();
    }
}
